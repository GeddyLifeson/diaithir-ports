package net.diaithir.worldgenchunkopts;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Module.ID)
public final class Module {
    public static final String ID = "diaithirworldgenchunkopts";

    public Module(IEventBus bus, ModContainer container) {
        net.diaithir.worldgenchunkopts.ksyxis.Feature.init(bus, container);
    }
}
