package com.diaithir.overworld_biomes.natures;

import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Map;

/** Legacy-id migration table and the global event hook point. */
public final class NaturesCompat {
    private static final String OLD_NS = "natures_spirit";
    /** old path -> new path, extend as the biome list is finalised. */
    private static final Map<String, String> ALIASES = Map.of(
            "heath_turf", "heath_turf",
            "steppe_loam", "steppe_loam",
            "riveted_basalt", "riveted_basalt",
            "chaparral", "heath_scrub",
            "cypress_wetlands", "cypress_marsh",
            "redwood_forest", "conifer_highland");

    private NaturesCompat() {}

    public static void onMissingMappings(Object event) {
    }

    /** Global Events hook point: other modules subscribe to NaturesGlobalEvent on the NeoForge bus. */
    public static void fireGlobalEvent(String id) {
        NeoForge.EVENT_BUS.post(new NaturesGlobalEvent(id));
    }

    public static final class NaturesGlobalEvent extends Event {
        private final String eventId;
        public NaturesGlobalEvent(String eventId) { this.eventId = eventId; }
        public String eventId() { return eventId; }
    }
}
