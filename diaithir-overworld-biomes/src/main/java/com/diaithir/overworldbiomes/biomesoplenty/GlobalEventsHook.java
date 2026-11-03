package com.diaithir.overworldbiomes.biomesoplenty;

import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public final class GlobalEventsHook {
    public static final class BiomeModuleEvent extends Event {
        public final String id;
        public final String payload;

        public BiomeModuleEvent(String id, String payload) {
            this.id = id;
            this.payload = payload;
        }
    }

    private GlobalEventsHook() {}

    public static void fire(String id, String payload) {
        NeoForge.EVENT_BUS.post(new BiomeModuleEvent(id, payload));
    }

    public static void onBiomesLoaded(ServerStartedEvent e) {
        fire("biomes_ready", BiomesOPlentyModule.MOD_ID);
    }
}
