package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.block.fluid.ACFluidRegistry;
import com.github.alexmodguy.alexscaves.server.entity.ACEntityRegistry;
import com.github.alexmodguy.alexscaves.server.entity.item.*;
import com.github.alexmodguy.alexscaves.server.entity.util.AlexsCavesBoat;
import com.github.alexmodguy.alexscaves.server.entity.util.GummyColors;
import com.github.alexmodguy.alexscaves.server.item.dispenser.FluidContainerDispenseItemBehavior;
import com.github.alexmodguy.alexscaves.server.level.biome.ACBiomeRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACProjectileDispenseBehavior;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import net.minecraft.core.*;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Mob;

public class ACItemRegistry {
    private static Map<DeferredHolder<Item, ? extends Item>, ResourceKey<Biome>> creativeTabSpawnEggMap = new LinkedHashMap<>();
    
    // Custom item rarities - uses NeoForge extensible enum system
    // Defined in ACRarityEnumParams and registered via META-INF/enumextensions.json
    // Use lazy initialization via getter methods to ensure enum extensions are applied first
    private static Rarity RARITY_DEMONIC_CACHE = null;
    private static Rarity RARITY_NUCLEAR_CACHE = null;
    private static Rarity RARITY_SWEET_CACHE = null;
    private static Rarity RARITY_RAINBOW_CACHE = null;
    
    public static Rarity getRarityDemonic() {
        if (RARITY_DEMONIC_CACHE == null) {
            RARITY_DEMONIC_CACHE = Rarity.valueOf("ALEXSCAVES_DEMONIC");
        }
        return RARITY_DEMONIC_CACHE;
    }
    
    public static Rarity getRarityNuclear() {
        if (RARITY_NUCLEAR_CACHE == null) {
            RARITY_NUCLEAR_CACHE = Rarity.valueOf("ALEXSCAVES_NUCLEAR");
        }
        return RARITY_NUCLEAR_CACHE;
    }
    
    public static Rarity getRaritySweet() {
        if (RARITY_SWEET_CACHE == null) {
            RARITY_SWEET_CACHE = Rarity.valueOf("ALEXSCAVES_SWEET");
        }
        return RARITY_SWEET_CACHE;
    }
    
    public static Rarity getRarityRainbow() {
        if (RARITY_RAINBOW_CACHE == null) {
            RARITY_RAINBOW_CACHE = Rarity.valueOf("ALEXSCAVES_RAINBOW");
        }
        return RARITY_RAINBOW_CACHE;
    }
    
    public static final ACArmorMaterial PRIMORDIAL_ARMOR_MATERIAL = new ACArmorMaterial("primordial", 20, new int[]{3, 4, 3, 2}, 25, SoundEvents.ARMOR_EQUIP_LEATHER, 0F);
    public static final ACArmorMaterial HAZMAT_SUIT_ARMOR_MATERIAL = new ACArmorMaterial("hazmat_suit", 20, new int[]{2, 4, 5, 2}, 25, SoundEvents.ARMOR_EQUIP_IRON, 0.5F);
    public static final ACArmorMaterial DIVING_SUIT_ARMOR_MATERIAL = new ACArmorMaterial("diving_suit", 20, new int[]{2, 6, 5, 2}, 25, SoundEvents.ARMOR_EQUIP_IRON, 0.0F);
    public static final ACArmorMaterial DARKNESS_ARMOR_MATERIAL = new ACArmorMaterial("darkness", 15, new int[]{4, 5, 1, 1}, 40, SoundEvents.ARMOR_EQUIP_LEATHER, 0.5F);
    public static final ACArmorMaterial RAINBOUNCE_ARMOR_MATERIAL = new ACArmorMaterial("rainbounce", 6, new int[]{2, 2, 1, 2}, 40, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F);
    public static final ACArmorMaterial GINGERBREAD_ARMOR_MATERIAL = new ACArmorMaterial("gingerbread", 10, new int[]{2, 4, 5, 2}, 25, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F);
    public static final DeferredRegister<Item> DEF_REG = DeferredRegister.create(Registries.ITEM, AlexsCaves.MODID);

