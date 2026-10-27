package net.diaithir.tooltips;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Puts the item's native name, in the player's starting culture, under its normal name. Same line in every game language. */
@Mod(value = DiaithirTooltips.ID, dist = Dist.CLIENT)
public final class ClientHook {
    private static final Style STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true)
            .withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath(DiaithirTooltips.ID, "small")));

    public ClientHook() {
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, ClientHook::onTooltip);
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
