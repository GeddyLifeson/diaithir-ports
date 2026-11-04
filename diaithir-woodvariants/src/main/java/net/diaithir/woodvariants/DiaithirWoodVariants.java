package net.diaithir.woodvariants;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Chairs, stools, benches, tables, walls and plank variants for woods that other mods add. The woods are rows in
 * data/diaithirwoodvariants/woods.json; a row whose mod is not loaded adds nothing.
 */
@Mod(DiaithirWoodVariants.ID)
public final class DiaithirWoodVariants {
    public static final String ID = "diaithirwoodvariants";
    private static final Logger LOG = LogUtils.getLogger();
    private static final String DATA = "/data/" + ID + "/woods.json";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ID);
    private static final List<DeferredItem<? extends Item>> ALL_ITEMS = new ArrayList<>();

    public static final DeferredHolder<EntityType<?>, EntityType<SeatEntity>> SEAT = ENTITIES.register("seat",
        id -> EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC).sized(0.01F, 0.01F).noSummon()
            .clientTrackingRange(8).updateInterval(20).build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public DiaithirWoodVariants(IEventBus bus) {
        int woods = 0;
        for (WoodDef wood : load()) {
            if (!wood.present(ModList.get()::isLoaded)) {
                continue;
            }
            woods++;
            add(wood, Piece.CHAIR, FurnitureBlock.Chair::new, true);
            add(wood, Piece.STOOL, FixedBlock::new, true);
            add(wood, Piece.BENCH, FurnitureBlock.Bench::new, true);
            add(wood, Piece.TABLE, TableBlock::new, true);
            add(wood, Piece.WALL, WallBlock::new, false);
            add(wood, Piece.VERTICAL, Block::new, false);
            add(wood, Piece.FRAMED, Block::new, false);
            DeferredBlock<DoorBlock> door = BLOCKS.registerBlock(wood.id() + "_" + Piece.DOOR.suffix,
                p -> new DoorBlock(BlockSetType.OAK, p), () -> props().noOcclusion().strength(3.0F).pushReaction(PushReaction.DESTROY));
            ALL_ITEMS.add(ITEMS.registerItem(wood.id() + "_" + Piece.DOOR.suffix,
                p -> new DoubleHighBlockItem(door.get(), p.useBlockDescriptionPrefix())));
            DeferredBlock<TrapDoorBlock> trap = BLOCKS.registerBlock(wood.id() + "_" + Piece.TRAPDOOR.suffix,
                p -> new TrapDoorBlock(BlockSetType.OAK, p), () -> props().noOcclusion().strength(3.0F));
            ALL_ITEMS.add(ITEMS.registerSimpleBlockItem(trap));
            if (wood.macaws()) {
                macaws(wood);
            }
        }
        MacawSet cfg = MacawSet.load(LOG);
        roofsAndPavings(cfg);
        LOG.info("Wood variants added for {} woods", woods);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        bus.addListener(DiaithirWoodVariants::tabs);
    }

    private static final String[] FENCES = {"picket", "stockade", "horse", "wired", "pyramid"};
    private static final String[] DOORS = {"barn", "stable", "cottage", "glass", "shoji"};
    private static final String[] TRAPS = {"beach", "barred", "glass", "cottage"};
    private static final String[] WINDOWS = {"window", "four_pane_window", "barred_window"};
    private static final String[] BRIDGES = {"plank_bridge", "log_bridge", "rope_bridge"};

    /** Fences, doors, trapdoors, windows, bridges and decorative furniture in the style of the Macaw's mods. */
    private static void macaws(WoodDef wood) {
        String w = wood.id();
        for (String f : FENCES) {
            add(wood, w + "_" + f + "_fence", FenceBlock::new, false);
        }
        for (String d : DOORS) {
            String name = w + "_" + d + "_door";
            DeferredBlock<DoorBlock> door = BLOCKS.registerBlock(name, p -> new DoorBlock(BlockSetType.OAK, p),
                () -> props().noOcclusion().strength(3.0F).pushReaction(PushReaction.DESTROY));
            ALL_ITEMS.add(ITEMS.registerItem(name, p -> new DoubleHighBlockItem(door.get(), p.useBlockDescriptionPrefix())));
        }
        for (String t : TRAPS) {
            String name = w + "_" + t + "_trapdoor";
            DeferredBlock<TrapDoorBlock> trap = BLOCKS.registerBlock(name, p -> new TrapDoorBlock(BlockSetType.OAK, p), () -> props().noOcclusion().strength(3.0F));
            ALL_ITEMS.add(ITEMS.registerSimpleBlockItem(trap));
        }
        for (String k : WINDOWS) {
            DeferredBlock<IronBarsBlock> b = BLOCKS.registerBlock(w + "_" + k, IronBarsBlock::new, () -> props().noOcclusion().strength(1.5F).sound(SoundType.GLASS));
            ALL_ITEMS.add(ITEMS.registerSimpleBlockItem(b));
        }
        for (String k : BRIDGES) {
            add(wood, w + "_" + k, BridgeBlock::new, true);
        }
        add(wood, w + "_coffee_table", p -> new DecorBlock(p, DecorBlock.COFFEE), true);
        add(wood, w + "_end_table", p -> new DecorBlock(p, DecorBlock.END), true);
        add(wood, w + "_cupboard", p -> new DecorBlock(p, DecorBlock.CUPBOARD), true);
        add(wood, w + "_counter", p -> new DecorBlock(p, DecorBlock.COUNTER), false);
        add(wood, w + "_bookcase", p -> new DecorBlock(p, DecorBlock.COUNTER), false);
    }

    private static BlockBehaviour.Properties roofProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_RED).sound(SoundType.STONE).strength(1.5F, 6.0F).requiresCorrectToolForDrops();
    }

    private static void roofsAndPavings(MacawSet cfg) {
        for (String c : cfg.roofColors()) {
            DeferredBlock<Block> base = BLOCKS.registerBlock(c + "_roof", Block::new, DiaithirWoodVariants::roofProps);
            ALL_ITEMS.add(ITEMS.registerSimpleBlockItem(base));
            DeferredBlock<StairBlock> stairs = BLOCKS.registerBlock(c + "_roof_stairs",
                p -> new StairBlock(base.get().defaultBlockState(), p), DiaithirWoodVariants::roofProps);
            ALL_ITEMS.add(ITEMS.registerSimpleBlockItem(stairs));
            DeferredBlock<SlabBlock> slab = BLOCKS.registerBlock(c + "_roof_slab", SlabBlock::new, DiaithirWoodVariants::roofProps);
            ALL_ITEMS.add(ITEMS.registerSimpleBlockItem(slab));
        }
        for (String pal : cfg.palettes()) {
            for (String pat : cfg.patterns()) {
                DeferredBlock<PavingBlock> b = BLOCKS.registerBlock(pat + "_paving_" + pal, PavingBlock::new,
                    () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(1.5F, 6.0F).requiresCorrectToolForDrops());
                ALL_ITEMS.add(ITEMS.registerSimpleBlockItem(b));
            }
        }
    }

    private static <B extends Block> void add(WoodDef wood, String name, Function<BlockBehaviour.Properties, B> factory, boolean open) {
        DeferredBlock<B> block = BLOCKS.registerBlock(name, factory, () -> open ? props().noOcclusion() : props());
        ALL_ITEMS.add(ITEMS.registerSimpleBlockItem(block));
    }

    private static BlockBehaviour.Properties props() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2.0F, 3.0F).ignitedByLava();
    }

    private static <B extends Block> void add(WoodDef wood, Piece piece, Function<BlockBehaviour.Properties, B> factory, boolean open) {
        String name = wood.id() + "_" + piece.suffix;
        DeferredBlock<B> block = BLOCKS.registerBlock(name, factory, () -> open ? props().noOcclusion() : props());
        ALL_ITEMS.add(ITEMS.registerSimpleBlockItem(block));
    }

    private static void tabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS || event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            for (DeferredItem<? extends Item> item : ALL_ITEMS) {
                event.accept(item);
            }
        }
    }

    private static List<WoodDef> load() {
        try (Reader r = new InputStreamReader(DiaithirWoodVariants.class.getResourceAsStream(DATA), StandardCharsets.UTF_8)) {
            return WoodDef.parse(r);
        } catch (IOException | RuntimeException e) {
            LOG.error("Could not read {}", DATA, e);
            return List.of();
        }
    }
}
