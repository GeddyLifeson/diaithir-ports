package net.diaithir.branding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/**
 * The Age whose screen look the shared screens (vanilla, tooltips and Jade, FTB Quests, Sophisticated Core) wear.
 * <p>
 * diaithir/ui_ages/{i..vii,rodos}.zip hold each Age's look of those screens (docs/ui/UI_SCHEMA.md section 6). The config
 * {@code uiAge} is a fixed Age ("I" to "VII", "RODOS") or "auto", which follows the player's Codex: the Codex stamps every
 * copy with its form in custom_model_data (a string), and the carried copy's form is the player's Age. A change selects the
 * matching zip on top of the other resource packs and reloads the resources, which is once per Age.
 */
final class UiAge {
    static final List<String> AGES = List.of("I", "II", "III", "IV", "V", "VI", "VII", "RODOS");
    private static final Set<String> IDS = Set.copyOf(AGES.stream().map(UiAge::packId).toList());
    static final ModConfigSpec SPEC;
    private static final ModConfigSpec.ConfigValue<String> CONFIG;

    static {
        var b = new ModConfigSpec.Builder();
        CONFIG = b.comment("Age look of the shared screens: auto (follow the Codex), I, II, III, IV, V, VI, VII or RODOS").define("uiAge", "auto");
        SPEC = b.build();
    }

    private static String applied = "I";  // the Age look in effect; Age I is also what the base packs carry
    private static int tick;
    private static boolean firstCheck = true;

    private UiAge() {}

    static String packId(String age) {
        return "file/" + age.toLowerCase(Locale.ROOT) + ".zip";
    }

    static void addPacks(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;
        var dir = Minecraft.getInstance().gameDirectory.toPath().resolve("diaithir/ui_ages");
        event.addRepositorySource(new FolderRepositorySource(dir, PackType.CLIENT_RESOURCES,
                PackSource.create(PackSource.NO_DECORATION, false), new DirectoryValidator(path -> false)));
    }

    /** A form string from the Codex stamp ("III", "age_iii", "3", "rodos") as an Age, or null when it is not one. */
    static String parse(String s) {
        if (s == null) return null;
        var t = s.trim().toUpperCase(Locale.ROOT).replaceFirst("^(AGE|FORM)[_ :-]*", "");
        if (AGES.contains(t)) return t;
        if (t.length() == 1 && t.charAt(0) >= '1' && t.charAt(0) <= '7') return AGES.get(t.charAt(0) - '1');
        return null;
    }

    private static String carriedAge(Inventory inv) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            CustomModelData cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
            if (stack.isEmpty() || cmd == null || !BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().contains("codex")) continue;
            for (String s : cmd.strings()) {
                String age = parse(s);
                if (age != null) return age;
            }
        }
        return null;
    }

    static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!firstCheck && ++tick % 40 != 0) return;
        if (mc.getOverlay() != null) return;  // wait for the first resource load
        String want = parse(CONFIG.get());
        if (want == null && mc.player != null) want = carriedAge(mc.player.getInventory());
        if (want == null) want = firstCheck ? applied : null;
        firstCheck = false;
        if (want == null || want.equals(applied)) return;
        var repo = mc.getResourcePackRepository();
        if (!repo.isAvailable(packId(want))) return;  // the zips are missing: stay on the Age I look
        List<String> ids = new ArrayList<>(repo.getSelectedIds());
        ids.removeAll(IDS);
        ids.add(packId(want));
        repo.setSelected(ids);
        mc.options.updateResourcePacks(repo);
        applied = want;
        mc.reloadResourcePacks();
    }
}
