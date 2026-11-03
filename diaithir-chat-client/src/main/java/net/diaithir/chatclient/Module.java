package net.diaithir.chatclient;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Module.ID)
public final class Module {
    public static final String ID = "diaithirchatclient";

    public Module(IEventBus bus, ModContainer container) {
        net.diaithir.chatclient.longerchathistory.Feature.init(bus, container);
    }
}
