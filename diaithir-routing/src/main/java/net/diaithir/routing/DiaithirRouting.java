package net.diaithir.routing;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/** Routers, modules, laser nodes, the shared filter item and the hopper family. Clean-room: written from a behaviour brief only. */
@Mod(DiaithirRouting.ID)
public final class DiaithirRouting {
    public static final String ID = "diaithirrouting";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ID);

    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 8.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties node() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F, 6.0F).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops();
    }

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FilterData>> FILTER_DATA = COMPONENTS.registerComponentType("filter",
            b -> b.persistent(FilterData.CODEC).networkSynchronized(FilterData.STREAM_CODEC));

    public static final DeferredBlock<RouterBlock> ROUTER = BLOCKS.registerBlock("router", RouterBlock::new, DiaithirRouting::metal);
    public static final DeferredBlock<LaserNodeBlock> LASER_NODE = BLOCKS.registerBlock("laser_node", LaserNodeBlock::new, DiaithirRouting::node);
    public static final DeferredBlock<ModHopperBlock> COPPER_FILTER_HOPPER = BLOCKS.registerBlock("copper_filter_hopper",
            p -> new ModHopperBlock(p, HopperKind.COPPER_FILTER), DiaithirRouting::metal);
    public static final DeferredBlock<ModHopperBlock> FAST_HOPPER = BLOCKS.registerBlock("fast_hopper",
            p -> new ModHopperBlock(p, HopperKind.FAST), DiaithirRouting::metal);
    public static final DeferredBlock<ModHopperBlock> FILTER_HOPPER = BLOCKS.registerBlock("filter_hopper",
            p -> new ModHopperBlock(p, HopperKind.FILTER), DiaithirRouting::metal);
    public static final DeferredBlock<ModHopperBlock> ITEM_COLLECTOR = BLOCKS.registerBlock("item_collector",
            p -> new ModHopperBlock(p, HopperKind.COLLECTOR), DiaithirRouting::metal);

    public static final DeferredItem<BlockItem> ROUTER_ITEM = ITEMS.registerSimpleBlockItem(ROUTER);
    public static final DeferredItem<BlockItem> LASER_NODE_ITEM = ITEMS.registerSimpleBlockItem(LASER_NODE);
    public static final DeferredItem<BlockItem> COPPER_FILTER_HOPPER_ITEM = ITEMS.registerSimpleBlockItem(COPPER_FILTER_HOPPER);
    public static final DeferredItem<BlockItem> FAST_HOPPER_ITEM = ITEMS.registerSimpleBlockItem(FAST_HOPPER);
    public static final DeferredItem<BlockItem> FILTER_HOPPER_ITEM = ITEMS.registerSimpleBlockItem(FILTER_HOPPER);
    public static final DeferredItem<BlockItem> ITEM_COLLECTOR_ITEM = ITEMS.registerSimpleBlockItem(ITEM_COLLECTOR);

    public static final DeferredItem<FilterItem> FILTER = ITEMS.registerItem("filter", FilterItem::new, p -> p.stacksTo(1));
    public static final DeferredItem<RouterLinkItem> ROUTER_LINK = ITEMS.registerItem("router_link", RouterLinkItem::new, p -> p.stacksTo(1));
    public static final DeferredItem<Item> RANGE_CARD = ITEMS.registerSimpleItem("range_card");

    public static final List<DeferredItem<ModuleItem>> MODULES = new ArrayList<>();

    static {
        for (ModuleKind k : ModuleKind.values()) {
            MODULES.add(ITEMS.registerItem(k.id() + "_module", p -> new ModuleItem(p, k), p -> p.stacksTo(1)));
        }
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RouterBlockEntity>> ROUTER_BE = BLOCK_ENTITIES.register("router",
            () -> new BlockEntityType<>(RouterBlockEntity::new, ROUTER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LaserNodeBlockEntity>> LASER_BE = BLOCK_ENTITIES.register("laser_node",
            () -> new BlockEntityType<>(LaserNodeBlockEntity::new, LASER_NODE.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ModHopperBlockEntity>> HOPPER_BE = BLOCK_ENTITIES.register("hopper",
            () -> new BlockEntityType<>(ModHopperBlockEntity::new, COPPER_FILTER_HOPPER.get(), FAST_HOPPER.get(), FILTER_HOPPER.get(), ITEM_COLLECTOR.get()));

    public static final DeferredHolder<MenuType<?>, MenuType<RouterMenu>> ROUTER_MENU = MENUS.register("router",
            () -> IMenuTypeExtension.create(RouterMenu::client));

    public DiaithirRouting(IEventBus bus, ModContainer container) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        COMPONENTS.register(bus);
        MENUS.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(RegisterMenuScreensEvent.class, e -> e.register(ROUTER_MENU.get(), RouterScreen::new));
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                for (Item i : List.of(ROUTER_ITEM.get(), LASER_NODE_ITEM.get(), COPPER_FILTER_HOPPER_ITEM.get(), FAST_HOPPER_ITEM.get(),
                        FILTER_HOPPER_ITEM.get(), ITEM_COLLECTOR_ITEM.get(), FILTER.get(), ROUTER_LINK.get(), RANGE_CARD.get())) e.accept(i);
                for (DeferredItem<ModuleItem> m : MODULES) e.accept(m.get());
            }
        });
    }
}
