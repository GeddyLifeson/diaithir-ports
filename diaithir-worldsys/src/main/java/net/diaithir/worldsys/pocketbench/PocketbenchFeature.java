package net.diaithir.worldsys.pocketbench;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers the Pocket Bench, a portable crafting grid item. */
public final class PocketbenchFeature {
    public static final String ID = "diaithirworldsys";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredItem<PocketBenchItem> POCKET_BENCH = ITEMS.registerItem("pocket_bench", PocketBenchItem::new, p -> p.stacksTo(1));

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) e.accept(POCKET_BENCH.get());
        });
    }

    private PocketbenchFeature() {}
}
