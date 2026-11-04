package net.diaithir.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Wild plants of Age I Earth that no mod in the pack covers (see DiaithirRPG world/earth/age1/FLORA_COVERAGE.csv). The ids
 * match tools/gen_flora.py, which writes the assets, loot tables and worldgen.
 */
public final class Flora {
    public static final String MOD = "diaithircompat";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD);
    private static final DeferredRegister<PlacementModifierType<?>> PLACEMENT = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, MOD);

    public static final List<String> PLANTS = List.of("dryas_octopetala", "betula_nana", "salix_herbacea", "artemisia_frigida", "ephedra_distachya",
            "empetrum_nigrum", "calluna_vulgaris", "hippophae_rhamnoides", "saxifraga_oppositifolia", "wild_einkorn", "wild_emmer", "wild_barley",
            "wild_lentil", "wild_pea", "wild_chickpea");
    public static final List<String> FOODS = List.of("wild_einkorn_grain", "wild_emmer_grain", "wild_barley_grain", "wild_lentils", "wild_peas",
            "wild_chickpeas", "wild_figs", "crowberries", "sea_buckthorn_berries", "cembra_pine_nuts");

    public static final List<DeferredBlock<Block>> PLANT_BLOCKS = new java.util.ArrayList<>();
    public static final List<DeferredItem<Item>> FOOD_ITEMS = new java.util.ArrayList<>();
    public static final List<DeferredItem<?>> BLOCK_ITEMS = new java.util.ArrayList<>();

    static {
        for (String id : PLANTS) {
            DeferredBlock<Block> b = BLOCKS.registerBlock(id, WildPlantBlock::new, Flora::plant);
            PLANT_BLOCKS.add(b);
            BLOCK_ITEMS.add(ITEMS.registerSimpleBlockItem(b));
        }
        tree("cembra_pine", MapColor.COLOR_GREEN);
        tree("wild_fig", MapColor.COLOR_LIGHT_GREEN);
        for (String id : FOODS) {
            int nutrition = id.equals("wild_figs") ? 3 : 2;
            FOOD_ITEMS.add(ITEMS.registerItem(id, Item::new, () -> new Item.Properties().food(new FoodProperties(nutrition, 0.3F, false))));
        }
        PLACEMENT.register("earth_region", () -> EarthRegionFilter.TYPE);
    }

    private static void tree(String id, MapColor color) {
        ResourceKey<ConfiguredFeature<?, ?>> key = ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(MOD, id));
        TreeGrower grower = new TreeGrower(MOD + "_" + id, Optional.empty(), Optional.of(key), Optional.empty());
        DeferredBlock<Block> leaves = BLOCKS.registerBlock(id + "_leaves", WildLeavesBlock::new, () -> leaves(color));
        DeferredBlock<Block> sapling = BLOCKS.registerBlock(id + "_sapling", p -> new WildSaplingBlock(grower, p), () -> plant().mapColor(color));
        BLOCK_ITEMS.add(ITEMS.registerSimpleBlockItem(leaves));
        BLOCK_ITEMS.add(ITEMS.registerSimpleBlockItem(sapling));
        PLANT_BLOCKS.add(leaves);
        PLANT_BLOCKS.add(sapling);
    }

    private static BlockBehaviour.Properties plant() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties leaves(MapColor color) {
        return BlockBehaviour.Properties.of().mapColor(color).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion()
                .isValidSpawn((s, g, p, t) -> false).isSuffocating((s, g, p) -> false).isViewBlocking((s, g, p) -> false)
                .pushReaction(PushReaction.DESTROY).isRedstoneConductor((s, g, p) -> false);
    }

    static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        PLACEMENT.register(bus);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
                BLOCK_ITEMS.forEach(i -> e.accept(i.get()));
            } else if (e.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
                FOOD_ITEMS.forEach(i -> e.accept(i.get()));
            }
        });
    }
}
