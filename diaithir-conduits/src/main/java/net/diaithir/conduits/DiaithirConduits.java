package net.diaithir.conduits;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Wireless conduits: nodes that join an owner-scoped numbered channel and carry items, fluids and FE between any two nodes on it, across dimensions.
 * Clean-room: written from public descriptions of the idea only (no code, textures or names of any other mod).
 */
@Mod(DiaithirConduits.ID)
public final class DiaithirConduits {
    public static final String ID = "diaithirconduits";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F, 9.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    public static final DeferredBlock<ConduitBlock> CONDUIT_1 = BLOCKS.registerBlock("conduit_1", p -> new ConduitBlock(p, 1), DiaithirConduits::metal);
    public static final DeferredBlock<ConduitBlock> CONDUIT_2 = BLOCKS.registerBlock("conduit_2", p -> new ConduitBlock(p, 2), DiaithirConduits::metal);
    public static final DeferredBlock<CellBlock> FLUID_CELL = BLOCKS.registerBlock("fluid_cell", p -> new CellBlock(p, 0), DiaithirConduits::metal);
    public static final DeferredBlock<CellBlock> ENERGY_CELL = BLOCKS.registerBlock("energy_cell", p -> new CellBlock(p, 1), DiaithirConduits::metal);

    public static final DeferredItem<BlockItem> CONDUIT_1_ITEM = ITEMS.registerSimpleBlockItem(CONDUIT_1);
    public static final DeferredItem<BlockItem> CONDUIT_2_ITEM = ITEMS.registerSimpleBlockItem(CONDUIT_2);
    public static final DeferredItem<BlockItem> FLUID_CELL_ITEM = ITEMS.registerSimpleBlockItem(FLUID_CELL);
    public static final DeferredItem<BlockItem> ENERGY_CELL_ITEM = ITEMS.registerSimpleBlockItem(ENERGY_CELL);
    public static final DeferredItem<Item> TUNER = ITEMS.registerSimpleItem("tuner");

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConduitBlockEntity>> CONDUIT_BE = BLOCK_ENTITIES.register("conduit",
            () -> new BlockEntityType<>(ConduitBlockEntity::new, CONDUIT_1.get(), CONDUIT_2.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CellBlockEntity>> CELL_BE = BLOCK_ENTITIES.register("cell",
            () -> new BlockEntityType<>(CellBlockEntity::new, FLUID_CELL.get(), ENERGY_CELL.get()));

    public DiaithirConduits(IEventBus bus, ModContainer container) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(RegisterCapabilitiesEvent.class, e -> {
            e.registerBlockEntity(Capabilities.Item.BLOCK, CONDUIT_BE.get(), (be, side) -> be.itemSink(side));
            e.registerBlockEntity(Capabilities.Fluid.BLOCK, CONDUIT_BE.get(), (be, side) -> be.fluidSink(side));
            e.registerBlockEntity(Capabilities.Energy.BLOCK, CONDUIT_BE.get(), (be, side) -> be.energySink(side));
            e.registerBlockEntity(Capabilities.Fluid.BLOCK, CELL_BE.get(), (be, side) -> be.tank);
            e.registerBlockEntity(Capabilities.Energy.BLOCK, CELL_BE.get(), (be, side) -> be.battery);
        });
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                for (DeferredItem<? extends Item> i : List.of(CONDUIT_1_ITEM, CONDUIT_2_ITEM, FLUID_CELL_ITEM, ENERGY_CELL_ITEM, TUNER)) e.accept(i.get());
            }
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> ConduitCommands.register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.level.block.BreakBlockEvent.class, DiaithirConduits::onBreak);
    }

    /** Only a node's owner (or an operator) may break it. */
    private static void onBreak(net.neoforged.neoforge.event.level.block.BreakBlockEvent e) {
        if (e.getLevel().getBlockEntity(e.getPos()) instanceof ConduitBlockEntity be && e.getPlayer() instanceof ServerPlayer p && !be.canManage(p)) {
            e.setCanceled(true);
            p.sendOverlayMessage(Component.literal("This node belongs to " + be.ownerName + "."));
        }
    }
}
