package net.diaithir.druidry.ars.setup.registry;

import net.diaithir.druidry.ars.api.ArsNouveauAPI;
import net.diaithir.druidry.ars.api.familiar.AbstractFamiliarHolder;
import net.diaithir.druidry.ars.api.perk.IPerk;
import net.diaithir.druidry.ars.api.registry.FamiliarRegistry;
import net.diaithir.druidry.ars.api.registry.GlyphRegistry;
import net.diaithir.druidry.ars.api.registry.PerkRegistry;
import net.diaithir.druidry.ars.api.registry.RitualRegistry;
import net.diaithir.druidry.ars.api.ritual.AbstractRitual;
import net.diaithir.druidry.ars.api.spell.SpellCaster;
import net.diaithir.druidry.ars.api.spell.SpellTier;
import net.diaithir.druidry.ars.common.armor.AnimatedMagicArmor;
import net.diaithir.druidry.ars.common.items.*;
import net.diaithir.druidry.ars.common.item.ArchwoodBoatItem;
import net.diaithir.druidry.ars.common.items.curios.*;
import net.diaithir.druidry.ars.common.items.data.PresentData;
import net.diaithir.druidry.ars.common.items.data.ScryCasterData;
import net.diaithir.druidry.ars.common.items.data.ScryPosData;
import net.diaithir.druidry.ars.common.items.itemscrolls.AllowItemScroll;
import net.diaithir.druidry.ars.common.items.itemscrolls.DenyItemScroll;
import net.diaithir.druidry.ars.common.items.itemscrolls.MimicItemScroll;
import net.diaithir.druidry.ars.common.lib.LibBlockNames;
import net.diaithir.druidry.ars.common.lib.LibItemNames;
import net.diaithir.druidry.ars.common.perk.EmptyPerk;
import net.diaithir.druidry.ars.common.spell.augment.AugmentAmplify;
import net.diaithir.druidry.ars.common.spell.augment.AugmentPierce;
import net.diaithir.druidry.ars.common.spell.augment.AugmentSplit;
import net.diaithir.druidry.ars.setup.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static net.diaithir.druidry.ars.ArsNouveau.MODID;
import static net.diaithir.druidry.ars.ArsNouveau.prefix;

public class ItemsRegistry {

    public static Style LORE_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
    public static PerkItem BLANK_THREAD;

