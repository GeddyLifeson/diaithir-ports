package io.github.tr100000.researcher.config;

import io.github.tr100000.researcher.ResearchSyncMode;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ResearcherServerConfig {
    public final ModConfigSpec spec;
    public final ModConfigSpec.DoubleValue researchCostMultiplier;
    public final ModConfigSpec.EnumValue<ResearchSyncMode> researchSyncMode;

    public ResearcherServerConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        researchCostMultiplier = builder.comment("Multiplies every research cost. Needs a data reload to apply.")
                .defineInRange("researchCostMultiplier", 1.0, 0.1, 10000.0);
        researchSyncMode = builder.comment("Who shares research progress with whom. Needs a relog to apply.")
                .defineEnum("researchSyncMode", ResearchSyncMode.NONE);
        spec = builder.build();
    }
}
