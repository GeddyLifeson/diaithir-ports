package net.diaithir.filters;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Filter item, filter card and the rule editor menu. Clean-room original. */
@Mod(DiaithirFilters.ID)
public final class DiaithirFilters {
    public static final String ID = "diaithirfilters";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FilterData>> FILTER_DATA = COMPONENTS.registerComponentType("filter",
            b -> b.persistent(FilterData.CODEC).networkSynchronized(FilterData.STREAM_CODEC));

    public static final DeferredItem<FilterItem> FILTER = ITEMS.registerItem("filter", p -> new FilterItem(p.stacksTo(1), false));
    public static final DeferredItem<FilterItem> FILTER_CARD = ITEMS.registerItem("filter_card", p -> new FilterItem(p.stacksTo(16), true));

    public static final DeferredHolder<MenuType<?>, MenuType<FilterMenu>> FILTER_MENU = MENUS.register("filter",
            () -> IMenuTypeExtension.create(FilterMenu::new));

    public DiaithirFilters(IEventBus bus, ModContainer container) {
        ITEMS.register(bus);
        COMPONENTS.register(bus);
        MENUS.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                e.accept(FILTER.get());
                e.accept(FILTER_CARD.get());
            }
        });
    }
}
