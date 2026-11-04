package net.diaithir.hudoverlays;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Module.ID)
public final class Module {
    public static final String ID = "diaithirhudoverlays";

    public Module(IEventBus bus, ModContainer container) {
        net.diaithir.hudoverlays.betteradvancements.Feature.init(bus, container);
    }
}
