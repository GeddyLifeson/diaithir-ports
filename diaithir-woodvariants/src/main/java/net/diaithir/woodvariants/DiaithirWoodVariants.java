package net.diaithir.woodvariants;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.BlockItem;
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
    private static final List<DeferredItem<BlockItem>> ALL_ITEMS = new ArrayList<>();

    public DiaithirWoodVariants(IEventBus bus) {
        int woods = 0;
        for (WoodDef wood : load()) {
            if (!ModList.get().isLoaded(wood.mod())) {
                continue;
            }
            woods++;
            add(wood, Piece.CHAIR, FurnitureBlock.Chair::new, true);
            add(wood, Piece.STOOL, FixedBlock.Stool::new, true);
            add(wood, Piece.BENCH, FurnitureBlock.Bench::new, true);
            add(wood, Piece.TABLE, FixedBlock.Table::new, true);
            add(wood, Piece.WALL, WallBlock::new, false);
            add(wood, Piece.VERTICAL, Block::new, false);
            add(wood, Piece.FRAMED, Block::new, false);
        }
        LOG.info("Wood variants added for {} woods", woods);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        bus.addListener(DiaithirWoodVariants::tabs);
    }

    private static <B extends Block> void add(WoodDef wood, Piece piece, Function<BlockBehaviour.Properties, B> factory, boolean open) {
        String name = wood.id() + "_" + piece.suffix;
        DeferredBlock<B> block = BLOCKS.registerBlock(name, factory, () -> {
            BlockBehaviour.Properties p = BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD)
                .strength(2.0F, 3.0F).ignitedByLava();
            return open ? p.noOcclusion() : p;
        });
        ALL_ITEMS.add(ITEMS.registerSimpleBlockItem(block));
    }

    private static void tabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS || event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            for (DeferredItem<BlockItem> item : ALL_ITEMS) {
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
