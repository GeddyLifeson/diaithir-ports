package net.diaithir.routing.foundry;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server config (diaithirrouting-server.toml). */
public final class Config {
    private Config() {}

    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue BARREL_STACKS = B
            .comment("Stacks of one item a Day Barrel holds.")
            .defineInRange("barrelStacks", 64, 1, 4096);

    public static final ModConfigSpec SPEC = B.build();
}
