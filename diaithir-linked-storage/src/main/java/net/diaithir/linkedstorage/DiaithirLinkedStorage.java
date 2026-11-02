package net.diaithir.linkedstorage;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
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
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;

/** Linked chest, tank and cell. Clean-room: written from a behaviour description only. */
@Mod(DiaithirLinkedStorage.ID)
public final class DiaithirLinkedStorage {
    public static final String ID = "diaithirlinkedstorage";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 8.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    public static final DeferredBlock<LinkedBlock> CHEST = BLOCKS.registerBlock("linked_chest", p -> new LinkedBlock(p, LinkKind.CHEST), DiaithirLinkedStorage::metal);
    public static final DeferredBlock<LinkedBlock> TANK = BLOCKS.registerBlock("linked_tank", p -> new LinkedBlock(p, LinkKind.TANK), DiaithirLinkedStorage::metal);
    public static final DeferredBlock<LinkedBlock> CELL = BLOCKS.registerBlock("linked_cell", p -> new LinkedBlock(p, LinkKind.CELL), DiaithirLinkedStorage::metal);

    public static final DeferredItem<BlockItem> CHEST_ITEM = ITEMS.registerSimpleBlockItem(CHEST);
    public static final DeferredItem<BlockItem> TANK_ITEM = ITEMS.registerSimpleBlockItem(TANK);
    public static final DeferredItem<BlockItem> CELL_ITEM = ITEMS.registerSimpleBlockItem(CELL);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LinkedBlockEntity>> LINKED_BE = BLOCK_ENTITIES.register("linked",
            () -> new BlockEntityType<>(LinkedBlockEntity::new, CHEST.get(), TANK.get(), CELL.get()));

    public DiaithirLinkedStorage(IEventBus bus, ModContainer container) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                for (DeferredItem<BlockItem> i : List.of(CHEST_ITEM, TANK_ITEM, CELL_ITEM)) e.accept(i.get());
            }
        });
        bus.addListener(RegisterCapabilitiesEvent.class, e -> {
            e.registerBlockEntity(Capabilities.Item.BLOCK, LINKED_BE.get(), DiaithirLinkedStorage::itemCap);
            e.registerBlockEntity(Capabilities.Fluid.BLOCK, LINKED_BE.get(), DiaithirLinkedStorage::fluidCap);
            e.registerBlockEntity(Capabilities.Energy.BLOCK, LINKED_BE.get(), DiaithirLinkedStorage::energyCap);
        });
    }

    @SuppressWarnings("unchecked")
    private static ResourceHandler<ItemResource> itemCap(LinkedBlockEntity be, Direction side) {
        Object h = be.itemHandler();
        return h instanceof ResourceHandler<?> r ? (ResourceHandler<ItemResource>) r : null;
    }

    @SuppressWarnings("unchecked")
    private static ResourceHandler<FluidResource> fluidCap(LinkedBlockEntity be, Direction side) {
        Object h = be.fluidHandler();
        return h instanceof ResourceHandler<?> r ? (ResourceHandler<FluidResource>) r : null;
    }

    private static EnergyHandler energyCap(LinkedBlockEntity be, Direction side) {
        Object h = be.energyHandler();
        return h instanceof EnergyHandler r ? r : null;
    }
}
