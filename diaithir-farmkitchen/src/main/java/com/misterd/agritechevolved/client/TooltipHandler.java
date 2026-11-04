package com.misterd.agritechevolved.client;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.datamap.ATEDataMaps;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = AgritechEvolved.MODID, value = Dist.CLIENT)
public class TooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        var soilData = event.getItemStack().getItem().builtInRegistryHolder().getData(ATEDataMaps.SOIL_MODIFIERS);
        var fertData = event.getItemStack().getItem().builtInRegistryHolder().getData(ATEDataMaps.FERTILIZERS);

        if (soilData == null && fertData == null) return;

        if (soilData != null) {
            event.getToolTip().add(
                    Component.translatable("tooltip.agritechevolved.soil_type")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            event.getToolTip().add(
                    Component.translatable("tooltip.agritechevolved.soil_growth_modifier",
                                    String.format("%.2fx", soilData.growthModifier()))
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }

        if (fertData != null) {
            event.getToolTip().add(
                    Component.translatable("tooltip.agritechevolved.fertilizer_type")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            event.getToolTip().add(
                    Component.translatable("tooltip.agritechevolved.fertilizer_speed",
                                    String.format("%.2fx", fertData.speedMultiplier()))
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            event.getToolTip().add(
                    Component.translatable("tooltip.agritechevolved.fertilizer_yield",
                                    String.format("%.2fx", fertData.yieldMultiplier()))
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
    }
}