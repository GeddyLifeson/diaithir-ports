package net.diaithir.tooltips;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Module.ID)
public final class Module {
    public static final String ID = "diaithirtooltipfx";

    public Module(IEventBus bus, ModContainer container) {
        net.diaithir.tooltips.foodeffecttooltips.Feature.init(bus, container);
        net.diaithir.tooltips.enchantmentdescriptions.Feature.init(bus, container);
        net.diaithir.tooltips.durabilitytooltip.Feature.init(bus, container);
    }
}
