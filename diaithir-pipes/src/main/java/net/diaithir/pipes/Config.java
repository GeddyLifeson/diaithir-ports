package net.diaithir.pipes;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server config: throughput and limits. */
public final class Config {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue ITEMS_PER_RUN = B.comment("Items a basic pipe moves per run; improved x4, advanced x16.").defineInRange("itemsPerRun", 4, 1, 4096);
    public static final ModConfigSpec.IntValue FLUID_PER_RUN = B.comment("Millibuckets a basic pipe moves per run; improved x4, advanced x16.").defineInRange("fluidPerRun", 250, 1, 1_000_000);
    public static final ModConfigSpec.IntValue ENERGY_PER_RUN = B.comment("Energy a basic pipe moves per run; improved x4, advanced x16.").defineInRange("energyPerRun", 500, 1, 10_000_000);
    public static final ModConfigSpec.IntValue TICKS_PER_RUN = B.comment("Ticks between runs of an extract face.").defineInRange("ticksPerRun", 10, 1, 200);
    public static final ModConfigSpec.IntValue MAX_NETWORK_PIPES = B.comment("Most pipes searched when finding targets.").defineInRange("maxNetworkPipes", 512, 8, 65536);
    public static final ModConfigSpec.IntValue CONNECTOR_DEPTH = B.comment("Most blocks the connector tool can span between two pipe ends.").defineInRange("connectorDepth", 8, 1, 8);

    public static final ModConfigSpec SPEC = B.build();

    private Config() {
    }
}
