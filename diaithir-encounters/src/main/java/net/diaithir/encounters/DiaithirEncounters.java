package net.diaithir.encounters;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** Clean-room caravans and horde-night content (Better Caravans, Pillager Caravans, Undead Nights). */
@Mod(DiaithirEncounters.ID)
public final class DiaithirEncounters {
    public static final String ID = "diaithirencounters";

    public DiaithirEncounters(IEventBus bus) {
        Consolidated.load();
        ConsolidatedItems.register(bus);
        ConsolidatedBehaviour.register();
    }
}
