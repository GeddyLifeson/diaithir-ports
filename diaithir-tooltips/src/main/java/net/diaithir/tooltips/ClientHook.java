package net.diaithir.tooltips;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Puts the item's native name, in the player's starting culture, under its normal name. Same line in every game language. */
@Mod(value = DiaithirTooltips.ID, dist = Dist.CLIENT)
public final class ClientHook {
    public static final Style STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true)
            .withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath(DiaithirTooltips.ID, "small")));

    public ClientHook() {
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, ClientHook::onTooltip);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> onTick());
    }

    private static String lastDimension;

    /** On entering a dimension: its name as a title, the culture's native name as the subtitle. */
    private static void onTick() {
        var mc = Minecraft.getInstance();
        if (mc.level == null) {
            lastDimension = null;
            return;
        }
        String id = mc.level.dimension().identifier().toString();
        if (lastDimension != null && !id.equals(lastDimension)) {
            String native_ = CultureNames.name("dimension", id);
            if (native_ != null) {
                String key = "dimension." + id.replace(':', '.');
                mc.gui.setTimes(10, 60, 20);
                mc.gui.setTitle(Component.literal(I18n.exists(key) ? I18n.get(key) : humanize(id)));
                mc.gui.setSubtitle(Component.literal(native_).withStyle(STYLE));
            }
        }
        lastDimension = id;
    }

    private static String humanize(String id) {
        var words = id.substring(id.indexOf(':') + 1).replace('/', ' ').replace('_', ' ').split(" ");
        var out = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                out.append(out.length() == 0 ? "" : " ").append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
            }
        }
        return out.toString();
    }

    private static void onTooltip(ItemTooltipEvent event) {
        var lines = event.getToolTip();
        if (lines.isEmpty()) {
            return;
        }
        String name = CultureNames.name("item", BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()).toString());
        if (name != null) {
            lines.add(1, Component.literal(name).withStyle(STYLE));
        }
    }
}
