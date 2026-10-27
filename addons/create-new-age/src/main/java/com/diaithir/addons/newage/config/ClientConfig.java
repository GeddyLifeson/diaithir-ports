package com.diaithir.addons.newage.config;


import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    public final ModConfigSpec.ConfigValue<Integer> wireSectionsPerMeter;
    public final ModConfigSpec.ConfigValue<Double> wireThickness;

    public ClientConfig(ModConfigSpec.Builder builder) {
        wireSectionsPerMeter = builder
                .comment(
                        "Choose how many wire sections are rendered in one meter (block).",
                        "Decreasing this value can theoretically improve performance"
                ).defineInRange("wireSectionsPerMeter", 10, 1, Integer.MAX_VALUE);

        wireThickness = builder
                .comment("...wire thickness...")
                .defineInRange("wireThickness", 0.03, 0, Double.MAX_VALUE);
    }
}
