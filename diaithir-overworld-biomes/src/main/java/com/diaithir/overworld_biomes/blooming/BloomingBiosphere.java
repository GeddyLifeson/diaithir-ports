package com.diaithir.overworld_biomes.blooming;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * Placeholder replacement: the original mod constructor registered the common config
 * a second time under the same mod id, producing a config file conflict
 * (diaithir_overworld_biomes-common.toml already registered). The config is registered
 * by the primary mod class only, so this class must not register it again.
 */
public class BloomingBiosphere {
    public static final String MOD_ID = "diaithir_overworld_biomes";

    public BloomingBiosphere(IEventBus modBus) {
        // Intentionally no config registration here; handled by the primary mod class.
    }
}
