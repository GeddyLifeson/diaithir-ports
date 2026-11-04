package net.diaithir.footsteps;

import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@Mod(DiaithirFootsteps.ID)
public final class DiaithirFootsteps {
    public static final String ID = "diaithirfootsteps";
    private static final Logger LOG = LogUtils.getLogger();
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final Map<LivingEntity, Boolean> LEFT_FOOT = new WeakHashMap<>();
    private static final Layers LAYERS = load();
    private static boolean announced;

    public DiaithirFootsteps() {
        NeoForge.EVENT_BUS.addListener(PlayLevelSoundEvent.AtPosition.class, DiaithirFootsteps::onSound);
    }

    private static Layers load() {
        try (var in = DiaithirFootsteps.class.getResourceAsStream("/data/diaithirfootsteps/footsteps.json")) {
            return Layers.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("footsteps.json is missing", e);
        }
    }

    // Step sounds are played at a position, not at an entity, so the walker is the nearest living thing standing there.
    private static void onSound(PlayLevelSoundEvent.AtPosition event) {
        if (event.getSound() == null) return;
        String path = event.getSound().value().location().getPath();
        if (!LAYERS.isStep(path)) return;
        Vec3 at = event.getPosition();
        LivingEntity who = event.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(0.6, 0.5, 0.6), e -> !e.isSpectator())
            .stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(at))).orElse(null);
        if (who == null) return;
        if (!announced) {
            announced = true;
            LOG.info("First footstep layered ({} on {})", path, who.getName().getString());
        }
        boolean left = LEFT_FOOT.merge(who, true, (was, one) -> !was);
        event.setNewPitch(LAYERS.stepPitch(event.getNewPitch(), left));
        List<String> worn = new ArrayList<>();
        for (EquipmentSlot slot : ARMOR) worn.add(material(who.getItemBySlot(slot)));
        play(who, LAYERS.armorLayer(worn, who.isShiftKeyDown(), who.isSprinting()));
        play(who, LAYERS.wetLayer(who.level().isRainingAt(who.blockPosition()), who.isShiftKeyDown()));
    }

    /** The armour material name from the item's equipment asset, e.g. minecraft:iron gives iron. Empty when not armour. */
    private static String material(ItemStack stack) {
        Equippable eq = stack.get(DataComponents.EQUIPPABLE);
        return eq == null || eq.assetId().isEmpty() ? "" : eq.assetId().get().identifier().getPath();
    }

    private static void play(LivingEntity who, Layers.Layer layer) {
        if (layer == null || layer.volume() <= 0F) return;
        SoundEvent sound = SoundEvent.createVariableRangeEvent(Identifier.parse(layer.sound()));
        who.level().playSound(null, who.getX(), who.getY(), who.getZ(), sound, who instanceof net.minecraft.world.entity.player.Player ? SoundSource.PLAYERS : SoundSource.NEUTRAL, layer.volume(), layer.pitch());
    }
}
