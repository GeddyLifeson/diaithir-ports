package net.diaithir.clientviewinput;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Module.ID)
public final class Module {
    public static final String ID = "diaithirclientviewinput";

    public Module(IEventBus bus, ModContainer container) {
        net.diaithir.clientviewinput.firstpersonmodel.Feature.init(bus, container);
    }
}
