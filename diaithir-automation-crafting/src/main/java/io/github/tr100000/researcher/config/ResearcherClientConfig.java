package io.github.tr100000.researcher.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ResearcherClientConfig {
    public final ModConfigSpec spec;
    public final ModConfigSpec.BooleanValue researchHud;
    public final ModConfigSpec.BooleanValue pinAvailableResearches;
    public final ModConfigSpec.EnumValue<ResearchTreeMode> researchTreeMode;
    public final ModConfigSpec.BooleanValue discoveryResearchMode;
    public final ModConfigSpec.IntValue highlightColor;
    public final ModConfigSpec.DoubleValue researchTreeScrollSensitivity;
    public final ModConfigSpec.IntValue researchScreenSidebarWidth;
    public final ModConfigSpec.IntValue researchScreenInfoViewHeight;
    public final ModConfigSpec.EnumValue<ResearchScreenAllowResizeMode> researchScreenAllowResize;
    public final ModConfigSpec.BooleanValue researchScreenAllowZoom;

    public ResearcherClientConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("hud");
        researchHud = builder.define("researchHud", true);
        pinAvailableResearches = builder.define("pinAvailableResearches", false);
        builder.pop();
        builder.push("research_screen");
        researchTreeMode = builder.defineEnum("researchTreeMode", ResearchTreeMode.ALL_RELATED);
        discoveryResearchMode = builder.define("discoveryResearchMode", false);
        highlightColor = builder.comment("ARGB colour used to highlight connections.")
                .defineInRange("highlightColor", 0xFFEBC000, Integer.MIN_VALUE, Integer.MAX_VALUE);
        researchTreeScrollSensitivity = builder.defineInRange("researchTreeScrollSensitivity", 2.5, 0.5, 30.0);
        researchScreenSidebarWidth = builder.defineInRange("researchScreenSidebarWidth", 230, 10, Integer.MAX_VALUE);
        researchScreenInfoViewHeight = builder.defineInRange("researchScreenInfoViewHeight", 140, 10, Integer.MAX_VALUE);
        researchScreenAllowResize = builder.defineEnum("researchScreenAllowResize", ResearchScreenAllowResizeMode.ALWAYS);
        researchScreenAllowZoom = builder.define("researchScreenAllowZoom", true);
        builder.pop();
        spec = builder.build();
    }

    public enum ResearchTreeMode {
        DIRECTLY_RELATED,
        ALL_RELATED,
        ALL
    }

    public enum ResearchScreenAllowResizeMode {
        ALWAYS,
        WHEN_HOLDING_SHIFT,
        NEVER
    }
}
