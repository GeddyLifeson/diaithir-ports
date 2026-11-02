package net.diaithir.hostile.celestials;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Diaithir Hostile Encounters. First part: lunar events (blood, blue, harvest and super moons), ported from Enhanced Celestials
 * (LGPL-3.0, Corgitaco, MC 1.21.1) onto plain NeoForge events. Horde nights and invasion tuning join the module next.
 */
@Mod(DiaithirHostile.ID)
public final class DiaithirHostile {
    public static final String ID = "diaithirhostile";

    public DiaithirHostile(net.neoforged.bus.api.IEventBus bus) {
        net.diaithir.hostile.encounters.Consolidated.load();
        net.diaithir.hostile.encounters.ConsolidatedItems.register(bus);
        NeoForge.EVENT_BUS.register(LunarEvents.class);
        NeoForge.EVENT_BUS.register(SelfTest.class);
        NeoForge.EVENT_BUS.register(WorldEventsBridge.class);
    }
}
