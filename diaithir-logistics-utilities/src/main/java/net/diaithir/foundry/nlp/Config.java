// SPDX-License-Identifier: LGPL-3.0-only
// Design derived from Not Logistics Pipes (c) Contributors to Not Logistics Pipes <https://nlp.rs485.network>, reimplemented in Java for NeoForge 26.1.2 (Diaithir).
package net.diaithir.foundry.nlp;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server config (diaithirlogistics-server.toml). */
public final class Config {
    private Config() {}

    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_PIPES = B
            .comment("Most pipes one route search explores. Keeps a runaway pipe run from stalling the server.")
            .defineInRange("maxPipesPerSearch", 800, 2, 20000);
    public static final ModConfigSpec.IntValue EXTRACT_INTERVAL = B
            .comment("Ticks between runs of an extractor pipe.")
            .defineInRange("extractIntervalTicks", 20, 1, 1200);
    public static final ModConfigSpec.IntValue EXTRACT_AMOUNT = B
            .comment("Most items one extractor pipe takes per run.")
            .defineInRange("extractAmount", 8, 1, 4096);
    public static final ModConfigSpec.IntValue MAX_TRIES = B
            .comment("Delivery attempts (one per hop time) before a stack that cannot be inserted is rerouted, then dropped.")
            .defineInRange("maxDeliveryTries", 10, 1, 1000);

    public static final ModConfigSpec SPEC = B.build();
}
