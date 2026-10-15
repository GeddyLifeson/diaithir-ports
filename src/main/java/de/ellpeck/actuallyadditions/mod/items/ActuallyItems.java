/*
 * This file ("InitItems.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items;

import net.minecraft.core.component.DataComponents;

import net.minecraft.world.level.block.entity.FuelValues;

import net.minecraft.world.item.ToolMaterial;

import net.neoforged.neoforge.transfer.energy.ItemAccessEnergyHandler;

import de.ellpeck.actuallyadditions.registration.RegistryIds;

import com.google.common.collect.ImmutableSet;
import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import de.ellpeck.actuallyadditions.api.ActuallyTags;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.blocks.AABlockItem;
import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;
import de.ellpeck.actuallyadditions.mod.components.ActuallyComponents;
import de.ellpeck.actuallyadditions.mod.items.base.ItemBase;
import de.ellpeck.actuallyadditions.mod.items.base.ItemEnergy;
import de.ellpeck.actuallyadditions.mod.items.lens.ItemLens;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.ComponentEnergyStorage;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.Supplier;

public final class ActuallyItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ActuallyAdditions.MODID);

    // MISC ITEMS
    public static final DeferredItem<ItemBase> CANOLA = RegistryIds.item(ITEMS, "canola", () -> new ItemBase());
    public static final DeferredItem<ItemCoffee> COFFEE_CUP = RegistryIds.item(ITEMS, "coffee_cup", ItemCoffee::new);
    public static final DeferredItem<ItemBase> RING = RegistryIds.item(ITEMS, "ring", () -> new ItemBase());
    public static final DeferredItem<ItemBase> BASIC_COIL = RegistryIds.item(ITEMS, "basic_coil", () -> new ItemBase());
    public static final DeferredItem<ItemBase> ADVANCED_COIL = RegistryIds.item(ITEMS, "advanced_coil", () -> new ItemBase());
    public static final DeferredItem<ItemBase> RICE = RegistryIds.item(ITEMS, "rice", () -> new ItemBase());
    public static final DeferredItem<ItemBase> RICE_DOUGH = RegistryIds.item(ITEMS, "rice_dough", () -> new ItemBase());
    public static final DeferredItem<ItemBase> TINY_COAL = RegistryIds.item(ITEMS, "tiny_coal", () -> new ItemBase() {
        @Override
        public int getBurnTime(@Nonnull ItemStack itemStack, @Nullable RecipeType<?> recipeType, FuelValues fuelValues) {
            return 200;
        }
    });
    public static final DeferredItem<ItemBase> TINY_CHARCOAL = RegistryIds.item(ITEMS, "tiny_charcoal", () -> new ItemBase() {
        @Override
        public int getBurnTime(@Nonnull ItemStack itemStack, @Nullable RecipeType<?> recipeType, FuelValues fuelValues) {
            return 200;
        }
    });
    public static final DeferredItem<ItemBase> RICE_SLIMEBALL = RegistryIds.item(ITEMS, "rice_slimeball", () -> new ItemBase());
    public static final DeferredItem<ItemBase> EMPTY_CUP = RegistryIds.item(ITEMS, "empty_cup", () -> new ItemBase());
    public static final DeferredItem<ItemBase> BATS_WING = RegistryIds.item(ITEMS, "bats_wing", () -> new ItemBase());
    public static final DeferredItem<ItemBase> DRILL_CORE = RegistryIds.item(ITEMS, "drill_core", () -> new ItemBase());
    public static final DeferredItem<ItemBase> LENS = RegistryIds.item(ITEMS, "lens", () -> new ItemBase());
    public static final DeferredItem<ItemBase> ENDER_STAR = RegistryIds.item(ITEMS, "ender_star", () -> new ItemBase() {
        @Override
        public boolean isFoil(@Nonnull ItemStack pStack) {
            return true;
        }
    });
    public static final DeferredItem<CanolaSeed> CRYSTALLIZED_CANOLA_SEED = RegistryIds.item(ITEMS, "crystallized_canola_seed", () -> new CanolaSeed(false));
    public static final DeferredItem<CanolaSeed> EMPOWERED_CANOLA_SEED = RegistryIds.item(ITEMS, "empowered_canola_seed", () -> new CanolaSeed(true));

    // SHARDS
    public static final DeferredItem<ItemBase> RESTONIA_CRYSTAL_SHARD = RegistryIds.item(ITEMS, "restonia_crystal_shard", () -> new ItemBase());
    public static final DeferredItem<ItemBase> PALIS_CRYSTAL_SHARD = RegistryIds.item(ITEMS, "palis_crystal_shard", () -> new ItemBase());
    public static final DeferredItem<ItemBase> DIAMATINE_CRYSTAL_SHARD = RegistryIds.item(ITEMS, "diamatine_crystal_shard", () -> new ItemBase());
    public static final DeferredItem<ItemBase> VOID_CRYSTAL_SHARD = RegistryIds.item(ITEMS, "void_crystal_shard", () -> new ItemBase());
    public static final DeferredItem<ItemBase> EMERADIC_CRYSTAL_SHARD = RegistryIds.item(ITEMS, "emeradic_crystal_shard", () -> new ItemBase());
    public static final DeferredItem<ItemBase> ENORI_CRYSTAL_SHARD = RegistryIds.item(ITEMS, "enori_crystal_shard", () -> new ItemBase());

    // CRYSTALS
    public static final DeferredItem<ItemCrystal> RESTONIA_CRYSTAL = RegistryIds.item(ITEMS, "restonia_crystal", () -> new ItemCrystal());
    public static final DeferredItem<ItemCrystal> PALIS_CRYSTAL = RegistryIds.item(ITEMS, "palis_crystal", () -> new ItemCrystal());
    public static final DeferredItem<ItemCrystal> DIAMATINE_CRYSTAL = RegistryIds.item(ITEMS, "diamatine_crystal", () -> new ItemCrystal());
    public static final DeferredItem<ItemCrystal> VOID_CRYSTAL = RegistryIds.item(ITEMS, "void_crystal", () -> new ItemCrystal());
    public static final DeferredItem<ItemCrystal> EMERADIC_CRYSTAL = RegistryIds.item(ITEMS, "emeradic_crystal", () -> new ItemCrystal());
    public static final DeferredItem<ItemCrystal> ENORI_CRYSTAL = RegistryIds.item(ITEMS, "enori_crystal", () -> new ItemCrystal());

    public static final DeferredItem<ItemCrystal> EMPOWERED_RESTONIA_CRYSTAL = RegistryIds.item(ITEMS, "empowered_restonia_crystal", () -> new ItemCrystal(true));
    public static final DeferredItem<ItemCrystal> EMPOWERED_PALIS_CRYSTAL = RegistryIds.item(ITEMS, "empowered_palis_crystal", () -> new ItemCrystal(true));
    public static final DeferredItem<ItemCrystal> EMPOWERED_DIAMATINE_CRYSTAL = RegistryIds.item(ITEMS, "empowered_diamatine_crystal", () -> new ItemCrystal(true));
    public static final DeferredItem<ItemCrystal> EMPOWERED_VOID_CRYSTAL = RegistryIds.item(ITEMS, "empowered_void_crystal", () -> new ItemCrystal(true));
    public static final DeferredItem<ItemCrystal> EMPOWERED_EMERADIC_CRYSTAL = RegistryIds.item(ITEMS, "empowered_emeradic_crystal", () -> new ItemCrystal(true));
    public static final DeferredItem<ItemCrystal> EMPOWERED_ENORI_CRYSTAL = RegistryIds.item(ITEMS, "empowered_enori_crystal", () -> new ItemCrystal(true));

    // BLACK QUARTZ
    public static final DeferredItem<ItemBase> BLACK_QUARTZ = RegistryIds.item(ITEMS, "black_quartz", () -> new ItemBase());

    public static final DeferredItem<ItemEngineerGoggles> ENGINEERS_GOGGLES_ADVANCED = RegistryIds.item(ITEMS, "engineers_goggles_advanced", () -> new ItemEngineerGoggles(true));
    public static final DeferredItem<ItemEngineerGoggles> ENGINEERS_GOGGLES = RegistryIds.item(ITEMS, "engineers_goggles", () -> new ItemEngineerGoggles(false));
    public static final DeferredItem<ItemLaserRelayUpgrade> LASER_UPGRADE_RANGE = RegistryIds.item(ITEMS, "laser_upgrade_range", ItemLaserRelayUpgrade::new);
    public static final DeferredItem<ItemLaserRelayUpgrade> LASER_UPGRADE_INVISIBILITY = RegistryIds.item(ITEMS, "laser_upgrade_invisibility", ItemLaserRelayUpgrade::new);
    public static final DeferredItem<Filler> HANDHELD_FILLER = RegistryIds.item(ITEMS, "handheld_filler", Filler::new);
    public static final DeferredItem<Sack> TRAVELERS_SACK = RegistryIds.item(ITEMS, "travelers_sack", () -> new Sack(false));
    public static final DeferredItem<Sack> VOID_SACK = RegistryIds.item(ITEMS, "void_sack", () -> new Sack(true));
    public static final DeferredItem<Worm> WORM = RegistryIds.item(ITEMS, "worm", Worm::new);
    public static final DeferredItem<ItemPlayerProbe> PLAYER_PROBE = RegistryIds.item(ITEMS, "player_probe", ItemPlayerProbe::new);
    public static final DeferredItem<ItemFilter> FILTER = RegistryIds.item(ITEMS, "filter", ItemFilter::new);
    public static final DeferredItem<Item> ITEM_TAG = RegistryIds.item(ITEMS, "item_tag", ItemTag::new);
    public static final DeferredItem<ItemWaterBowl> WATER_BOWL = RegistryIds.item(ITEMS, "water_bowl", ItemWaterBowl::new);
    public static final DeferredItem<ItemGeneric> CRATE_KEEPER = RegistryIds.item(ITEMS, "crate_keeper", () -> new ItemGeneric(defaultProps().stacksTo(1)));
    public static final DeferredItem<ItemLens> LENS_OF_COLOR = RegistryIds.item(ITEMS, "lens_of_color", () -> new ItemLens(ActuallyAdditionsAPI.lensColor));
    public static final DeferredItem<ItemLens> LENS_OF_DETONATION = RegistryIds.item(ITEMS, "lens_of_detonation", () -> new ItemLens(ActuallyAdditionsAPI.lensDetonation));
    public static final DeferredItem<ItemLens> LENS_OF_CERTAIN_DEATH = RegistryIds.item(ITEMS, "lens_of_certain_death", () -> new ItemLens(ActuallyAdditionsAPI.lensDeath));
    public static final DeferredItem<ItemLens> LENS_OF_THE_KILLER = RegistryIds.item(ITEMS, "lens_of_the_killer", () -> new ItemLens(ActuallyAdditionsAPI.lensEvenMoarDeath));
    public static final DeferredItem<ItemLens> LENS_OF_DISENCHANTING = RegistryIds.item(ITEMS, "lens_of_disenchanting", () -> new ItemLens(ActuallyAdditionsAPI.lensDisenchanting));
    public static final DeferredItem<ItemLens> LENS_OF_THE_MINER = RegistryIds.item(ITEMS, "lens_of_the_miner", () -> new ItemLens(ActuallyAdditionsAPI.lensMining));
    public static final DeferredItem<ItemLaserWrench> LASER_WRENCH = RegistryIds.item(ITEMS, "laser_wrench", ItemLaserWrench::new);
    //    public static final DeferredItem<Item> itemChestToCrateUpgrade = RegistryIds.item(ITEMS, "", new ItemChestToCrateUpgrade("chest_to_crate_upgrade", TileEntityChest.class, InitBlocks.blockGiantChest.getDefaultState()));
    //    public static final DeferredItem<Item> itemSmallToMediumCrateUpgrade = RegistryIds.item(ITEMS, "", new ItemChestToCrateUpgrade("small_to_medium_crate_upgrade", TileEntityGiantChest.class, InitBlocks.blockGiantChestMedium.getDefaultState()));
    //    public static final DeferredItem<Item> itemMediumToLargeCrateUpgrade = RegistryIds.item(ITEMS, "", new ItemChestToCrateUpgrade("medium_to_large_crate_upgrade", TileEntityGiantChestMedium.class, InitBlocks.blockGiantChestLarge.getDefaultState()));
    public static final DeferredItem<ItemBooklet> ITEM_BOOKLET = RegistryIds.item(ITEMS, "booklet", ItemBooklet::new);
    public static final DeferredItem<ItemGrowthRing> RING_OF_GROWTH = RegistryIds.item(ITEMS, "ring_of_growth", ItemGrowthRing::new);
    public static final DeferredItem<ItemMagnetRing> RING_OF_MAGNETIZING = RegistryIds.item(ITEMS, "ring_of_magnetizing", ItemMagnetRing::new);
    public static final DeferredItem<ItemTeleportStaff> TELEPORT_STAFF = RegistryIds.item(ITEMS, "teleport_staff", ItemTeleportStaff::new);
    public static final DeferredItem<ItemWingsOfTheBats> WINGS_OF_THE_BATS = RegistryIds.item(ITEMS, "wings_of_the_bats", ItemWingsOfTheBats::new);

    public static final DeferredItem<DrillItem> DRILL_MAIN = RegistryIds.item(ITEMS, "drill_light_blue", DrillItem::new);
    public static final DeferredItem<DrillItem> DRILL_BLACK = RegistryIds.item(ITEMS, "drill_black", DrillItem::new);
    public static final DeferredItem<DrillItem> DRILL_BLUE = RegistryIds.item(ITEMS, "drill_blue", DrillItem::new);
    public static final DeferredItem<DrillItem> DRILL_BROWN = RegistryIds.item(ITEMS, "drill_brown", DrillItem::new);
    public static final DeferredItem<DrillItem> DRILL_CYAN = RegistryIds.item(ITEMS, "drill_cyan", DrillItem::new);
    public static final DeferredItem<DrillItem> DRILL_GRAY = RegistryIds.item(ITEMS, "drill_gray", DrillItem::new);
    public static final DeferredItem<DrillItem> DRILL_GREEN = RegistryIds.item(ITEMS, "drill_green", DrillItem::new);
    public static final DeferredItem<DrillItem> DRILL_LIGHT_GRAY = RegistryIds.item(ITEMS, "drill_light_gray", DrillItem::new);
    public static final DeferredItem<DrillItem> DRILL_LIME = RegistryIds.item(ITEMS, "drill_lime", DrillItem::new);
    public static final DeferredItem<DrillItem> DRILL_MAGENTA = RegistryIds.item(ITEMS, "drill_magenta", DrillItem::new);
    public static final DeferredItem<DrillItem> DRILL_ORANGE = RegistryIds.item(ITEMS, "drill_orange", DrillItem::new);
    public static final DeferredItem<DrillItem> DRILL_PINK = RegistryIds.item(ITEMS, "drill_pink", DrillItem::new);
    public static final DeferredItem<DrillItem> DRILL_PURPLE = RegistryIds.item(ITEMS, "drill_purple", DrillItem::new);
    public static final DeferredItem<DrillItem> DRILL_RED = RegistryIds.item(ITEMS, "drill_red", DrillItem::new);
    public static final DeferredItem<DrillItem> DRILL_WHITE = RegistryIds.item(ITEMS, "drill_white", DrillItem::new);
    public static final DeferredItem<DrillItem> DRILL_YELLOW = RegistryIds.item(ITEMS, "drill_yellow", DrillItem::new);


    public static final DeferredItem<ItemBattery> SINGLE_BATTERY = RegistryIds.item(ITEMS, "single_battery", () -> new ItemBattery(200000, 1000));
    public static final DeferredItem<ItemBattery> DOUBLE_BATTERY = RegistryIds.item(ITEMS, "double_battery", () -> new ItemBattery(350000, 5000));
    public static final DeferredItem<ItemBattery> TRIPLE_BATTERY = RegistryIds.item(ITEMS, "triple_battery", () -> new ItemBattery(600000, 10000));
    public static final DeferredItem<ItemBattery> QUADRUPLE_BATTERY = RegistryIds.item(ITEMS, "quadruple_battery", () -> new ItemBattery(1000000, 30000));
    public static final DeferredItem<ItemBattery> QUINTUPLE_BATTERY = RegistryIds.item(ITEMS, "quintuple_battery", () -> new ItemBattery(2000000, 100000));
    public static final DeferredItem<ItemDrillUpgrade> DRILL_UPGRADE_SPEED = RegistryIds.item(ITEMS, "drill_upgrade_speed", () -> new ItemDrillUpgrade(ItemDrillUpgrade.UpgradeType.SPEED));
    public static final DeferredItem<ItemDrillUpgrade> DRILL_UPGRADE_SPEED_II = RegistryIds.item(ITEMS, "drill_upgrade_speed_ii", () -> new ItemDrillUpgrade(ItemDrillUpgrade.UpgradeType.SPEED_II));
    public static final DeferredItem<ItemDrillUpgrade> DRILL_UPGRADE_SPEED_III = RegistryIds.item(ITEMS, "drill_upgrade_speed_iii", () -> new ItemDrillUpgrade(ItemDrillUpgrade.UpgradeType.SPEED_III));
    public static final DeferredItem<ItemDrillUpgrade> DRILL_UPGRADE_SILK_TOUCH = RegistryIds.item(ITEMS, "drill_upgrade_silk_touch", () -> new ItemDrillUpgrade(ItemDrillUpgrade.UpgradeType.SILK_TOUCH));
    public static final DeferredItem<ItemDrillUpgrade> DRILL_UPGRADE_FORTUNE = RegistryIds.item(ITEMS, "drill_upgrade_fortune", () -> new ItemDrillUpgrade(ItemDrillUpgrade.UpgradeType.FORTUNE));
    public static final DeferredItem<ItemDrillUpgrade> DRILL_UPGRADE_FORTUNE_II = RegistryIds.item(ITEMS, "drill_upgrade_fortune_ii", () -> new ItemDrillUpgrade(ItemDrillUpgrade.UpgradeType.FORTUNE_II));
    public static final DeferredItem<ItemDrillUpgrade> DRILL_UPGRADE_THREE_BY_THREE = RegistryIds.item(ITEMS, "drill_upgrade_three_by_three", () -> new ItemDrillUpgrade(ItemDrillUpgrade.UpgradeType.THREE_BY_THREE));
    public static final DeferredItem<ItemDrillUpgrade> DRILL_UPGRADE_FIVE_BY_FIVE = RegistryIds.item(ITEMS, "drill_upgrade_five_by_five", () -> new ItemDrillUpgrade(ItemDrillUpgrade.UpgradeType.FIVE_BY_FIVE));
    public static final DeferredItem<ItemDrillUpgrade> DRILL_UPGRADE_BLOCK_PLACING = RegistryIds.item(ITEMS, "drill_upgrade_block_placing", () -> new ItemDrillUpgrade(ItemDrillUpgrade.UpgradeType.PLACER));
    public static final DeferredItem<ItemPhantomConnector> PHANTOM_CONNECTOR = RegistryIds.item(ITEMS, "phantom_connector", ItemPhantomConnector::new);
    //public static final DeferredItem<Item> FOOD = RegistryIds.item(ITEMS, "food", () -> new ItemBase()); //just... food? //TODO
    public static final DeferredItem<ItemCrafterOnAStick> CRAFTER_ON_A_STICK = RegistryIds.item(ITEMS, "crafter_on_a_stick", ItemCrafterOnAStick::new);
    //public static final DeferredItem<Item> DUST = RegistryIds.item(ITEMS, "dust", ItemDust::new); //TODO flatten
    public static final DeferredItem<ItemSolidifiedExperience> SOLIDIFIED_EXPERIENCE = RegistryIds.item(ITEMS, "solidified_experience", ItemSolidifiedExperience::new);
    public static final DeferredItem<ItemLeafBlower> LEAF_BLOWER = RegistryIds.item(ITEMS, "leaf_blower", () -> new ItemLeafBlower(false));
    public static final DeferredItem<ItemLeafBlower> ADVANCED_LEAF_BLOWER = RegistryIds.item(ITEMS, "advanced_leaf_blower", () -> new ItemLeafBlower(true));

    public static final DeferredItem<AABlockItem.AASeedItem> COFFEE_BEANS = RegistryIds.item(ITEMS, "coffee_beans", () -> new AABlockItem.AASeedItem(ActuallyBlocks.COFFEE.get(), ActuallyItems.defaultProps()));

    public static final DeferredItem<AABlockItem.AASeedItem> RICE_SEEDS = RegistryIds.item(ITEMS, "rice_seeds", () -> new AABlockItem.AASeedItem(ActuallyBlocks.RICE.get(), ActuallyItems.defaultProps())); //() -> new ItemSeed("seedRice", ActuallyBlocks.RICE.get(), FOOD.get(), TheFoods.RICE.ordinal()));
    public static final DeferredItem<AABlockItem.AASeedItem> CANOLA_SEEDS = RegistryIds.item(ITEMS, "canola_seeds", () -> new AABlockItem.AASeedItem(ActuallyBlocks.CANOLA.get(), ActuallyItems.defaultProps())); //() -> new ItemFoodSeed("seedCanola", ActuallyBlocks.CANOLA, itemMisc, 0, 1, 0.01F, 10).setPotionEffect(new PotionEffect(MobEffects.NAUSEA, 1000, 0), 0.2F));
    public static final DeferredItem<AABlockItem.AASeedItem> FLAX_SEEDS = RegistryIds.item(ITEMS, "flax_seeds", () -> new AABlockItem.AASeedItem(ActuallyBlocks.FLAX.get(), ActuallyItems.defaultProps())); //() -> new ItemSeed("seedFlax", ActuallyBlocks.FLAX, Items.STRING, 0));

    // TOOLS & ARMOR
    public static final DeferredItem<AllInOneTool> WOODEN_AIOT = RegistryIds.item(ITEMS, "wooden_aiot", () -> new AllInOneTool(ToolMaterial.WOOD));
    public static final DeferredItem<AllInOneTool> STONE_AIOT = RegistryIds.item(ITEMS, "stone_aiot", () -> new AllInOneTool(ToolMaterial.STONE));
    public static final DeferredItem<AllInOneTool> IRON_AIOT = RegistryIds.item(ITEMS, "iron_aiot", () -> new AllInOneTool(ToolMaterial.IRON));
    public static final DeferredItem<AllInOneTool> GOLD_AIOT = RegistryIds.item(ITEMS, "gold_aiot", () -> new AllInOneTool(ToolMaterial.GOLD));
    public static final DeferredItem<AllInOneTool> DIAMOND_AIOT = RegistryIds.item(ITEMS, "diamond_aiot", () -> new AllInOneTool(ToolMaterial.DIAMOND));
    public static final DeferredItem<AllInOneTool> NETHERITE_AIOT = RegistryIds.item(ITEMS, "netherite_aiot", () -> new AllInOneTool(ToolMaterial.NETHERITE));

    // Banner Pattern
    public static final DeferredItem<Item> DRILL_PATTERN = RegistryIds.item(ITEMS, "drill_pattern", () -> new Item(defaultNonStacking().delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(ActuallyTags.BannerPatterns.PATTERN_DRILL))));
    public static final DeferredItem<Item> LEAF_BLO_PATTERN = RegistryIds.item(ITEMS, "leaf_blo_pattern", () -> new Item(defaultNonStacking().delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(ActuallyTags.BannerPatterns.PATTERN_LEAF_BLO))));
    public static final DeferredItem<Item> PHAN_CON_PATTERN = RegistryIds.item(ITEMS, "phan_con_pattern", () -> new Item(defaultNonStacking().delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(ActuallyTags.BannerPatterns.PATTERN_PHAN_CON))));
    public static final DeferredItem<Item> BOOK_PATTERN = RegistryIds.item(ITEMS, "book_pattern", () -> new Item(defaultNonStacking().delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(ActuallyTags.BannerPatterns.PATTERN_BOOK))));

    public static final Set<DeferredItem<? extends Item>> SIMPLE_ITEMS = ImmutableSet.of(
        // Crystals
        BLACK_QUARTZ, RESTONIA_CRYSTAL, PALIS_CRYSTAL, DIAMATINE_CRYSTAL,
        VOID_CRYSTAL, EMERADIC_CRYSTAL, ENORI_CRYSTAL, EMPOWERED_RESTONIA_CRYSTAL,
        EMPOWERED_PALIS_CRYSTAL, EMPOWERED_DIAMATINE_CRYSTAL, EMPOWERED_VOID_CRYSTAL, EMPOWERED_EMERADIC_CRYSTAL,
        EMPOWERED_ENORI_CRYSTAL,

        // The rest?
        RESTONIA_CRYSTAL_SHARD, PALIS_CRYSTAL_SHARD, DIAMATINE_CRYSTAL_SHARD, VOID_CRYSTAL_SHARD, EMERADIC_CRYSTAL_SHARD,
        ENORI_CRYSTAL_SHARD, ENGINEERS_GOGGLES, ENGINEERS_GOGGLES_ADVANCED, LASER_UPGRADE_RANGE, LASER_UPGRADE_INVISIBILITY,
        FILTER,
        RING, BASIC_COIL, ADVANCED_COIL, RICE_DOUGH, TINY_COAL, TINY_CHARCOAL, RICE_SLIMEBALL, CANOLA, EMPTY_CUP,
        BATS_WING, DRILL_CORE, LENS, ENDER_STAR, CRYSTALLIZED_CANOLA_SEED, EMPOWERED_CANOLA_SEED, LENS_OF_COLOR,
        LENS_OF_DETONATION, LENS_OF_CERTAIN_DEATH, LENS_OF_THE_KILLER, LENS_OF_DISENCHANTING, LENS_OF_THE_MINER,
        DRILL_UPGRADE_SPEED, DRILL_UPGRADE_SPEED_II, DRILL_UPGRADE_SPEED_III, DRILL_UPGRADE_SILK_TOUCH,
        DRILL_UPGRADE_FORTUNE, DRILL_UPGRADE_FORTUNE_II, DRILL_UPGRADE_THREE_BY_THREE, DRILL_UPGRADE_FIVE_BY_FIVE, DRILL_UPGRADE_BLOCK_PLACING,
        COFFEE_CUP, RICE,
/*        CRUSHED_IRON, CRUSHED_GOLD, CRUSHED_DIAMOND, CRUSHED_EMERALD, CRUSHED_LAPIS,
        CRUSHED_QUARTZ, CRUSHED_COAL, CRUSHED_BLACK_QUARTZ, */
        SOLIDIFIED_EXPERIENCE, ITEM_TAG,
        COFFEE_BEANS, RICE_SEEDS, CANOLA_SEEDS, FLAX_SEEDS, DRILL_PATTERN, LEAF_BLO_PATTERN, PHAN_CON_PATTERN, BOOK_PATTERN
    );

    public static final Set<DeferredItem<? extends Item>> TOOLS = ImmutableSet.of(
            // All in one tools
            WOODEN_AIOT, STONE_AIOT, IRON_AIOT, GOLD_AIOT, DIAMOND_AIOT, NETHERITE_AIOT,
            LEAF_BLOWER, ADVANCED_LEAF_BLOWER, CRAFTER_ON_A_STICK,
            RING_OF_GROWTH, RING_OF_MAGNETIZING, PHANTOM_CONNECTOR,
            LASER_WRENCH, TELEPORT_STAFF, WINGS_OF_THE_BATS, SINGLE_BATTERY, DOUBLE_BATTERY, TRIPLE_BATTERY, QUADRUPLE_BATTERY, QUINTUPLE_BATTERY,
            DRILL_BLACK, DRILL_BLUE, DRILL_BROWN, DRILL_CYAN, DRILL_GRAY, DRILL_GREEN, DRILL_MAIN, DRILL_LIGHT_GRAY,
            DRILL_LIME, DRILL_MAGENTA, DRILL_ORANGE, DRILL_PINK, DRILL_PURPLE, DRILL_RED, DRILL_WHITE, DRILL_YELLOW,
            HANDHELD_FILLER, TRAVELERS_SACK, VOID_SACK, PLAYER_PROBE, WATER_BOWL
            );

    private static Supplier<Item> basicItem() {
        return () -> new Item(defaultProps());
    }
    public static Item.Properties defaultProps() {
        return RegistryIds.withId(new Item.Properties());
    }

    public static Item.Properties defaultNonStacking() {
        return defaultProps().stacksTo(1);
    }

    public static void init(IEventBus evt) {
        ITEMS.register(evt);
        evt.addListener(ActuallyItems::registerCapabilities);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (DeferredHolder<Item, ? extends Item> holder : ITEMS.getEntries()) {
            if (holder.get() instanceof ItemEnergy energyItem) {
                event.registerItem(Capabilities.Energy.ITEM, (stack, access) ->
                        new ItemAccessEnergyHandler(access, ActuallyComponents.ENERGY_STORAGE.get(), energyItem.maxPower, energyItem.transfer),
                        energyItem
                );
            }
        }
    }
}
