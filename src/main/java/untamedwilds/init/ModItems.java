package untamedwilds.init;

import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import untamedwilds.UntamedWilds;
import untamedwilds.item.*;
import untamedwilds.item.debug.*;
import untamedwilds.util.ModCreativeModeTab;

import java.util.function.Function;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = UntamedWilds.MOD_ID)
public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(UntamedWilds.MOD_ID);
    // Wild Level Item instances

    // Debug Tools
    public static DeferredHolder<Item, Item> LOGO = createItem("logo", props -> new Item(props.stacksTo(1).rarity(Rarity.EPIC)));
    public static DeferredHolder<Item, Item> OWNERSHIP_DEED = createItem("ownership_deed", props -> new OwnershipDeedItem(props.stacksTo(1)));
    public static DeferredHolder<Item, Item> DEBUG_ERASER = createItem("debug_eraser", props -> new EraserItem(props.stacksTo(1).rarity(Rarity.EPIC)));
    public static DeferredHolder<Item, Item> DEBUG_ANALYZER = createItem("debug_analyzer", props -> new AnalyzerItem(props.stacksTo(1).rarity(Rarity.EPIC)));
    public static DeferredHolder<Item, Item> DEBUG_IPECAC = createItem("debug_ipecac", props -> new IpecacItem(props.stacksTo(1).rarity(Rarity.EPIC)));
    public static DeferredHolder<Item, Item> DEBUG_LOVE_POTION = createItem("debug_love_potion", props -> new LovePotionItem(props.stacksTo(1).rarity(Rarity.EPIC)));
    public static DeferredHolder<Item, Item> DEBUG_GROWTH_TONIC = createItem("debug_growth_tonic", props -> new GrowthTonicItem(props.stacksTo(1).rarity(Rarity.EPIC)));
    public static DeferredHolder<Item, Item> DEBUG_HIGHLIGHTER = createItem("debug_highlighter", props -> new HighlighterItem(props.stacksTo(1).rarity(Rarity.EPIC)));

    // Materials
    public static DeferredHolder<Item, Item> MATERIAL_FAT = createItem("material_fat", props -> new LardItem(props.food((new FoodProperties.Builder()).nutrition(1).saturationModifier(1F).build())));
    public static DeferredHolder<Item, Item> MATERIAL_BLUBBER = createItem("material_blubber", props -> new LardItem(props.food((new FoodProperties.Builder()).nutrition(1).saturationModifier(1F).build())){
        public int getBurnTime(ItemStack itemStack, RecipeType<?> recipeType) {
            return 1200;
        }
    });
         public static DeferredHolder<Item, Item> MATERIAL_PEARL = createItem("material_pearl", props -> new Item(props));
     public static DeferredHolder<Item, Item> RARE_GIANT_PEARL = createItem("material_giant_pearl", props -> new Item(props.rarity(Rarity.UNCOMMON)));
     public static DeferredHolder<Item, Item> MATERIAL_SNAKE_SKIN = createItem("material_snake_skin", props -> new Item(props));
    public static DeferredHolder<Item, Item> CHUM = createItem("chum", props -> new ChumItem(props.food((new FoodProperties.Builder()).nutrition(1).saturationModifier(0.1F).build()).component(net.minecraft.core.component.DataComponents.CONSUMABLE, net.minecraft.world.item.component.Consumable.builder().onConsume(new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.NAUSEA, 1200, 0), 1F)).build())));

    // Food
    public static DeferredHolder<Item, Item> MEAT_BEAR_RAW = createItem("food_bear_raw", props -> new Item(props.food((new FoodProperties.Builder()).nutrition(3).saturationModifier(0.6F).build())));
    public static DeferredHolder<Item, Item> MEAT_BEAR_COOKED = createItem("food_bear_cooked", props -> new Item(props.food((new FoodProperties.Builder()).nutrition(7).saturationModifier(1F).build())));
    public static DeferredHolder<Item, Item> MEAT_TURTLE_RAW = createItem("food_turtle_raw", props -> new Item(props.food((new FoodProperties.Builder()).nutrition(2).saturationModifier(0.3F).build())));
    public static DeferredHolder<Item, Item> MEAT_TURTLE_COOKED = createItem("food_turtle_cooked", props -> new Item(props.food((new FoodProperties.Builder()).nutrition(6).saturationModifier(0.6F).build())));
        public static DeferredHolder<Item, Item> MEAT_HIPPO_RAW = createItem("food_pachyderm_raw", props -> new Item(props.food((new FoodProperties.Builder()).nutrition(3).saturationModifier(0.7F).build())));
    public static DeferredHolder<Item, Item> MEAT_HIPPO_COOKED = createItem("food_pachyderm_cooked", props -> new Item(props.food((new FoodProperties.Builder()).nutrition(7).saturationModifier(1.1F).build())));
    public static DeferredHolder<Item, Item> FOOD_TURTLE_SOUP = createItem("food_turtle_soup", props -> new Item(props.food((new FoodProperties.Builder()).nutrition(8).saturationModifier(0.6F).build()).stacksTo(1)));
    public static DeferredHolder<Item, Item> FOOD_PEMMICAN = createItem("food_pemmican", props -> new Item(props.food((new FoodProperties.Builder()).nutrition(6).saturationModifier(1.0F).build())));
    public static DeferredHolder<Item, Item> VEGETABLE_AARDVARK_CUCUMBER = createItem("food_aardvark_cucumber", props -> new Item(props.food((new FoodProperties.Builder()).nutrition(3).saturationModifier(0.2F).build())));
    public static DeferredHolder<Item, Item> FOOD_HEMLOCK_STEW = createItem("food_hemlock_stew", props -> new Item(props.food((new FoodProperties.Builder()).nutrition(6).saturationModifier(0.1F).build()).component(net.minecraft.core.component.DataComponents.CONSUMABLE, net.minecraft.world.item.component.Consumable.builder().onConsume(new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.POISON, 1200, 3), 1.0F)).build()).stacksTo(1)));

    // Hides
    public static DeferredHolder<Item, Item> HIDE_BEAR_ASHEN = createItem("hide_bear_ashen", props -> new Item(props));
    public static DeferredHolder<Item, Item> HIDE_BEAR_BLACK = createItem("hide_bear_black", props -> new Item(props));
    public static DeferredHolder<Item, Item> HIDE_BEAR_BROWN = createItem("hide_bear_brown", props -> new Item(props));
    public static DeferredHolder<Item, Item> HIDE_BEAR_WHITE = createItem("hide_bear_white", props -> new Item(props));
    public static DeferredHolder<Item, Item> HIDE_BIGCAT_JAGUAR = createItem("hide_bigcat_jaguar", props -> new Item(props));
    public static DeferredHolder<Item, Item> HIDE_BIGCAT_LEOPARD = createItem("hide_bigcat_leopard", props -> new Item(props));
    public static DeferredHolder<Item, Item> HIDE_BIGCAT_LION = createItem("hide_bigcat_lion", props -> new Item(props));
    public static DeferredHolder<Item, Item> HIDE_BIGCAT_PANTHER = createItem("hide_bigcat_panther", props -> new Item(props));
    public static DeferredHolder<Item, Item> HIDE_BIGCAT_PUMA = createItem("hide_bigcat_puma", props -> new Item(props));
    public static DeferredHolder<Item, Item> HIDE_BIGCAT_SNOW_LEOPARD = createItem("hide_bigcat_snow_leopard", props -> new Item(props));
    public static DeferredHolder<Item, Item> HIDE_BIGCAT_TIGER = createItem("hide_bigcat_tiger", props -> new Item(props));

    // Item Blocks
    public static DeferredHolder<Item, Item> SEED_TITAN_ARUM = createItem("flora_titan_arum_corm", props -> new net.minecraft.world.item.BlockItem(ModBlock.TITAN_ARUM.get(), props));
    public static DeferredHolder<Item, Item> SEED_ZIMBABWE_ALOE = createItem("flora_zimbabwe_aloe_sapling", props -> new net.minecraft.world.item.BlockItem(ModBlock.ZIMBABWE_ALOE.get(), props));
    public static DeferredHolder<Item, Item> WATER_HYACINTH_BLOCK = createItem("flora_water_hyacinth_item", props -> new PlaceOnWaterBlockItem(ModBlock.WATER_HYACINTH.get(), props));

    public static DeferredHolder<Item, Item> createItem(String name, Function<Item.Properties, ? extends Item> factory) {
        return ModItems.ITEMS.registerItem(name, factory);
    }

    public static void registerSpawnItems() {
        // These items have no associated objects, as they are not supposed to be accessed, and I do not want to register each variant
        // Tarantula Items
    ModItems.ITEMS.registerItem("egg_tarantula", props -> new MobEggItem(ModEntity.TARANTULA::get, props));
    ModItems.ITEMS.registerItem("bottle_tarantula", props -> new MobBottledItem(ModEntity.TARANTULA::get, props));

        // Small Snake Items
    ModItems.ITEMS.registerItem("egg_snake", props -> new MobEggItem(ModEntity.SNAKE::get, props));
    ModItems.ITEMS.registerItem("spawn_snake", props -> new MobSpawnItem(ModEntity.SNAKE::get, props));

        // Softshell Turtle Items
    ModItems.ITEMS.registerItem("egg_softshell_turtle", props -> new MobEggItem(ModEntity.SOFTSHELL_TURTLE::get, props));
    ModItems.ITEMS.registerItem("spawn_softshell_turtle", props -> new MobSpawnItem(ModEntity.SOFTSHELL_TURTLE::get, props));

        // Giant Clam Items
    ModItems.ITEMS.registerItem("egg_giant_clam", props -> new MobEggItem(ModEntity.GIANT_CLAM::get, props));
    ModItems.ITEMS.registerItem("spawn_giant_clam", props -> new MobSpawnItem(ModEntity.GIANT_CLAM::get, props));

        // Sunfish Items
    ModItems.ITEMS.registerItem("egg_sunfish", props -> new MobEggItem(ModEntity.SUNFISH::get, props));

        // Trevally Items
    ModItems.ITEMS.registerItem("egg_trevally", props -> new MobEggItem(ModEntity.TREVALLY::get, props));
    ModItems.ITEMS.registerItem("bucket_trevally", props -> new MobBucketedItem(ModEntity.TREVALLY::get, Fluids.WATER, props));

        // Arowana Items
    ModItems.ITEMS.registerItem("egg_arowana", props -> new MobEggItem(ModEntity.AROWANA::get, props));
    ModItems.ITEMS.registerItem("bucket_arowana", props -> new MobBucketedItem(ModEntity.AROWANA::get, Fluids.WATER, props));

        // Football Fish Items
    ModItems.ITEMS.registerItem("egg_football_fish", props -> new MobEggItem(ModEntity.FOOTBALL_FISH::get, props));
    ModItems.ITEMS.registerItem("bucket_football_fish", props -> new MobBucketedItem(ModEntity.FOOTBALL_FISH::get, Fluids.WATER, props));

        // Giant Salamander Items
    ModItems.ITEMS.registerItem("egg_giant_salamander", props -> new MobEggItem(ModEntity.GIANT_SALAMANDER::get, props));
    ModItems.ITEMS.registerItem("bucket_giant_salamander", props -> new MobBucketedItem(ModEntity.GIANT_SALAMANDER::get, Fluids.WATER, props));

        // Newt Items
    ModItems.ITEMS.registerItem("egg_newt", props -> new MobEggItem(ModEntity.NEWT::get, props));
    ModItems.ITEMS.registerItem("bucket_newt", props -> new MobBucketedItem(ModEntity.NEWT::get, Fluids.WATER, props));

        // Tortoise Items
    ModItems.ITEMS.registerItem("egg_tortoise", props -> new MobEggItem(ModEntity.TORTOISE::get, props));
    ModItems.ITEMS.registerItem("spawn_tortoise", props -> new MobSpawnItem(ModEntity.TORTOISE::get, props));

        // Large Snake Items
    ModItems.ITEMS.registerItem("egg_large_snake", props -> new MobEggItem(ModEntity.ANACONDA::get, props));

        // Triggerfish Items
    ModItems.ITEMS.registerItem("egg_triggerfish", props -> new MobEggItem(ModEntity.TRIGGERFISH::get, props));
    ModItems.ITEMS.registerItem("bucket_triggerfish", props -> new MobBucketedItem(ModEntity.TRIGGERFISH::get, Fluids.WATER, props));

        // Catfish Items
    ModItems.ITEMS.registerItem("egg_catfish", props -> new MobEggItem(ModEntity.CATFISH::get, props));
    ModItems.ITEMS.registerItem("bucket_catfish", props -> new MobBucketedItem(ModEntity.CATFISH::get, Fluids.WATER, props));

        // King Crab Items
    ModItems.ITEMS.registerItem("egg_king_crab", props -> new MobEggItem(ModEntity.KING_CRAB::get, props));
    ModItems.ITEMS.registerItem("bucket_king_crab", props -> new MobBucketedItem(ModEntity.KING_CRAB::get, Fluids.WATER, props));

        // Monitor Items
    ModItems.ITEMS.registerItem("egg_monitor", props -> new MobEggItem(ModEntity.MONITOR::get, props));

        // Spadefish Items
    ModItems.ITEMS.registerItem("egg_spadefish", props -> new MobEggItem(ModEntity.SPADEFISH::get, props));
    ModItems.ITEMS.registerItem("bucket_spadefish", props -> new MobBucketedItem(ModEntity.SPADEFISH::get, Fluids.WATER, props));

    }
}