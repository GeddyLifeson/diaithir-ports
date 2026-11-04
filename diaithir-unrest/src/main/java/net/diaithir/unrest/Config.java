package net.diaithir.unrest;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Every number is server config data (plans/FACTORIO_TECH.md section 2f). */
public final class Config {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    public static final ModConfigSpec.IntValue STEP_TICKS = B.comment("Ticks between spread and decay steps").defineInRange("stepTicks", 200, 20, 24000);
    public static final ModConfigSpec.DoubleValue SPREAD = B.comment("Fraction of a chunk's Unrest given to its four neighbours each step").defineInRange("spread", 0.10, 0.0, 0.5);
    public static final ModConfigSpec.DoubleValue DECAY = B.comment("Fraction of all Unrest lost each step").defineInRange("decay", 0.02, 0.0, 1.0);
    public static final ModConfigSpec.IntValue SCAN_TICKS = B.comment("Ticks between scans of noisy machines near players").defineInRange("scanTicks", 100, 20, 2400);
    public static final ModConfigSpec.IntValue SCAN_RADIUS = B.comment("Chunk radius around each player scanned for noisy machines").defineInRange("scanRadiusChunks", 4, 1, 12);
    public static final ModConfigSpec.DoubleValue LOW = B.comment("Unrest per scan from a block in the noisy_low tag").defineInRange("lowWeight", 1.0, 0.0, 1000.0);
    public static final ModConfigSpec.DoubleValue MID = B.comment("Unrest per scan from a block in the noisy_mid tag").defineInRange("midWeight", 3.0, 0.0, 1000.0);
    public static final ModConfigSpec.DoubleValue HIGH = B.comment("Unrest per scan from a block in the noisy_high tag").defineInRange("highWeight", 10.0, 0.0, 1000.0);
    public static final ModConfigSpec.DoubleValue QUIET = B.comment("Unrest removed per scan by a block in the quiet tag (sound-proofed housings, filters)").defineInRange("quietWeight", 2.0, 0.0, 1000.0);
    public static final ModConfigSpec.DoubleValue EXPLOSION = B.comment("Unrest per unit of explosion radius").defineInRange("explosionPerRadius", 5.0, 0.0, 1000.0);
    public static final ModConfigSpec.IntValue NEST_RADIUS = B.comment("Chunk radius a nest absorbs Unrest from").defineInRange("nestRadiusChunks", 3, 1, 8);
    public static final ModConfigSpec.DoubleValue NEST_RATE = B.comment("Fraction of the Unrest in range a nest absorbs each step").defineInRange("nestAbsorb", 0.25, 0.0, 1.0);
    public static final ModConfigSpec.DoubleValue WAVE_COST = B.comment("Absorbed Unrest a nest spends to send one attack wave").defineInRange("waveCost", 200.0, 1.0, 100000.0);
    public static final ModConfigSpec.IntValue MAX_TIER = B.comment("Highest nest tier allowed (raise by Age)").defineInRange("maxTier", 2, 0, 4);
    public static final ModConfigSpec SPEC = B.build();

    private Config() {}
}
