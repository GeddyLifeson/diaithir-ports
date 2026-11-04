package net.diaithir.worldvisuals;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Module.ID)
public final class Module {
    public static final String ID = "diaithirworldvisuals";

    public Module(IEventBus bus, ModContainer container) {
        net.diaithir.worldvisuals.flickerfix.Feature.init(bus, container);
        net.diaithir.worldvisuals.lambdynamiclights.Feature.init(bus, container);
        new net.diaithir.worldvisuals.ctm.DiaithirCtm(bus, container);
        net.diaithir.worldvisuals.soundphysics.Feature.init(bus, container);
    }
}
