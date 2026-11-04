package net.diaithir.foundry;

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
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/** Foundry: melts metals by tag, casts shapes, cuts gems. Clean-room original. */
@Mod(DiaithirFoundry.ID)
public final class DiaithirFoundry {
    public static final String ID = "diaithirfoundry";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    private static BlockBehaviour.Properties steel() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F, 9.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties hot() {
        return steel().lightLevel(s -> 8);
    }

    public static final DeferredBlock<FoundryControllerBlock> CONTROLLER = BLOCKS.registerBlock("foundry_controller", FoundryControllerBlock::new, DiaithirFoundry::hot);
    public static final DeferredBlock<Block> BRICKS = BLOCKS.registerBlock("foundry_bricks", Block::new, DiaithirFoundry::steel);
    public static final DeferredBlock<Block> DRAIN = BLOCKS.registerBlock("foundry_drain", Block::new, DiaithirFoundry::steel);
    public static final DeferredBlock<Block> TANK = BLOCKS.registerBlock("foundry_tank", Block::new, DiaithirFoundry::steel);
    public static final DeferredBlock<GemCutterBlock> GEM_CUTTER = BLOCKS.registerBlock("gem_cutter", GemCutterBlock::new, DiaithirFoundry::steel);

    public static final DeferredItem<BlockItem> CONTROLLER_ITEM = ITEMS.registerSimpleBlockItem(CONTROLLER);
    public static final DeferredItem<BlockItem> BRICKS_ITEM = ITEMS.registerSimpleBlockItem(BRICKS);
    public static final DeferredItem<BlockItem> DRAIN_ITEM = ITEMS.registerSimpleBlockItem(DRAIN);
    public static final DeferredItem<BlockItem> TANK_ITEM = ITEMS.registerSimpleBlockItem(TANK);
    public static final DeferredItem<BlockItem> GEM_CUTTER_ITEM = ITEMS.registerSimpleBlockItem(GEM_CUTTER);

    public static final DeferredItem<Item> CAST_INGOT = ITEMS.registerSimpleItem("cast_ingot");
    public static final DeferredItem<Item> CAST_NUGGET = ITEMS.registerSimpleItem("cast_nugget");
    public static final DeferredItem<Item> CAST_PLATE = ITEMS.registerSimpleItem("cast_plate");
    public static final DeferredItem<Item> CAST_ROD = ITEMS.registerSimpleItem("cast_rod");
    public static final DeferredItem<Item> CAST_GEAR = ITEMS.registerSimpleItem("cast_gear");
    public static final DeferredItem<Item> CAST_BLOCK = ITEMS.registerSimpleItem("cast_block");

    public static final DeferredItem<Item> RAW_GEM = ITEMS.registerSimpleItem("raw_gem");
    public static final DeferredItem<Item> CUT_GEM = ITEMS.registerSimpleItem("cut_gem");
    public static final DeferredItem<Item> PLATE = ITEMS.registerSimpleItem("plate");
    public static final DeferredItem<Item> ROD = ITEMS.registerSimpleItem("rod");
    public static final DeferredItem<Item> GEAR = ITEMS.registerSimpleItem("gear");

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FoundryControllerBlockEntity>> CONTROLLER_BE = BLOCK_ENTITIES.register("foundry_controller",
            () -> new BlockEntityType<>(FoundryControllerBlockEntity::new, CONTROLLER.get()));

    public DiaithirFoundry(IEventBus bus, ModContainer container) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                for (DeferredItem<BlockItem> i : List.of(CONTROLLER_ITEM, BRICKS_ITEM, DRAIN_ITEM, TANK_ITEM, GEM_CUTTER_ITEM)) e.accept(i.get());
            }
            if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                for (DeferredItem<Item> i : List.of(CAST_INGOT, CAST_NUGGET, CAST_PLATE, CAST_ROD, CAST_GEAR, CAST_BLOCK, RAW_GEM, CUT_GEM, PLATE, ROD, GEAR))
                    e.accept(i.get());
            }
        });
    }
}
