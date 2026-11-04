package net.diaithir.inventoryui;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Module.ID)
public final class Module {
    public static final String ID = "diaithirinventoryui";

    public Module(IEventBus bus, ModContainer container) {
        net.diaithir.inventoryui.inventoryessentials.Feature.init(bus, container);
        net.diaithir.inventoryui.trashslot.Feature.init(bus, container);
    }
}
