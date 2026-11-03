package net.diaithir.structurenavigation;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Module.ID)
public final class Module {
    public static final String ID = "diaithirstructurenavigation";

    public Module(IEventBus bus, ModContainer container) {
        net.diaithir.structurenavigation.structurecompass.Feature.init(bus, container);
    }
}
