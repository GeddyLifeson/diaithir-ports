package com.diaithir.nether.nether;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

/** Cross-mod hook points: global event firing and optional-dependency guards. */
public final class NetherIntegration {
    public static final int AGE = 5;
    public static final int ENERGY_QUANTUM_J = 100;

    private NetherIntegration() {}

    /** Posted when a player first enters one of our biomes; other modules (Global Events, Codex) subscribe. */
    public static class BiomeEnteredEvent extends Event {
        private final String biomeId;
        private final java.util.UUID player;

        public BiomeEnteredEvent(String biomeId, java.util.UUID player) {
            this.biomeId = biomeId;
            this.player = player;
        }

        public String biomeId() { return biomeId; }
        public java.util.UUID player() { return player; }
    }

    public static void fireBiomeEntered(String biomeId, java.util.UUID player) {
        NeoForge.EVENT_BUS.post(new BiomeEnteredEvent(biomeId, player));
    }

    public static boolean hasCodex() { return ModList.get().isLoaded("diaithir_codex"); }
    public static boolean hasTellus() { return ModList.get().isLoaded("tellus"); }
    public static boolean hasTerralith() { return ModList.get().isLoaded("terralith"); }
}
