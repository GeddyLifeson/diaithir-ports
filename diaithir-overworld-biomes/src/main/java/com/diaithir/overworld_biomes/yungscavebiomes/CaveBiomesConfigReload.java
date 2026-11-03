package com.diaithir.overworld_biomes.yungscavebiomes;

import net.neoforged.fml.event.config.ModConfigEvent;

final class CaveBiomesConfigReload {
    private CaveBiomesConfigReload() {}

    static void onReload(ModConfigEvent.Reloading e) {
        // config values are read lazily at biome-modifier time; nothing cached here.
    }
}
