package net.diaithir.furnishings;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** English is the display name; the Diaithiris name sits under it as a grey italic line, from the lang key item.&lt;id&gt;.native. */
@EventBusSubscriber(modid = DiaithirFurnishings.ID, value = Dist.CLIENT)
public final class NativeNames {
    private NativeNames() {
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        Identifier id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (!DiaithirFurnishings.ID.equals(id.getNamespace())) {
            return;
        }
        String key = "item." + id.getNamespace() + "." + id.getPath() + ".native";
        if (I18n.exists(key) && event.getToolTip().size() >= 1) {
            event.getToolTip().add(1, Component.translatable(key).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}
