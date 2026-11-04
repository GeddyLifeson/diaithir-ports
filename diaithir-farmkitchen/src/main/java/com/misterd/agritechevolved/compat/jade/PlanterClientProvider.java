package com.misterd.agritechevolved.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.text.NumberFormat;
import java.util.Locale;

public enum PlanterClientProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return PlanterProvider.UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.getBooleanOr("hasCrop", false)) return;

        String cropName = data.getStringOr("cropName", "");
        int currentStage = data.getIntOr("currentStage", 0);
        int maxStage = data.getIntOr("maxStage", 0);
        float progressPercent = data.getFloatOr("progressPercent", 0f);
        String soilName = data.getStringOr("soilName", "");
        float growthModifier = data.getFloatOr("growthModifier", 1f);

        if (progressPercent >= 100.0F) {
            tooltip.add(Component.translatable("jade.agritechevolved.crop_ready", cropName)
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        } else {
            tooltip.add(Component.translatable("jade.agritechevolved.crop_progress",
                            cropName, currentStage, maxStage, Math.round(progressPercent))
                    .withStyle(ChatFormatting.DARK_GREEN));
        }

        tooltip.add(Component.translatable("jade.agritechevolved.soil_info",
                        soilName, String.format("%.2fx", growthModifier))
                .withStyle(ChatFormatting.GRAY));

        if (data.getBooleanOr("hasFertilizer", false)) {
            float fertSpeed = data.getFloatOr("fertilizerSpeedModifier", 1f);
            float fertYield = data.getFloatOr("fertilizerYieldModifier", 1f);
            tooltip.add(Component.translatable("jade.agritechevolved.fertilizer_info",
                            data.getStringOr("fertilizerName", ""),
                            String.format("%.2fx", fertSpeed),
                            String.format("%.2fx", fertYield))
                    .withStyle(ChatFormatting.YELLOW));
        }

        if (data.getBooleanOr("isCloched", false)) {
            float clocheSpeed = data.getFloatOr("clocheSpeedModifier", 1f);
            float clocheYield = data.getFloatOr("clocheYieldModifier", 1f);
            tooltip.add(Component.translatable("jade.agritechevolved.cloche_installed",
                            String.format("%.2fx", clocheSpeed),
                            String.format("%.2fx", clocheYield))
                    .withStyle(ChatFormatting.AQUA));
        }

        if (data.getBooleanOr("isAdvanced", false)) {
            int energy = data.getIntOr("energyStored", 0);
            int maxEnergy = data.getIntOr("maxEnergy", 0);
            NumberFormat fmt = NumberFormat.getInstance(Locale.US);
            tooltip.add(Component.translatable("jade.agritechevolved.energy_info",
                            fmt.format(energy), fmt.format(maxEnergy))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));

            if (data.getBooleanOr("hasModules", false)) {
                float moduleSpeed = data.getFloatOr("moduleSpeedModifier", 1f);
                float moduleYield = data.getFloatOr("moduleYieldModifier", 1f);
                tooltip.add(Component.translatable("jade.agritechevolved.module_info",
                                String.format("%.2fx", moduleSpeed),
                                String.format("%.2fx", moduleYield))
                        .withStyle(ChatFormatting.GOLD));
            }
        }
    }
}