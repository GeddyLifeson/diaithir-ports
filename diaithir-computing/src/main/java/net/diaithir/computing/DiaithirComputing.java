package net.diaithir.computing;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(DiaithirComputing.ID)
public final class DiaithirComputing {
    public static final String ID = "diaithircomputing";

    public DiaithirComputing(IEventBus bus) {
        Registry.register(bus);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> Commands.register(e.getDispatcher()));
    }
}
