package net.diaithir.admincore;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Module.ID)
public final class Module {
    public static final String ID = "diaithiradmincore";

    public Module(IEventBus bus, ModContainer container) {
        net.diaithir.admincore.simplebackups.Feature.init(bus, container);
    }
}
