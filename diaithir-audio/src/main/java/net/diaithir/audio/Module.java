package net.diaithir.audio;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Module.ID)
public final class Module {
    public static final String ID = "diaithiraudio";

    public Module(IEventBus bus, ModContainer container) {
        net.diaithir.audio.surroundings.Feature.init(bus, container);
        net.diaithir.audio.soundphysics.Feature.init(bus, container);
        net.diaithir.audio.soundmuffler.Feature.init(bus, container);
    }
}
