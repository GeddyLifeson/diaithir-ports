package net.diaithir.freight;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * The freight layer: pallets, barrels, chests and crates (real blocks that hold items), walkable shipping containers (20 and 40 ft, reefer, tank) built from
 * thin plates, and docks that hand a container's contents to any storage network. Lives in the diaithir-carts jar. See FREIGHT.md.
 */
public final class Freight {
    public static final String ID = "diaithircarts";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ID);

    /** What a container item carries: the loads standing in it (each with its items), so a lifted container keeps its cargo. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BayData>> BAY = COMPONENTS.registerComponentType("bay",
            b -> b.persistent(BayData.CODEC));

    private static BlockBehaviour.Properties wood() { return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5F, 3F).sound(SoundType.WOOD); }

    private static BlockBehaviour.Properties steel() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4F, 12F).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops();
    }

    /** One block and item per loose load type, by load id. */
    public static final Map<String, DeferredBlock<LoadBlock>> LOADS = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<BlockItem>> LOAD_ITEMS = new LinkedHashMap<>();
    /** One item per container type, by load id. */
    public static final Map<String, DeferredItem<ContainerItem>> CONTAINER_ITEMS = new LinkedHashMap<>();

    static {
        for (LoadType t : LoadType.ALL) {
            if (t.looseItems()) {
                DeferredBlock<LoadBlock> b = BLOCKS.registerBlock("freight_" + t.id(), p -> new LoadBlock(p, t), Freight::wood);
                LOADS.put(t.id(), b);
                LOAD_ITEMS.put(t.id(), ITEMS.registerSimpleBlockItem(b));
            } else {
                CONTAINER_ITEMS.put(t.id(), ITEMS.registerItem(t.id(), p -> new ContainerItem(p, t), (UnaryOperator<Item.Properties>) p -> p.stacksTo(1)));
            }
        }
    }

    public static final DeferredBlock<ContainerWall> WALL = BLOCKS.registerBlock("container_wall", ContainerWall::new, Freight::steel);
    public static final DeferredBlock<ContainerCore> CORE = BLOCKS.registerBlock("container_core", ContainerCore::new, Freight::steel);
    public static final DeferredBlock<ContainerDoor> DOOR = BLOCKS.registerBlock("container_door", ContainerDoor::new, Freight::steel);
    public static final DeferredBlock<Block> REEFER_UNIT = BLOCKS.registerBlock("reefer_unit", Block::new, Freight::steel);
    public static final DeferredBlock<Block> TANK_VESSEL = BLOCKS.registerBlock("tank_vessel", Block::new, Freight::steel);
    public static final DeferredBlock<LoadingDock> LOADING_DOCK = BLOCKS.registerBlock("loading_dock", p -> new LoadingDock(p, false), Freight::steel);
    public static final DeferredBlock<LoadingDock> RIFT_DOCK = BLOCKS.registerBlock("rift_dock", p -> new LoadingDock(p, true), Freight::steel);
    public static final DeferredItem<BlockItem> LOADING_DOCK_ITEM = ITEMS.registerSimpleBlockItem(LOADING_DOCK);
    public static final DeferredItem<BlockItem> RIFT_DOCK_ITEM = ITEMS.registerSimpleBlockItem(RIFT_DOCK);
    public static final DeferredItem<Item> CARGO_HOOK = ITEMS.registerItem("cargo_hook", CargoHook::new, (UnaryOperator<Item.Properties>) p -> p.stacksTo(1));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LoadBlockEntity>> LOAD_BE = BLOCK_ENTITIES.register("freight_load",
            () -> new BlockEntityType<>(LoadBlockEntity::new, LOADS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ContainerCoreEntity>> CORE_BE = BLOCK_ENTITIES.register("container_core",
            () -> new BlockEntityType<>(ContainerCoreEntity::new, CORE.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DockBlockEntity>> DOCK_BE = BLOCK_ENTITIES.register("dock",
            () -> new BlockEntityType<>(DockBlockEntity::new, LOADING_DOCK.get(), RIFT_DOCK.get()));

    private Freight() {}

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        COMPONENTS.register(bus);
        bus.addListener(RegisterCapabilitiesEvent.class, e -> {
            e.registerBlockEntity(Capabilities.Item.BLOCK, DOCK_BE.get(), (be, side) -> be.items());
            e.registerBlockEntity(Capabilities.Fluid.BLOCK, DOCK_BE.get(), (be, side) -> be.fluids());
            e.registerBlockEntity(Capabilities.Fluid.BLOCK, CORE_BE.get(), (be, side) -> be.tank());
            e.registerBlockEntity(Capabilities.Item.BLOCK, LOAD_BE.get(), (be, side) -> net.neoforged.neoforge.transfer.item.VanillaContainerWrapper.of(be));
        });
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                LOAD_ITEMS.values().forEach(i -> e.accept(i.get()));
                CONTAINER_ITEMS.values().forEach(i -> e.accept(i.get()));
                e.accept(LOADING_DOCK_ITEM.get());
                e.accept(RIFT_DOCK_ITEM.get());
            }
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) e.accept(CARGO_HOOK.get());
        });
    }
}
