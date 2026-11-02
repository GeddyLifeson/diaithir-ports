package net.diaithir.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Module.ID)
public final class Module {
    public static final String ID = "diaithirclient";

    public Module(IEventBus bus, ModContainer container) {
        net.diaithir.client.diaithirearlywindow.Feature.init(bus, container);
    }
}
