package net.diaithir.filters;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue MAX_RULES;
    public static final ModConfigSpec.IntValue MAX_DEPTH;
    public static final ModConfigSpec.IntValue MATCHES_PER_TICK;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        MAX_RULES = b.comment("Most rules one filter may hold in total").defineInRange("maxRules", 32, 1, 256);
        MAX_DEPTH = b.comment("Deepest AND/OR/NOT nesting allowed").defineInRange("maxDepth", 4, 1, 16);
        MATCHES_PER_TICK = b.comment("Throughput: most candidate stacks a caller should test per tick (advisory, see Filters.budget)")
                .defineInRange("matchesPerTick", 256, 1, 100000);
        SPEC = b.build();
    }

    private Config() {
    }
}
