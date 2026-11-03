package net.diaithir.networkcompat;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Module.ID)
public final class Module {
    public static final String ID = "diaithirnetworkcompat";

    public Module(IEventBus bus, ModContainer container) {
        net.diaithir.networkcompat.packetfixer.Feature.init(bus, container);
    }
}
