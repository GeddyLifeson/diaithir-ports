package net.diaithir.entityanimation;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Module.ID)
public final class Module {
    public static final String ID = "diaithirentityanimation";

    public Module(IEventBus bus, ModContainer container) {
        net.diaithir.entityanimation.emfetf.Feature.init(bus, container);
        net.diaithir.entityanimation.skinlayers3d.Feature.init(bus, container);
        net.diaithir.entityanimation.notenoughanimations.Feature.init(bus, container);
    }
}