    public static FoodProperties SOURCE_BERRY_FOOD = new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).alwaysEdible().build();
    public static FoodProperties SOURCE_PIE_FOOD = new FoodProperties.Builder().nutrition(9).saturationModifier(0.9F).alwaysEdible().build();
    public static FoodProperties SOURCE_ROLL_FOOD = new FoodProperties.Builder().nutrition(8).saturationModifier(0.6F).alwaysEdible().build();
    public static FoodProperties MENDOSTEEN_FOOD = new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).alwaysEdible().build();
    public static FoodProperties BLASTING_FOOD = new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F)
            .alwaysEdible().build();
    public static FoodProperties BASTION_FOOD = new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F)
            .alwaysEdible().build();
    public static FoodProperties FROSTAYA_FOOD = new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F)
            .alwaysEdible().build();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, MODID);

    public static final ItemRegistryWrapper<SpellBook> NOVICE_SPELLBOOK = register(LibItemNames.NOVICE_SPELL_BOOK, () -> new SpellBook(SpellTier.ONE));
    public static final ItemRegistryWrapper<SpellBook> APPRENTICE_SPELLBOOK = register(LibItemNames.APPRENTICE_SPELL_BOOK, () -> new SpellBook(SpellTier.TWO));
    public static final ItemRegistryWrapper<SpellBook> ARCHMAGE_SPELLBOOK = register(LibItemNames.ARCHMAGE_SPELL_BOOK, () -> new SpellBook(SpellTier.THREE));
    public static final ItemRegistryWrapper<SpellBook> CREATIVE_SPELLBOOK = register(LibItemNames.CREATIVE_SPELL_BOOK, () -> new SpellBook(SpellTier.CREATIVE));
    public static final ItemRegistryWrapper<ModItem> BLANK_GLYPH = register(LibItemNames.BLANK_GLYPH);
    public static final ItemRegistryWrapper<ModItem> MAGE_BLOOM = register(LibItemNames.MAGE_BLOOM, () -> new ModItem().withTooltip(Component.translatable("ars_nouveau.tooltip.magebloom")));
    public static final ItemRegistryWrapper<ModItem> MAGE_FIBER = register(LibItemNames.MAGE_FIBER);
    public static final ItemRegistryWrapper<ModItem> MUNDANE_BELT = register(LibItemNames.MUNDANE_BELT, () -> new ModItem().withTooltip(Component.translatable("ars_nouveau.tooltip.dull")));
    public static final ItemRegistryWrapper<ModItem> RING_OF_POTENTIAL = register(LibItemNames.RING_OF_POTENTIAL, () -> new ModItem().withTooltip(Component.translatable("ars_nouveau.tooltip.dull")));
    public static final ItemRegistryWrapper<SpellParchment> SPELL_PARCHMENT = register(LibItemNames.SPELL_PARCHMENT, SpellParchment::new);
    public static final ItemRegistryWrapper<DominionWand> DOMINION_ROD = register(LibItemNames.DOMINION_WAND, DominionWand::new);
    public static final ItemRegistryWrapper<ModItem> DULL_TRINKET = register(LibItemNames.DULL_TRINKET, () -> new ModItem().withTooltip(Component.translatable("ars_nouveau.tooltip.dull")));
    public static final ItemRegistryWrapper<ModItem> WHIRLISPRIG_SHARDS = register(LibItemNames.WHIRLISPRIG_SHARDS, () -> new ModItem().withTooltip("tooltip.whirlisprig_shard").withTooltip(Component.translatable("tooltip.whirlisprig_shard2").withStyle(LORE_STYLE)));
    public static final ItemRegistryWrapper<ModItem> SOURCE_GEM = register(LibItemNames.SOURCE_GEM, () -> new ModItem().withTooltip(Component.translatable("tooltip.source_gem")));
    public static final ItemRegistryWrapper<AllowItemScroll> ALLOW_ITEM_SCROLL = register(LibItemNames.ALLOW_ITEM_SCROLL, AllowItemScroll::new);
    public static final ItemRegistryWrapper<DenyItemScroll> DENY_ITEM_SCROLL = register(LibItemNames.DENY_ITEM_SCROLL, DenyItemScroll::new);
    public static final ItemRegistryWrapper<MimicItemScroll> MIMIC_ITEM_SCROLL = register(LibItemNames.MIMIC_ITEM_SCROLL, MimicItemScroll::new);
    // TODO: 1.22 rename to EnchantersWand
    public static final ItemRegistryWrapper<ModItem> WIXIE_SHARD = register(LibItemNames.WIXIE_SHARD, () -> new ModItem().withTooltip(Component.translatable("tooltip.wixie_shard")).withTooltip(Component.translatable("tooltip.wixie_shard2").withStyle(ItemsRegistry.LORE_STYLE)));
    // TODO: 1.22 rename to EnchantersBow
    public static final ItemRegistryWrapper<ModItem> WILDEN_HORN = register(LibItemNames.WILDEN_HORN, () -> new ModItem().withTooltip(Component.translatable("tooltip.wilden_horn")));
    public static final ItemRegistryWrapper<ModItem> WILDEN_SPIKE = register(LibItemNames.WILDEN_SPIKE, () -> new ModItem().withTooltip(Component.translatable("tooltip.wilden_spike")));
    public static final ItemRegistryWrapper<ModItem> WILDEN_WING = register(LibItemNames.WILDEN_WING, () -> new ModItem().withTooltip(Component.translatable("tooltip.wilden_wing")));
    public static final ItemRegistryWrapper<ModItem> DRYGMY_SHARD = register(LibItemNames.DRYGMY_SHARD, () -> new ModItem().withTooltip(Component.translatable("tooltip.ars_nouveau.drygmy_shard")).withTooltip(Component.translatable("tooltip.drygmy_shard2").withStyle(ItemsRegistry.LORE_STYLE)));
    public static final ItemRegistryWrapper<ModItem> WILDEN_TRIBUTE = register(LibItemNames.WILDEN_TRIBUTE, () -> new ModItem(defaultItemProperties().fireResistant()).withTooltip(Component.translatable("tooltip.ars_nouveau.wilden_tribute").withStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.BLUE))).withRarity(Rarity.EPIC));
    public static final ItemRegistryWrapper<ShapersFocus> SHAPERS_FOCUS = register(LibItemNames.SHAPERS_FOCUS, () -> new ShapersFocus(defaultItemProperties().stacksTo(1)));
    public static final ItemRegistryWrapper<AnimatedMagicArmor> SORCERER_BOOTS = register(LibItemNames.SORCERER_BOOTS, () -> AnimatedMagicArmor.light(ArmorType.BOOTS));
    public static final ItemRegistryWrapper<AnimatedMagicArmor> SORCERER_LEGGINGS = register(LibItemNames.SORCERER_LEGGINGS, () -> AnimatedMagicArmor.light(ArmorType.LEGGINGS));
    public static final ItemRegistryWrapper<AnimatedMagicArmor> SORCERER_ROBES = register(LibItemNames.SORCERER_ROBES, () -> AnimatedMagicArmor.light(ArmorType.CHESTPLATE));
    public static final ItemRegistryWrapper<AnimatedMagicArmor> SORCERER_HOOD = register(LibItemNames.SORCERER_HOOD, () -> AnimatedMagicArmor.light(ArmorType.HELMET));
    public static final ItemRegistryWrapper<AnimatedMagicArmor> ARCANIST_BOOTS = register(LibItemNames.ARCANIST_BOOTS, () -> AnimatedMagicArmor.medium(ArmorType.BOOTS));
    public static final ItemRegistryWrapper<AnimatedMagicArmor> ARCANIST_LEGGINGS = register(LibItemNames.ARCANIST_LEGGINGS, () -> AnimatedMagicArmor.medium(ArmorType.LEGGINGS));
    public static final ItemRegistryWrapper<AnimatedMagicArmor> ARCANIST_ROBES = register(LibItemNames.ARCANIST_ROBES, () -> AnimatedMagicArmor.medium(ArmorType.CHESTPLATE));
    public static final ItemRegistryWrapper<AnimatedMagicArmor> ARCANIST_HOOD = register(LibItemNames.ARCANIST_HOOD, () -> AnimatedMagicArmor.medium(ArmorType.HELMET));
    public static final ItemRegistryWrapper<AnimatedMagicArmor> BATTLEMAGE_BOOTS = register(LibItemNames.BATTLEMAGE_BOOTS, () -> AnimatedMagicArmor.heavy(ArmorType.BOOTS));
    public static final ItemRegistryWrapper<AnimatedMagicArmor> BATTLEMAGE_LEGGINGS = register(LibItemNames.BATTLEMAGE_LEGGINGS, () -> AnimatedMagicArmor.heavy(ArmorType.LEGGINGS));
    public static final ItemRegistryWrapper<AnimatedMagicArmor> BATTLEMAGE_ROBES = register(LibItemNames.BATTLEMAGE_ROBES, () -> AnimatedMagicArmor.heavy(ArmorType.CHESTPLATE));
    public static final ItemRegistryWrapper<AnimatedMagicArmor> BATTLEMAGE_HOOD = register(LibItemNames.BATTLEMAGE_HOOD, () -> AnimatedMagicArmor.heavy(ArmorType.HELMET));
    public static final ItemRegistryWrapper<ScryerScroll> SCRYER_SCROLL = register(LibItemNames.SCRYER_SCROLL, ScryerScroll::new);
    public static final ItemRegistryWrapper<DowsingRod> DOWSING_ROD = register(LibItemNames.DOWSING_ROD, DowsingRod::new);

    // TODO: 1.22 rename to EnchantersCrossbow
    public static final ItemRegistryWrapper<Item> ALAKARKINOS_SHARD = register(LibItemNames.ALAKARKINOS_SHARD, () -> new ModItem().withTooltip("tooltip.alakarkinos_shard1").withTooltip(Component.translatable("tooltip.alakarkinos_shard2").withStyle(LORE_STYLE)));

    // ARS_STENCIL (BannerPatternItem) removed: banner pattern items no longer exist in 26.1 (data-driven provides_banner_patterns component)

    public static final ItemRegistryWrapper<SignItem> ARCHWOOD_SIGN = register(LibBlockNames.ARCHWOOD_SIGN, () -> new SignItem(BlockRegistry.ARCHWOOD_SIGN.get(), BlockRegistry.ARCHWOOD_WALL_SIGN.get(), defaultItemProperties().stacksTo(16)));
    public static final ItemRegistryWrapper<HangingSignItem> ARCHWOOD_HANGING_SIGN = register(LibBlockNames.ARCHWOOD_HANGING_SIGN, () -> new HangingSignItem(BlockRegistry.ARCHWOOD_HANGING_SIGN.get(), BlockRegistry.ARCHWOOD_HANGING_WALL_SIGN.get(), defaultItemProperties().stacksTo(16)));
    public static final ItemRegistryWrapper<ArchwoodBoatItem> ARCHWOOD_BOAT = register(LibItemNames.ARCHWOOD_BOAT, () -> new ArchwoodBoatItem(defaultItemProperties().stacksTo(1)));

    public static final ItemRegistryWrapper<ExperienceGem> EXPERIENCE_GEM = register(LibItemNames.EXP_GEM, () -> {
        ExperienceGem gem = new ExperienceGem() {
            @Override
            public int getValue() {
                return 3;
            }
        };
        gem.withTooltip(Component.translatable("ars_nouveau.tooltip.exp_gem"));
        return gem;
    });
    public static final ItemRegistryWrapper<ExperienceGem> GREATER_EXPERIENCE_GEM = register(LibItemNames.GREATER_EXP_GEM, () -> {
        ExperienceGem gem = new ExperienceGem() {
            @Override
            public int getValue() {
                return 12;
            }
        };
        gem.withTooltip(Component.translatable("ars_nouveau.tooltip.exp_gem"));
        return gem;
    });
    public static final ItemRegistryWrapper<JarOfLight> JAR_OF_LIGHT = register(LibItemNames.JAR_OF_LIGHT, JarOfLight::new);
    public static final ItemRegistryWrapper<WarpScroll> WARP_SCROLL = register(LibItemNames.WARP_SCROLL, WarpScroll::new);
    public static final ItemRegistryWrapper<EnchantersShield> ENCHANTERS_SHIELD = register(LibItemNames.ENCHANTERS_SHIELD, EnchantersShield::new);
    public static final ItemRegistryWrapper<CasterTome> CASTER_TOME = register(LibItemNames.CASTER_TOME, CasterTome::new);
    public static final ItemRegistryWrapper<ManipulationEssence> MANIPULATION_ESSENCE = register(LibItemNames.MANIPULATION_ESSENCE, ManipulationEssence::new);
    public static final ItemRegistryWrapper<StableWarpScroll> STABLE_WARP_SCROLL = register(LibItemNames.STABLE_WARP_SCROLL, () -> new StableWarpScroll(defaultItemProperties().stacksTo(1)));

    public static <T extends Item> ItemRegistryWrapper<T> register(String name, Supplier<T> item) {
        return new ItemRegistryWrapper<>(ITEMS.register(name, () -> {
            CURRENT_ID = ResourceKey.create(Registries.ITEM, prefix(name));
            try {
                return item.get();
            } finally {
                CURRENT_ID = null;
            }
        }));
    }

    public static ItemRegistryWrapper<ModItem> register(String name) {
        return register(name, ModItem::new);
    }

    public static void onItemRegistry(RegisterEvent.RegisterHelper<Item> helper) {
        ArsNouveauAPI api = ArsNouveauAPI.getInstance();
        for (Map.Entry<Identifier, Supplier<Glyph>> glyphEntry : GlyphRegistry.getGlyphItemMap().entrySet()) {
            Glyph glyph = scoped(glyphEntry.getKey(), glyphEntry.getValue());
            helper.register(glyphEntry.getKey(), glyph);
            glyph.spellPart.glyphItem = glyph;
        }

        for (AbstractRitual ritual : RitualRegistry.getRitualMap().values()) {
            RitualTablet tablet = scoped(ritual.getRegistryName(), () -> new RitualTablet(ritual));
            helper.register(ritual.getRegistryName(), tablet);
            RitualRegistry.getRitualItemMap().put(ritual.getRegistryName(), tablet);
        }

        for (AbstractFamiliarHolder holder : FamiliarRegistry.getFamiliarHolderMap().values()) {
            FamiliarScript script = scoped(holder.getRegistryName(), () -> new FamiliarScript(holder));
            FamiliarRegistry.getFamiliarScriptMap().put(holder.getRegistryName(), script);
            helper.register(holder.getRegistryName(), script);
        }

        for (IPerk perk : PerkRegistry.getPerkMap().values()) {
            PerkItem perkItem = scoped(perk.getRegistryName(), () -> new PerkItem(perk));
            PerkRegistry.getPerkItemMap().put(perk.getRegistryName(), perkItem);
            helper.register(perk.getRegistryName(), perkItem);
            if (perk instanceof EmptyPerk) {
                BLANK_THREAD = perkItem;
            }
        }

    }

    private static <T> T scoped(Identifier id, Supplier<T> item) {
        CURRENT_ID = ResourceKey.create(Registries.ITEM, id);
        try {
            return item.get();
        } finally {
            CURRENT_ID = null;
        }
    }

    public static Item.Properties defaultItemProperties() {
        Item.Properties props = new Item.Properties();
        return CURRENT_ID == null ? props : props.setId(CURRENT_ID);
    }

    /** Id of the item currently being constructed by register(); 26.1 Item.Properties need an id before the item is built. */
    private static ResourceKey<Item> CURRENT_ID;

    static Consumable eatWith(Holder<MobEffect> effect, int duration, int amplifier) {
        return Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(effect, duration, amplifier))).build();
    }

    static Consumable eatWith(Holder<MobEffect> effect, int duration) {
        return eatWith(effect, duration, 0);
    }
}

