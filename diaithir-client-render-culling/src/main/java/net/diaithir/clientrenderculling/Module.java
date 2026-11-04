package net.diaithir.clientrenderculling;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Module.ID)
public final class Module {
    public static final String ID = "diaithirclientrenderculling";

    public Module(IEventBus bus, ModContainer container) {
        net.diaithir.clientrenderculling.badoptimizations.Feature.init(bus, container);
        net.diaithir.clientrenderculling.immediatelyfast.Feature.init(bus, container);
        net.diaithir.clientrenderculling.entityculling.Feature.init(bus, container);
    }
}