    public static <I extends Item> DeferredHolder<Item, I> reg(String name, java.util.function.Supplier<I> supplier) {
        return DEF_REG.register(name, () -> {
            com.github.alexmodguy.alexscaves.server.misc.ACProps.setItem(name);
            try { return supplier.get(); } finally { com.github.alexmodguy.alexscaves.server.misc.ACProps.setItem(null); }
        });
    }
    public static final DeferredHolder<Item, Item> ADVANCEMENT_TAB_ICON = reg("advancement_tab_icon", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> CAVE_TABLET = reg("cave_tablet", () -> new CaveInfoItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item(), true));
    public static final DeferredHolder<Item, Item> CAVE_CODEX = reg("cave_codex", () -> new CaveInfoItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item(), false));
    public static final DeferredHolder<Item, Item> CAVE_BOOK = reg("cave_book", () -> new CaveBookItem());
    public static final DeferredHolder<Item, Item> CAVE_MAP = reg("cave_map", () -> new CaveMapItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(1)));
    public static final DeferredHolder<Item, Item> CAVE_MAP_SPRITE = reg("cave_map_inventory", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> CAVE_MAP_LOADING_SPRITE = reg("cave_map_loading", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> CAVE_MAP_FILLED_SPRITE = reg("cave_map_filled", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> RAW_SCARLET_NEODYMIUM = reg("raw_scarlet_neodymium", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> RAW_AZURE_NEODYMIUM = reg("raw_azure_neodymium", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> SCARLET_NEODYMIUM_INGOT = reg("scarlet_neodymium_ingot", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> AZURE_NEODYMIUM_INGOT = reg("azure_neodymium_ingot", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> TELECORE = reg("telecore", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> NOTOR_COMPONENT = reg("notor_gizmo", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> HEAVYWEIGHT = reg("heavyweight", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> FERROUSLIME_BALL = reg("ferrouslime_ball", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> QUARRY_SMASHER = reg("quarry_smasher", () -> new QuarrySmasherItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(1)));
    public static final DeferredHolder<Item, Item> HOLOCODER = reg("holocoder", () -> new HolocoderItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(1)));
    public static final DeferredHolder<Item, Item> SEEKING_ARROW = reg("seeking_arrow", () -> new SeekingArrowItem());
    public static final DeferredHolder<Item, Item> GALENA_GAUNTLET = reg("galena_gauntlet", () -> new GalenaGauntletItem());
    public static final DeferredHolder<Item, Item> RESISTOR_SHIELD = reg("resistor_shield", () -> new ResistorShieldItem());
    public static final DeferredHolder<Item, Item> POLARITY_ARMOR_TRIM_SMITHING_TEMPLATE = reg("polarity_armor_trim_smithing_template", () -> SmithingTemplateItem.createArmorTrimTemplate(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> PEWEN_DOOR = reg("pewen_door", () -> new DoubleHighBlockItem(ACBlockRegistry.PEWEN_DOOR.get(), (com.github.alexmodguy.alexscaves.server.misc.ACProps.item())));
    public static final DeferredHolder<Item, Item> PEWEN_SIGN = reg("pewen_sign", () -> new SignItem(ACBlockRegistry.PEWEN_SIGN.get(), ACBlockRegistry.PEWEN_WALL_SIGN.get(), com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(16).useBlockDescriptionPrefix()));
    public static final DeferredHolder<Item, Item> PEWEN_HANGING_SIGN = reg("pewen_hanging_sign", () -> new HangingSignItem(ACBlockRegistry.PEWEN_HANGING_SIGN.get(), ACBlockRegistry.PEWEN_WALL_HANGING_SIGN.get(), (com.github.alexmodguy.alexscaves.server.misc.ACProps.item()).stacksTo(16)));
    public static final DeferredHolder<Item, Item> PEWEN_BOAT = reg("pewen_boat", () -> new CaveBoatItem(false, AlexsCavesBoat.Type.PEWEN, com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(1)));
    public static final DeferredHolder<Item, Item> PEWEN_CHEST_BOAT = reg("pewen_chest_boat", () -> new CaveBoatItem(true, AlexsCavesBoat.Type.PEWEN, com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(1)));
    public static final DeferredHolder<Item, Item> TRILOCARIS_BUCKET = reg("trilocaris_bucket", () -> new ModFishBucketItem(ACEntityRegistry.TRILOCARIS, () -> Fluids.WATER, com.github.alexmodguy.alexscaves.server.misc.ACProps.item().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredHolder<Item, Item> TRILOCARIS_TAIL = reg("trilocaris_tail", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.TRILOCARIS_TAIL.food(), ACFoods.TRILOCARIS_TAIL.consumable())));
    public static final DeferredHolder<Item, Item> COOKED_TRILOCARIS_TAIL = reg("cooked_trilocaris_tail", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.TRILOCARIS_TAIL_COOKED.food(), ACFoods.TRILOCARIS_TAIL_COOKED.consumable())));
    public static final DeferredHolder<Item, Item> PINE_NUTS = reg("pine_nuts", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.PINE_NUTS.food(), ACFoods.PINE_NUTS.consumable())));
    public static final DeferredHolder<Item, Item> PEWEN_SAP = reg("pewen_sap", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> AMBER_CURIOSITY = reg("amber_curiosity", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> DINOSAUR_NUGGET = reg("dinosaur_nugget", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.DINOSAUR_NUGGETS.food(), ACFoods.DINOSAUR_NUGGETS.consumable())));
    public static final DeferredHolder<Item, Item> SERENE_SALAD = reg("serene_salad", () -> new PrehistoricMixtureItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(1).food(ACFoods.SERENE_SALAD.food(), ACFoods.SERENE_SALAD.consumable())));
    public static final DeferredHolder<Item, Item> SEETHING_STEW = reg("seething_stew", () -> new PrehistoricMixtureItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(1).food(ACFoods.SEETHING_STEW.food(), ACFoods.SEETHING_STEW.consumable())));
    public static final DeferredHolder<Item, Item> PRIMORDIAL_SOUP = reg("primordial_soup", () -> new PrehistoricMixtureItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(1).food(ACFoods.PRIMORDIAL_SOUP.food(), ACFoods.PRIMORDIAL_SOUP.consumable())));
    public static final DeferredHolder<Item, Item> TOUGH_HIDE = reg("tough_hide", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> HEAVY_BONE = reg("heavy_bone", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(16)));
    public static final DeferredHolder<Item, Item> PRIMITIVE_CLUB = reg("primitive_club", () -> new PrimitiveClubItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().durability(120)));
    public static final DeferredHolder<Item, Item> PRIMITIVE_CLUB_SPRITE = reg("primitive_club_inventory", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> PRIMORDIAL_HELMET = reg("primordial_helmet", () -> new PrimordialArmorItem(PRIMORDIAL_ARMOR_MATERIAL, net.minecraft.world.item.equipment.ArmorType.HELMET));
    public static final DeferredHolder<Item, Item> PRIMORDIAL_TUNIC = reg("primordial_tunic", () -> new PrimordialArmorItem(PRIMORDIAL_ARMOR_MATERIAL, net.minecraft.world.item.equipment.ArmorType.CHESTPLATE));
    public static final DeferredHolder<Item, Item> PRIMORDIAL_PANTS = reg("primordial_pants", () -> new PrimordialArmorItem(PRIMORDIAL_ARMOR_MATERIAL, net.minecraft.world.item.equipment.ArmorType.LEGGINGS));
    public static final DeferredHolder<Item, Item> LIMESTONE_SPEAR = reg("limestone_spear", () -> new LimestoneSpearItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(16)));
    public static final DeferredHolder<Item, Item> LIMESTONE_SPEAR_SPRITE = reg("limestone_spear_inventory", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> OMINOUS_CATALYST = reg("ominous_catalyst", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().rarity(Rarity.UNCOMMON).fireResistant()));
    public static final DeferredHolder<Item, Item> TECTONIC_SHARD = reg("tectonic_shard", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().rarity(getRarityDemonic()).fireResistant()));
    public static final DeferredHolder<Item, Item> EXTINCTION_SPEAR = reg("extinction_spear", () -> new ExtinctionSpearItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().durability(1300).rarity(getRarityDemonic()).fireResistant()));
    public static final DeferredHolder<Item, Item> EXTINCTION_SPEAR_SPRITE = reg("extinction_spear_inventory", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> DINOSAUR_POTTERY_SHERD = reg("dinosaur_pottery_sherd", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> FOOTPRINT_POTTERY_SHERD = reg("footprint_pottery_sherd", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> DINOSAUR_TRAIN = reg("dinosaur_train", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> ACID_BUCKET = reg("acid_bucket", () -> new BucketItem(ACFluidRegistry.ACID_FLUID_SOURCE.get(), com.github.alexmodguy.alexscaves.server.misc.ACProps.item().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredHolder<Item, Item> RADGILL_BUCKET = reg("radgill_bucket", () -> new ModFishBucketItem(ACEntityRegistry.RADGILL, ACFluidRegistry.ACID_FLUID_SOURCE::get, com.github.alexmodguy.alexscaves.server.misc.ACProps.item().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredHolder<Item, Item> RADGILL = reg("radgill", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.RADGILL.food(), ACFoods.RADGILL.consumable())));
    public static final DeferredHolder<Item, Item> COOKED_RADGILL = reg("cooked_radgill", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.RADGILL_COOKED.food(), ACFoods.RADGILL_COOKED.consumable())));
    public static final DeferredHolder<Item, Item> URANIUM = reg("uranium", () -> new RadioactiveItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item(), 0.001F));
    public static final DeferredHolder<Item, Item> URANIUM_SHARD = reg("uranium_shard", () -> new RadioactiveItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item(), 0.001F));
    public static final DeferredHolder<Item, Item> SULFUR_DUST = reg("sulfur_dust", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> RADON_BOTTLE = reg("radon_bottle", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().craftRemainder(Items.GLASS_BOTTLE).stacksTo(16)));
    public static final DeferredHolder<Item, Item> CINDER_BRICK = reg("cinder_brick", () -> new ThrownProjectileItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item(), player -> new CinderBrickEntity(player.level(), player), -20.0F, 0.65F, 0.9F));
    public static final DeferredHolder<Item, Item> SPELUNKIE = reg("spelunkie", () -> new RadiationRemovingFoodItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.SPELUNKIE.food(), ACFoods.SPELUNKIE.consumable())));
    public static final DeferredHolder<Item, Item> SLAM = reg("slam", () -> new RadiationRemovingFoodItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.SLAM.food(), ACFoods.SLAM.consumable())));
    public static final DeferredHolder<Item, Item> GREEN_SOYLENT = reg("green_soylent", () -> new RadiationRemovingFoodItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.SOYLENT_GREEN.food(), ACFoods.SOYLENT_GREEN.consumable())));
    public static final DeferredHolder<Item, Item> TOXIC_PASTE = reg("toxic_paste", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> POLYMER_PLATE = reg("polymer_plate", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> HAZMAT_MASK = reg("hazmat_mask", () -> new HazmatArmorItem(HAZMAT_SUIT_ARMOR_MATERIAL, net.minecraft.world.item.equipment.ArmorType.HELMET));
    public static final DeferredHolder<Item, Item> HAZMAT_CHESTPLATE = reg("hazmat_chestplate", () -> new HazmatArmorItem(HAZMAT_SUIT_ARMOR_MATERIAL, net.minecraft.world.item.equipment.ArmorType.CHESTPLATE));
    public static final DeferredHolder<Item, Item> HAZMAT_LEGGINGS = reg("hazmat_leggings", () -> new HazmatArmorItem(HAZMAT_SUIT_ARMOR_MATERIAL, net.minecraft.world.item.equipment.ArmorType.LEGGINGS));
    public static final DeferredHolder<Item, Item> HAZMAT_BOOTS = reg("hazmat_boots", () -> new HazmatArmorItem(HAZMAT_SUIT_ARMOR_MATERIAL, net.minecraft.world.item.equipment.ArmorType.BOOTS));
    public static final DeferredHolder<Item, Item> FISSILE_CORE = reg("fissile_core", () -> new RadioactiveItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().rarity(Rarity.UNCOMMON), 0.001F));
    public static final DeferredHolder<Item, Item> CHARRED_REMNANT = reg("charred_remnant", () -> new RadioactiveItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item(), 0.0005F));
    public static final DeferredHolder<Item, Item> REMOTE_DETONATOR = reg("remote_detonator", () -> new RemoteDetonatorItem());
    public static final DeferredHolder<Item, Item> RAYGUN = reg("raygun", () -> new RaygunItem());
    public static final DeferredHolder<Item, Item> MUSIC_DISC_FUSION_FRAGMENT = reg("disc_fragment_fusion", () -> new DiscFragmentItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    // Music discs - in 1.21+, use jukeboxPlayable component in item properties
    // The jukebox songs are data-driven and defined in data/alexscaves/jukebox_song/
    public static final ResourceKey<JukeboxSong> JUKEBOX_SONG_FUSION = ResourceKey.create(Registries.JUKEBOX_SONG, Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "fusion"));
    public static final DeferredHolder<Item, Item> MUSIC_DISC_FUSION = reg("music_disc_fusion", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(1).rarity(getRarityNuclear()).jukeboxPlayable(JUKEBOX_SONG_FUSION)));
    public static final DeferredHolder<Item, Item> LANTERNFISH_BUCKET = reg("lanternfish_bucket", () -> new ModFishBucketItem(ACEntityRegistry.LANTERNFISH, () -> Fluids.WATER, com.github.alexmodguy.alexscaves.server.misc.ACProps.item().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredHolder<Item, Item> LANTERNFISH = reg("lanternfish", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.LANTERNFISH.food(), ACFoods.LANTERNFISH.consumable())));
    public static final DeferredHolder<Item, Item> COOKED_LANTERNFISH = reg("cooked_lanternfish", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.LANTERNFISH_COOKED.food(), ACFoods.LANTERNFISH_COOKED.consumable())));
    public static final DeferredHolder<Item, Item> TRIPODFISH_BUCKET = reg("tripodfish_bucket", () -> new ModFishBucketItem(ACEntityRegistry.TRIPODFISH, () -> Fluids.WATER, com.github.alexmodguy.alexscaves.server.misc.ACProps.item().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredHolder<Item, Item> TRIPODFISH = reg("tripodfish", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.TRIPODFISH.food(), ACFoods.TRIPODFISH.consumable())));
    public static final DeferredHolder<Item, Item> COOKED_TRIPODFISH = reg("cooked_tripodfish", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.TRIPODFISH_COOKED.food(), ACFoods.TRIPODFISH_COOKED.consumable())));
    public static final DeferredHolder<Item, Item> SEA_PIG_BUCKET = reg("sea_pig_bucket", () -> new ModFishBucketItem(ACEntityRegistry.SEA_PIG, () -> Fluids.WATER, com.github.alexmodguy.alexscaves.server.misc.ACProps.item().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredHolder<Item, Item> SEA_PIG = reg("sea_pig", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.SEA_PIG.food(), ACFoods.SEA_PIG.consumable())));
    public static final DeferredHolder<Item, Item> MARINE_SNOW = reg("marine_snow", () -> new MarineSnowItem());
    public static final DeferredHolder<Item, Item> GOSSAMER_WORM_BUCKET = reg("gossamer_worm_bucket", () -> new ModFishBucketItem(ACEntityRegistry.GOSSAMER_WORM, () -> Fluids.WATER, com.github.alexmodguy.alexscaves.server.misc.ACProps.item().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredHolder<Item, Item> BIOLUMINESSCENCE = reg("bioluminesscence", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> PEARL = reg("pearl", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> COOKED_MUSSEL = reg("cooked_mussel", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.MUSSEL_COOKED.food(), ACFoods.MUSSEL_COOKED.consumable())));
    public static final DeferredHolder<Item, Item> DEEP_SEA_SUSHI_ROLL = reg("deep_sea_sushi_roll", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.DEEP_SEA_SUSHI_ROLL.food(), ACFoods.DEEP_SEA_SUSHI_ROLL.consumable())));
    public static final DeferredHolder<Item, Item> SEA_GLASS_SHARDS = reg("sea_glass_shards", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> SUBMARINE = reg("submarine", () -> new SubmarineItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> DIVING_HELMET = reg("diving_helmet", () -> new DivingArmorItem(DIVING_SUIT_ARMOR_MATERIAL, net.minecraft.world.item.equipment.ArmorType.HELMET));
    public static final DeferredHolder<Item, Item> DIVING_CHESTPLATE = reg("diving_chestplate", () -> new DivingArmorItem(DIVING_SUIT_ARMOR_MATERIAL, net.minecraft.world.item.equipment.ArmorType.CHESTPLATE));
    public static final DeferredHolder<Item, Item> DIVING_LEGGINGS = reg("diving_leggings", () -> new DivingArmorItem(DIVING_SUIT_ARMOR_MATERIAL, net.minecraft.world.item.equipment.ArmorType.LEGGINGS));
    public static final DeferredHolder<Item, Item> DIVING_BOOTS = reg("diving_boots", () -> new DivingArmorItem(DIVING_SUIT_ARMOR_MATERIAL, net.minecraft.world.item.equipment.ArmorType.BOOTS));
    public static final DeferredHolder<Item, Item> FLOATER = reg("floater", () -> new FloaterItem());
    public static final DeferredHolder<Item, Item> GAZING_PEARL = reg("gazing_pearl", () -> new GazingPearlItem());
    public static final DeferredHolder<Item, Item> INK_BOMB = reg("ink_bomb", () -> new InkBombItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item(), false));
    public static final DeferredHolder<Item, Item> GLOW_INK_BOMB = reg("glow_ink_bomb", () -> new InkBombItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item(), true));
    public static final DeferredHolder<Item, Item> MAGIC_CONCH = reg("magic_conch", () -> new MagicConchItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().durability(5).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> SEA_STAFF = reg("sea_staff", () -> new SeaStaffItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().durability(850).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> SEA_STAFF_SPRITE = reg("sea_staff_inventory", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> ORTHOLANCE = reg("ortholance", () -> new OrtholanceItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().durability(340).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> ORTHOLANCE_SPRITE = reg("ortholance_inventory", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> DEPTH_CHARGE = reg("depth_charge", () -> new ThrownProjectileItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item(), player -> new DepthChargeEntity(player.level(), player), -10.0F, 0.65F, 1.5F));
    public static final DeferredHolder<Item, Item> GUARDIAN_POTTERY_SHERD = reg("guardian_pottery_sherd", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> HERO_POTTERY_SHERD = reg("hero_pottery_sherd", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> BIOLUMINESCENT_TORCH = reg("bioluminescent_torch", () -> new StandingAndWallBlockItem(ACBlockRegistry.BIOLUMINESCENT_TORCH.get(), ACBlockRegistry.BIOLUMINESCENT_WALL_TORCH.get(), Direction.DOWN, com.github.alexmodguy.alexscaves.server.misc.ACProps.item().useBlockDescriptionPrefix()));
    public static final DeferredHolder<Item, Item> GAME_CONTROLLER = reg("game_controller", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> STINKY_FISH = reg("stinky_fish", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().rarity(Rarity.RARE).food(ACFoods.STINKY_FISH.food(), ACFoods.STINKY_FISH.consumable())));
    public static final DeferredHolder<Item, Item> IMMORTAL_EMBRYO = reg("immortal_embryo", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> GUANO = reg("guano", () -> new ThrownProjectileItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item(), player -> new GuanoEntity(player.level(), player), 0.0F, 1.0F, 1.0F));
    public static final DeferredHolder<Item, Item> MOTH_DUST = reg("moth_dust", () -> new MothDustItem());
    public static final DeferredHolder<Item, Item> FERTILIZER = reg("fertilizer", () -> new FertilizerItem());
    public static final DeferredHolder<Item, Item> DARK_TATTERS = reg("dark_tatters", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> OCCULT_GEM = reg("occult_gem", () -> new OccultGemItem());
    public static final DeferredHolder<Item, Item> TOTEM_OF_POSSESSION = reg("totem_of_possession", () -> new TotemOfPossessionItem());
    public static final DeferredHolder<Item, Item> DESOLATE_DAGGER = reg("desolate_dagger", () -> new DesolateDaggerItem());
    public static final DeferredHolder<Item, Item> CORRODENT_TEETH = reg("corrodent_teeth", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> BURROWING_ARROW = reg("burrowing_arrow", () -> new BurrowingArrowItem());
    public static final DeferredHolder<Item, Item> VESPER_WING = reg("vesper_wing", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.VESPER_WING.food(), ACFoods.VESPER_WING.consumable())));
    public static final DeferredHolder<Item, Item> VESPER_STEW = reg("vesper_stew", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.VESPER_SOUP.food(), ACFoods.VESPER_SOUP.consumable()).stacksTo(1)));
    public static final DeferredHolder<Item, Item> PURE_DARKNESS = reg("pure_darkness", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().rarity(getRarityDemonic())));
    public static final DeferredHolder<Item, Item> SHADOW_SILK = reg("shadow_silk", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> HOOD_OF_DARKNESS = reg("hood_of_darkness", () -> new DarknessArmorItem(DARKNESS_ARMOR_MATERIAL, net.minecraft.world.item.equipment.ArmorType.HELMET));
    public static final DeferredHolder<Item, Item> CLOAK_OF_DARKNESS = reg("cloak_of_darkness", () -> new DarknessArmorItem(DARKNESS_ARMOR_MATERIAL, net.minecraft.world.item.equipment.ArmorType.CHESTPLATE));
    public static final DeferredHolder<Item, Item> DARKENED_APPLE = reg("darkened_apple", () -> new DarkenedAppleItem());
    public static final DeferredHolder<Item, Item> DREADBOW = reg("dreadbow", () -> new DreadbowItem());
    public static final DeferredHolder<Item, Item> DREADBOW_SPRITE = reg("dreadbow_inventory", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> DREADBOW_PULLING_0_SPRITE = reg("dreadbow_pulling_0_inventory", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> DREADBOW_PULLING_1_SPRITE = reg("dreadbow_pulling_1_inventory", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> DREADBOW_PULLING_2_SPRITE = reg("dreadbow_pulling_2_inventory", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> THORNWOOD_DOOR = reg("thornwood_door", () -> new DoubleHighBlockItem(ACBlockRegistry.THORNWOOD_DOOR.get(), (com.github.alexmodguy.alexscaves.server.misc.ACProps.item())));
    public static final DeferredHolder<Item, Item> THORNWOOD_SIGN = reg("thornwood_sign", () -> new SignItem(ACBlockRegistry.THORNWOOD_SIGN.get(), ACBlockRegistry.THORNWOOD_WALL_SIGN.get(), com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(16).useBlockDescriptionPrefix()));
    public static final DeferredHolder<Item, Item> THORNWOOD_HANGING_SIGN = reg("thornwood_hanging_sign", () -> new HangingSignItem(ACBlockRegistry.THORNWOOD_HANGING_SIGN.get(), ACBlockRegistry.THORNWOOD_WALL_HANGING_SIGN.get(), (com.github.alexmodguy.alexscaves.server.misc.ACProps.item()).stacksTo(16)));
    public static final DeferredHolder<Item, Item> THORNWOOD_BOAT = reg("thornwood_boat", () -> new CaveBoatItem(false, AlexsCavesBoat.Type.THORNWOOD, com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(1)));
    public static final DeferredHolder<Item, Item> THORNWOOD_CHEST_BOAT = reg("thornwood_chest_boat", () -> new CaveBoatItem(true, AlexsCavesBoat.Type.THORNWOOD, com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(1)));
    public static final DeferredHolder<Item, Item> PURPLE_SODA_BUCKET = reg("purple_soda_bucket", () -> new BucketItem(ACFluidRegistry.PURPLE_SODA_FLUID_SOURCE.get(), com.github.alexmodguy.alexscaves.server.misc.ACProps.item().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredHolder<Item, Item> PURPLE_SODA_BOTTLE = reg("purple_soda_bottle", () -> new DrinkableBottledItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(16).food(ACFoods.PURPLE_SODA_BOTTLE.food(), ACFoods.PURPLE_SODA_BOTTLE.consumable())));
    public static final DeferredHolder<Item, Item> SWEETISH_FISH_RED_BUCKET = reg("sweetish_fish_red_bucket", () -> new SweetishFishBucketItem(GummyColors.RED));
    public static final DeferredHolder<Item, Item> SWEETISH_FISH_GREEN_BUCKET = reg("sweetish_fish_green_bucket", () -> new SweetishFishBucketItem(GummyColors.GREEN));
    public static final DeferredHolder<Item, Item> SWEETISH_FISH_BLUE_BUCKET = reg("sweetish_fish_blue_bucket", () -> new SweetishFishBucketItem(GummyColors.BLUE));
    public static final DeferredHolder<Item, Item> SWEETISH_FISH_YELLOW_BUCKET = reg("sweetish_fish_yellow_bucket", () -> new SweetishFishBucketItem(GummyColors.YELLOW));
    public static final DeferredHolder<Item, Item> SWEETISH_FISH_PINK_BUCKET = reg("sweetish_fish_pink_bucket", () -> new SweetishFishBucketItem(GummyColors.PINK));
    public static final DeferredHolder<Item, Item> SWEETISH_FISH_RED = reg("sweetish_fish_red", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.SWEETISH_FISH.food(), ACFoods.SWEETISH_FISH.consumable())));
    public static final DeferredHolder<Item, Item> SWEETISH_FISH_GREEN = reg("sweetish_fish_green", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.SWEETISH_FISH.food(), ACFoods.SWEETISH_FISH.consumable())));
    public static final DeferredHolder<Item, Item> SWEETISH_FISH_BLUE = reg("sweetish_fish_blue", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.SWEETISH_FISH.food(), ACFoods.SWEETISH_FISH.consumable())));
    public static final DeferredHolder<Item, Item> SWEETISH_FISH_YELLOW = reg("sweetish_fish_yellow", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.SWEETISH_FISH.food(), ACFoods.SWEETISH_FISH.consumable())));
    public static final DeferredHolder<Item, Item> SWEETISH_FISH_PINK = reg("sweetish_fish_pink", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.SWEETISH_FISH.food(), ACFoods.SWEETISH_FISH.consumable())));
    public static final DeferredHolder<Item, Item> GELATIN_RED = reg("gelatin_red", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.GELATIN.food(), ACFoods.GELATIN.consumable())));
    public static final DeferredHolder<Item, Item> GELATIN_GREEN = reg("gelatin_green", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.GELATIN.food(), ACFoods.GELATIN.consumable())));
    public static final DeferredHolder<Item, Item> GELATIN_BLUE = reg("gelatin_blue", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.GELATIN.food(), ACFoods.GELATIN.consumable())));
    public static final DeferredHolder<Item, Item> GELATIN_YELLOW = reg("gelatin_yellow", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.GELATIN.food(), ACFoods.GELATIN.consumable())));
    public static final DeferredHolder<Item, Item> GELATIN_PINK = reg("gelatin_pink", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.GELATIN.food(), ACFoods.GELATIN.consumable())));
    public static final DeferredHolder<Item, Item> HOT_CHOCOLATE_BOTTLE = reg("hot_chocolate_bottle", () -> new HotChocolateBottleItem());
    public static final DeferredHolder<Item, Item> VANILLA_ICE_CREAM_SCOOP = reg("vanilla_ice_cream_scoop", () -> new ThrownProjectileItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item(), player -> new ThrownIceCreamScoopEntity(player.level(), player), -10.0F, 1.0F, 0.2F));
    public static final DeferredHolder<Item, Item> CHOCOLATE_ICE_CREAM_SCOOP = reg("chocolate_ice_cream_scoop", () -> new ThrownProjectileItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item(), player -> new ThrownIceCreamScoopEntity(player.level(), player), -10.0F, 1.0F, 0.2F));
    public static final DeferredHolder<Item, Item> SWEETBERRY_ICE_CREAM_SCOOP = reg("sweetberry_ice_cream_scoop", () -> new ThrownProjectileItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item(), player -> new ThrownIceCreamScoopEntity(player.level(), player), -10.0F, 1.0F, 0.2F));
    public static final DeferredHolder<Item, Item> SUNDAE = reg("sundae", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.SUNDAE.food(), ACFoods.SUNDAE.consumable()).rarity(getRaritySweet()).stacksTo(1)));
    public static final DeferredHolder<Item, Item> SHARPENED_CANDY_CANE = reg("sharpened_candy_cane", () -> new SharpenedCandyCaneItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.CANDY_CANE.food(), ACFoods.CANDY_CANE.consumable())));
    public static final DeferredHolder<Item, Item> PEPPERMINT_POWDER = reg("peppermint_powder", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.PEPPERMINT_POWDER.food(), ACFoods.PEPPERMINT_POWDER.consumable())));
    public static final DeferredHolder<Item, Item> RAINBOUNCE_BOOTS = reg("rainbounce_boots", () -> new RainbounceBootsItem(RAINBOUNCE_ARMOR_MATERIAL));
    public static final DeferredHolder<Item, Item> GUMBALL_PILE = reg("gumball_pile", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.GUMBALL_PILE.food(), ACFoods.GUMBALL_PILE.consumable())));
    public static final DeferredHolder<Item, Item> SHOT_GUM = reg("shot_gum", () -> new ShotGumItem());
    public static final DeferredHolder<Item, Item> CARAMEL = reg("caramel", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.CARAMEL.food(), ACFoods.CARAMEL.consumable())));
    public static final DeferredHolder<Item, Item> CARAMEL_APPLE = reg("caramel_apple", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.CARAMEL_APPLE.food(), ACFoods.CARAMEL_APPLE.consumable())));
    public static final DeferredHolder<Item, Item> CANDY_CANE_HOOK = reg("candy_cane_hook", () -> new CandyCaneHookItem());
    public static final DeferredHolder<Item, Item> SWEET_TOOTH = reg("sweet_tooth", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().rarity(getRaritySweet())));
    public static final DeferredHolder<Item, Item> RADIANT_ESSENCE = reg("radiant_essence", () -> new RadiantEssenceItem());
    public static final DeferredHolder<Item, Item> LICOWITCH_RADIANT_ESSENCE = reg("licowitch_radiant_essence", () -> new RadiantEssenceItem());
    public static final DeferredHolder<Item, Item> SACK_OF_SATING = reg("sack_of_sating", () -> new SackOfSatingItem());
    public static final DeferredHolder<Item, Item> SUGAR_STAFF = reg("sugar_staff", () -> new SugarStaffItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().durability(100).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> SUGAR_STAFF_SPRITE = reg("sugar_staff_inventory", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> GINGERBREAD_CRUMBS = reg("gingerbread_crumbs", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.GINGERBREAD_CRUMBS.food(), ACFoods.GINGERBREAD_CRUMBS.consumable())));
    public static final DeferredHolder<Item, Item> GINGERBREAD_HELMET = reg("gingerbread_helmet", () -> new GingerbreadArmorItem(GINGERBREAD_ARMOR_MATERIAL, net.minecraft.world.item.equipment.ArmorType.HELMET));
    public static final DeferredHolder<Item, Item> GINGERBREAD_CHESTPLATE = reg("gingerbread_chestplate", () -> new GingerbreadArmorItem(GINGERBREAD_ARMOR_MATERIAL, net.minecraft.world.item.equipment.ArmorType.CHESTPLATE));
    public static final DeferredHolder<Item, Item> GINGERBREAD_LEGGINGS = reg("gingerbread_leggings", () -> new GingerbreadArmorItem(GINGERBREAD_ARMOR_MATERIAL, net.minecraft.world.item.equipment.ArmorType.LEGGINGS));
    public static final DeferredHolder<Item, Item> GINGERBREAD_BOOTS = reg("gingerbread_boots", () -> new GingerbreadArmorItem(GINGERBREAD_ARMOR_MATERIAL, net.minecraft.world.item.equipment.ArmorType.BOOTS));
    public static final DeferredHolder<Item, Item> PURPLE_SODA_BOTTLE_ROCKET = reg("purple_soda_bottle_rocket", () -> new SodaBottleRocketItem());
    public static final DeferredHolder<Item, Item> FROSTMINT_SPEAR = reg("frostmint_spear", () -> new FrostmintSpearItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(16)));
    public static final DeferredHolder<Item, Item> FROSTMINT_SPEAR_SPRITE = reg("frostmint_spear_inventory", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final DeferredHolder<Item, Item> MUSIC_DISC_TASTY_FRAGMENT = reg("disc_fragment_tasty", () -> new DiscFragmentItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item()));
    public static final ResourceKey<JukeboxSong> JUKEBOX_SONG_TASTY = ResourceKey.create(Registries.JUKEBOX_SONG, Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "tasty"));
    public static final DeferredHolder<Item, Item> MUSIC_DISC_TASTY = reg("music_disc_tasty", () -> new Item(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(1).rarity(getRaritySweet()).jukeboxPlayable(JUKEBOX_SONG_TASTY)));
    public static final DeferredHolder<Item, Item> ALEX_MEAL = reg("alex_meal", () -> new AlexMealItem());
    public static final DeferredHolder<Item, Item> BIOME_TREAT = reg("biome_treat", () -> new BiomeTreatItem());
    public static final DeferredHolder<Item, Item> JELLY_BEAN = reg("jelly_bean", () -> new JellyBeanItem());

    static {
        spawnEgg("teletor", ACEntityRegistry.TELETOR, 0X433B4A, 0X0060EF, ACBiomeRegistry.MAGNETIC_CAVES);
        spawnEgg("magnetron", ACEntityRegistry.MAGNETRON, 0XFF002A, 0X203070, ACBiomeRegistry.MAGNETIC_CAVES);
        spawnEgg("boundroid", ACEntityRegistry.BOUNDROID, 0XBB1919, 0XFFFFFF, ACBiomeRegistry.MAGNETIC_CAVES);
        spawnEgg("ferrouslime", ACEntityRegistry.FERROUSLIME, 0X26272D, 0X53556C, ACBiomeRegistry.MAGNETIC_CAVES);
        spawnEgg("notor", ACEntityRegistry.NOTOR, 0X5F5369, 0XC6C6C6, ACBiomeRegistry.MAGNETIC_CAVES);
        spawnEgg("subterranodon", ACEntityRegistry.SUBTERRANODON, 0X00B1B2, 0XFFF11C, ACBiomeRegistry.PRIMORDIAL_CAVES);
        spawnEgg("vallumraptor", ACEntityRegistry.VALLUMRAPTOR, 0X22389A, 0XEEE5AB, ACBiomeRegistry.PRIMORDIAL_CAVES);
        spawnEgg("grottoceratops", ACEntityRegistry.GROTTOCERATOPS, 0XAC3B03, 0XD39B4E, ACBiomeRegistry.PRIMORDIAL_CAVES);
        spawnEgg("trilocaris", ACEntityRegistry.TRILOCARIS, 0X713E0D, 0X8B2010, ACBiomeRegistry.PRIMORDIAL_CAVES);
        spawnEgg("tremorsaurus", ACEntityRegistry.TREMORSAURUS, 0X53780E, 0XDFA211, ACBiomeRegistry.PRIMORDIAL_CAVES);
        spawnEgg("relicheirus", ACEntityRegistry.RELICHEIRUS, 0X6AE4F9, 0X5B2152, ACBiomeRegistry.PRIMORDIAL_CAVES);
        spawnEgg("luxtructosaurus", ACEntityRegistry.LUXTRUCTOSAURUS, 0X1F0E15, 0XB30C03, ACBiomeRegistry.PRIMORDIAL_CAVES);
        spawnEgg("atlatitan", ACEntityRegistry.ATLATITAN, 0XB67000, 0XBFBAA4, ACBiomeRegistry.PRIMORDIAL_CAVES);
        spawnEgg("nucleeper", ACEntityRegistry.NUCLEEPER, 0X95A1A5, 0X00FF00, ACBiomeRegistry.TOXIC_CAVES);
        spawnEgg("radgill", ACEntityRegistry.RADGILL, 0X43302C, 0XE8E400, ACBiomeRegistry.TOXIC_CAVES);
        spawnEgg("brainiac", ACEntityRegistry.BRAINIAC, 0X3E5136, 0XE87C9E, ACBiomeRegistry.TOXIC_CAVES);
        spawnEgg("gammaroach", ACEntityRegistry.GAMMAROACH, 0X56682A, 0X2A2B19, ACBiomeRegistry.TOXIC_CAVES);
        spawnEgg("raycat", ACEntityRegistry.RAYCAT, 0X67FF00, 0X030A00, ACBiomeRegistry.TOXIC_CAVES);
        spawnEgg("tremorzilla", ACEntityRegistry.TREMORZILLA, 0X574D2F, 0X8CFF08, ACBiomeRegistry.TOXIC_CAVES);
        spawnEgg("lanternfish", ACEntityRegistry.LANTERNFISH, 0X182538, 0XECA500, ACBiomeRegistry.ABYSSAL_CHASM);
        spawnEgg("sea_pig", ACEntityRegistry.SEA_PIG, 0XFFA3B9, 0XF88672, ACBiomeRegistry.ABYSSAL_CHASM);
        spawnEgg("hullbreaker", ACEntityRegistry.HULLBREAKER, 0X182538, 0X76FFFD, ACBiomeRegistry.ABYSSAL_CHASM);
        spawnEgg("gossamer_worm", ACEntityRegistry.GOSSAMER_WORM, 0XC8F1FF, 0X96DEF6, ACBiomeRegistry.ABYSSAL_CHASM);
        spawnEgg("tripodfish", ACEntityRegistry.TRIPODFISH, 0X34529D, 0X81A1CF, ACBiomeRegistry.ABYSSAL_CHASM);
        spawnEgg("deep_one", ACEntityRegistry.DEEP_ONE, 0X0D2547, 0X0A843B, ACBiomeRegistry.ABYSSAL_CHASM);
        spawnEgg("deep_one_knight", ACEntityRegistry.DEEP_ONE_KNIGHT, 0X472C3B, 0XD4CCC3, ACBiomeRegistry.ABYSSAL_CHASM);
        spawnEgg("deep_one_mage", ACEntityRegistry.DEEP_ONE_MAGE, 0X96DEF6, 0XD1FF00, ACBiomeRegistry.ABYSSAL_CHASM);
        spawnEgg("mine_guardian", ACEntityRegistry.MINE_GUARDIAN, 0X404253, 0XE62008, ACBiomeRegistry.ABYSSAL_CHASM);
        spawnEgg("gloomoth", ACEntityRegistry.GLOOMOTH, 0X5E463D, 0XEBD3BE, ACBiomeRegistry.FORLORN_HOLLOWS);
        spawnEgg("underzealot", ACEntityRegistry.UNDERZEALOT, 0X291C17, 0XF27C68, ACBiomeRegistry.FORLORN_HOLLOWS);
        spawnEgg("watcher", ACEntityRegistry.WATCHER, 0X291C17, 0XEC1900, ACBiomeRegistry.FORLORN_HOLLOWS);
        spawnEgg("corrodent", ACEntityRegistry.CORRODENT, 0X351A14, 0X593B33, ACBiomeRegistry.FORLORN_HOLLOWS);
        spawnEgg("vesper", ACEntityRegistry.VESPER, 0X884E2A, 0XA54A6B, ACBiomeRegistry.FORLORN_HOLLOWS);
        spawnEgg("forsaken", ACEntityRegistry.FORSAKEN, 0X000000, 0X110909, ACBiomeRegistry.FORLORN_HOLLOWS);
        spawnEgg("sweetish_fish", ACEntityRegistry.SWEETISH_FISH, 0XE9132C, 0XFF364D, ACBiomeRegistry.CANDY_CAVITY);
        spawnEgg("caniac", ACEntityRegistry.CANIAC, 0XF9F0FF, 0XFF3F56, ACBiomeRegistry.CANDY_CAVITY);
        spawnEgg("gumbeeper", ACEntityRegistry.GUMBEEPER, 0XFF2B44, 0XE7BAFF, ACBiomeRegistry.CANDY_CAVITY);
        spawnEgg("candicorn", ACEntityRegistry.CANDICORN, 0XE86B00, 0XFFEF57, ACBiomeRegistry.CANDY_CAVITY);
        spawnEgg("gum_worm", ACEntityRegistry.GUM_WORM, 0X92FFD9, 0XFFA1DC, ACBiomeRegistry.CANDY_CAVITY);
        spawnEgg("caramel_cube", ACEntityRegistry.CARAMEL_CUBE, 0XCC8015, 0XB86A0D, ACBiomeRegistry.CANDY_CAVITY);
        spawnEgg("gummy_bear", ACEntityRegistry.GUMMY_BEAR, 0XFF463F, 0XFDA09E, ACBiomeRegistry.CANDY_CAVITY);
        spawnEgg("licowitch", ACEntityRegistry.LICOWITCH, 0X681182, 0XFF6CD7, ACBiomeRegistry.CANDY_CAVITY);
        spawnEgg("gingerbread_man", ACEntityRegistry.GINGERBREAD_MAN, 0XBB581D, 0XFFFFFF, ACBiomeRegistry.CANDY_CAVITY);
    }

    private static void spawnEgg(String entityName, Supplier<? extends EntityType<? extends Mob>> type, int color1, int color2, ResourceKey<Biome> biomeTab) {
        DeferredHolder<Item, Item> item = reg("spawn_egg_" + entityName, () -> new net.minecraft.world.item.SpawnEggItem(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().spawnEgg(type.get())));
        creativeTabSpawnEggMap.put(item, biomeTab);
    }

    public static void setup() {
        DispenserBlock.registerBehavior(SEEKING_ARROW.get(), new ACProjectileDispenseBehavior() {
            protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
                AbstractArrow abstractarrow = new SeekingArrowEntity(level, position.x(), position.y(), position.z());
                abstractarrow.pickup = AbstractArrow.Pickup.ALLOWED;
                return abstractarrow;
            }
        });
        
        DispenserBlock.registerBehavior(TRILOCARIS_BUCKET.get(), new FluidContainerDispenseItemBehavior());
        DispenserBlock.registerBehavior(ACID_BUCKET.get(), new FluidContainerDispenseItemBehavior());
        DispenserBlock.registerBehavior(RADGILL_BUCKET.get(), new FluidContainerDispenseItemBehavior());
        DispenserBlock.registerBehavior(CINDER_BRICK.get(), new ACProjectileDispenseBehavior() {
            protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
                return new CinderBrickEntity(level, position.x(), position.y(), position.z());
            }
        });
        DispenserBlock.registerBehavior(LANTERNFISH_BUCKET.get(), new FluidContainerDispenseItemBehavior());
        DispenserBlock.registerBehavior(TRIPODFISH_BUCKET.get(), new FluidContainerDispenseItemBehavior());
        DispenserBlock.registerBehavior(SEA_PIG_BUCKET.get(), new FluidContainerDispenseItemBehavior());
        DispenserBlock.registerBehavior(GOSSAMER_WORM_BUCKET.get(), new FluidContainerDispenseItemBehavior());
        DispenserBlock.registerBehavior(INK_BOMB.get(), new ACProjectileDispenseBehavior() {
            protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
                return new InkBombEntity(level, position.x(), position.y(), position.z());
            }
        });
        DispenserBlock.registerBehavior(GLOW_INK_BOMB.get(), new ACProjectileDispenseBehavior() {
            protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
                InkBombEntity inkBombEntity = new InkBombEntity(level, position.x(), position.y(), position.z());
                inkBombEntity.setGlowingBomb(true);
                inkBombEntity.setItem(itemStack);
                return inkBombEntity;
            }
        });
        DispenserBlock.registerBehavior(GUANO.get(), new ACProjectileDispenseBehavior() {
            protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
                return new GuanoEntity(level, position.x(), position.y(), position.z());
            }
        });
        DispenserBlock.registerBehavior(BURROWING_ARROW.get(), new ACProjectileDispenseBehavior() {
            protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
                AbstractArrow abstractarrow = new BurrowingArrowEntity(level, position.x(), position.y(), position.z());
                abstractarrow.pickup = AbstractArrow.Pickup.ALLOWED;
                return abstractarrow;
            }
        });
        DispenserBlock.registerBehavior(ACBlockRegistry.NUCLEAR_BOMB.get(), new DefaultDispenseItemBehavior() {
            protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
                Level level = blockSource.level();
                BlockPos blockpos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
                NuclearBombEntity nuclearBomb = new NuclearBombEntity(level, (double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D);
                level.addFreshEntity(nuclearBomb);
                level.playSound((Player)null, nuclearBomb.getX(), nuclearBomb.getY(), nuclearBomb.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent((Entity)null, GameEvent.ENTITY_PLACE, blockpos);
                itemStack.shrink(1);
                return itemStack;
            }
        });
        DispenserBlock.registerBehavior(PURPLE_SODA_BUCKET.get(), new FluidContainerDispenseItemBehavior());
        DispenserBlock.registerBehavior(SWEETISH_FISH_RED_BUCKET.get(), new FluidContainerDispenseItemBehavior());
        DispenserBlock.registerBehavior(SWEETISH_FISH_GREEN_BUCKET.get(), new FluidContainerDispenseItemBehavior());
        DispenserBlock.registerBehavior(SWEETISH_FISH_BLUE_BUCKET.get(), new FluidContainerDispenseItemBehavior());
        DispenserBlock.registerBehavior(SWEETISH_FISH_YELLOW_BUCKET.get(), new FluidContainerDispenseItemBehavior());
        DispenserBlock.registerBehavior(SWEETISH_FISH_PINK_BUCKET.get(), new FluidContainerDispenseItemBehavior());
        // Compostables are now registered via data map: data/neoforge/data_maps/item/compostables.json

    }

    public static List<DeferredHolder<Item, Item>> getSpawnEggsForTab(ResourceKey<Biome> tabName) {
        List<DeferredHolder<Item, Item>> list = new ArrayList<>();
        for (Map.Entry<DeferredHolder<Item, ? extends Item>, ResourceKey<Biome>> entry : creativeTabSpawnEggMap.entrySet()) {
            if (entry.getValue().equals(tabName)) {
                list.add((DeferredHolder<Item, Item>) entry.getKey());
            }
        }
        return list;
    }

    public static Item getSpawnEggFor(EntityType type) {
        for (Map.Entry<DeferredHolder<Item, ? extends Item>, ResourceKey<Biome>> entry : creativeTabSpawnEggMap.entrySet()) {
            if (entry.getKey().get() instanceof net.minecraft.world.item.SpawnEggItem forgeSpawnEggItem && false) {
                return forgeSpawnEggItem;
            }
        }
        return Items.AIR;
    }
}
