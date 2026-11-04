package com.diaithir.overworld_biomes.blooming;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;

public final class BloomingEvents {
    private BloomingEvents() {}

    /** Global Events hook point: other modules may listen to adjust or veto the blooming overhaul. */
    public static final class WorldgenOverhaulEvent extends Event {
        public static final String ID = "diaithir_overworld_biomes:blooming_overhaul";
    }

    public static void fireWorldgenHook(IEventBus bus) {
        bus.post(new WorldgenOverhaulEvent());
    }
}
