package net.diaithir.crashtools;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Module.ID)
public final class Module {
    public static final String ID = "diaithircrashtools";

    public Module(IEventBus bus, ModContainer container) {
        net.diaithir.worldgenchunkopts.ksyxis.Feature.init(bus, container);
        net.diaithir.networkcompat.packetfixer.Feature.init(bus, container);
        net.diaithir.crashtools.crashassistant.Feature.init(bus, container);
        net.diaithir.networkcompat.handshake.PackHandshake.init(bus);
    }
}
