package net.diaithir.upgrades;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The 36 module items: 3 themes x 4 kinds x 3 tiers. Ids never change:
 * circuit_speed_1, ogham_productivity_2, spice_efficiency_3 ...
 * Ages follow plans/PROGRESSION.md: ogham and spice III, circuit VI, tiers 2 and 3 VII.
 */
public final class Modules {
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DiaithirUpgrades.ID);
    public static final List<DeferredItem<ModuleItem>> ALL = new ArrayList<>();

    private Modules() {}

    static void register(IEventBus bus) {
        for (Theme theme : Theme.values()) {
            for (Kind kind : Kind.values()) {
                for (int tier = 1; tier <= 3; tier++) {
                    String id = ModuleIds.idOf(theme, kind, tier);
                    ModuleSpec spec = new ModuleSpec(id, kind, tier, theme, ModuleIds.ageOf(theme, tier));
                    ModuleRegistry.register(spec);
                    ALL.add(ITEMS.registerItem(id, p -> new ModuleItem(p.stacksTo(16), spec)));
                }
            }
        }
        ITEMS.register(bus);
    }
}
