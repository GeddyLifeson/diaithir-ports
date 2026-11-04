package com.diaithir.overworld_biomes.yungscavebiomes;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

public final class CaveBiomesEvents {
    private CaveBiomesEvents() {}

    public static boolean isLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    /** Global Events hook point fired when a cave biome is entered. */
    public static class CaveBiomeEnteredEvent extends Event {
        public final String biome;
        public final String player;

        public CaveBiomeEnteredEvent(String biome, String player) {
            this.biome = biome;
            this.player = player;
        }
    }

    public static void fireEntered(String biome, String player) {
        NeoForge.EVENT_BUS.post(new CaveBiomeEnteredEvent(biome, player));
    }

    public static void register(IEventBus bus) {
        bus.addListener(CaveBiomesConfigReload::onReload);
    }
}
