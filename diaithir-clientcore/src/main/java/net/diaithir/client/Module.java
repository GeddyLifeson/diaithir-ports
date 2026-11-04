package net.diaithir.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Module.ID)
public final class Module {
    public static final String ID = "diaithirclientcore";

    public Module(IEventBus bus, ModContainer container) {
        net.diaithir.client.gpl.melodies.Feature.init(bus, container);
        net.diaithir.client.disableaccessibility.Feature.init(bus, container);
        net.diaithir.client.updater.Feature.init(bus, container);
        net.diaithir.audio.soundmuffler.Feature.init(bus, container);
        net.diaithir.audio.soundphysics.Feature.init(bus, container);
        net.diaithir.audio.surroundings.Feature.init(bus, container);
        net.diaithir.client.diaithirearlywindow.Feature.init(bus, container);
    }
}
