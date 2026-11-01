package net.diaithir.storagenet;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/** Storage network: controller, cables, import/export/filter cables, access terminal and crafting terminal. Clean-room: written from public descriptions of the idea only. */
@Mod(DiaithirStorageNet.ID)
public final class DiaithirStorageNet {
    public static final String ID = "diaithirstoragenet";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 8.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties wire() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5F, 4.0F).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops();
    }

    public static final DeferredBlock<ControllerBlock> CONTROLLER = BLOCKS.registerBlock("controller", ControllerBlock::new, DiaithirStorageNet::metal);
    public static final DeferredBlock<CableBlock> CABLE = BLOCKS.registerBlock("cable", p -> new CableBlock(p, CableKind.CABLE), DiaithirStorageNet::wire);
    public static final DeferredBlock<PortCableBlock> IMPORT_CABLE = BLOCKS.registerBlock("import_cable", p -> new PortCableBlock(p, CableKind.IMPORT), DiaithirStorageNet::wire);
    public static final DeferredBlock<PortCableBlock> EXPORT_CABLE = BLOCKS.registerBlock("export_cable", p -> new PortCableBlock(p, CableKind.EXPORT), DiaithirStorageNet::wire);
    public static final DeferredBlock<PortCableBlock> FILTER_CABLE = BLOCKS.registerBlock("filter_cable", p -> new PortCableBlock(p, CableKind.FILTER), DiaithirStorageNet::wire);
    public static final DeferredBlock<TerminalBlock> TERMINAL = BLOCKS.registerBlock("terminal", p -> new TerminalBlock(p, false), DiaithirStorageNet::metal);
    public static final DeferredBlock<TerminalBlock> CRAFTING_TERMINAL = BLOCKS.registerBlock("crafting_terminal", p -> new TerminalBlock(p, true), DiaithirStorageNet::metal);

    public static final DeferredItem<BlockItem> CONTROLLER_ITEM = ITEMS.registerSimpleBlockItem(CONTROLLER);
    public static final DeferredItem<BlockItem> CABLE_ITEM = ITEMS.registerSimpleBlockItem(CABLE);
    public static final DeferredItem<BlockItem> IMPORT_CABLE_ITEM = ITEMS.registerSimpleBlockItem(IMPORT_CABLE);
    public static final DeferredItem<BlockItem> EXPORT_CABLE_ITEM = ITEMS.registerSimpleBlockItem(EXPORT_CABLE);
    public static final DeferredItem<BlockItem> FILTER_CABLE_ITEM = ITEMS.registerSimpleBlockItem(FILTER_CABLE);
    public static final DeferredItem<BlockItem> TERMINAL_ITEM = ITEMS.registerSimpleBlockItem(TERMINAL);
    public static final DeferredItem<BlockItem> CRAFTING_TERMINAL_ITEM = ITEMS.registerSimpleBlockItem(CRAFTING_TERMINAL);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ControllerBlockEntity>> CONTROLLER_BE = BLOCK_ENTITIES.register("controller",
            () -> new BlockEntityType<>(ControllerBlockEntity::new, CONTROLLER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PortCableBlockEntity>> PORT_BE = BLOCK_ENTITIES.register("port_cable",
            () -> new BlockEntityType<>(PortCableBlockEntity::new, IMPORT_CABLE.get(), EXPORT_CABLE.get(), FILTER_CABLE.get()));

    public DiaithirStorageNet(IEventBus bus, ModContainer container) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                for (DeferredItem<BlockItem> i : List.of(CONTROLLER_ITEM, CABLE_ITEM, IMPORT_CABLE_ITEM, EXPORT_CABLE_ITEM, FILTER_CABLE_ITEM, TERMINAL_ITEM,
                        CRAFTING_TERMINAL_ITEM)) e.accept(i.get());
            }
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> StorageCommands.register(e.getDispatcher()));
    }
}
