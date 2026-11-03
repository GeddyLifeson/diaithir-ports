package net.diaithir.druidry.ars.common.datagen;

import net.diaithir.druidry.ars.ArsNouveau;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** Tag keys only (datagen provider removed in the port). */
public class ItemTagProvider {
    public static TagKey<Item> SUMMON_BED_ITEMS = ItemTags.create(ArsNouveau.prefix( "summon_bed"));
    public static TagKey<Item> SOURCE_GEM_TAG = ItemTags.create(Identifier.fromNamespaceAndPath("c", "gems/source"));
    public static TagKey<Item> SOURCE_GEM_BLOCK_TAG = ItemTags.create(Identifier.fromNamespaceAndPath("c", "storage_blocks/source"));
    public static TagKey<Item> ARCHWOOD_LOG_TAG = ItemTags.create(Identifier.fromNamespaceAndPath("c", "logs/archwood"));
    public static TagKey<Item> MAGIC_FOOD = ItemTags.create(ArsNouveau.prefix( "magic_food"));
    public static TagKey<Item> WILDEN_DROP_TAG = ItemTags.create(ArsNouveau.prefix( "wilden_drop"));
    public static TagKey<Item> SHARD_TAG = ItemTags.create(ArsNouveau.prefix( "golem/shard"));
    public static TagKey<Item> BERRY_TAG = ItemTags.create(Identifier.fromNamespaceAndPath("c", "fruits/berry"));
    public static final TagKey<Item> SUMMON_SHARDS_TAG = ItemTags.create(ArsNouveau.prefix( "magic_shards"));
    public static TagKey<Item> JAR_ITEM_BLACKLIST = ItemTags.create(ArsNouveau.prefix( "interact_jar_blacklist"));
    public static TagKey<Item> RITUAL_LOOT_BLACKLIST = ItemTags.create(ArsNouveau.prefix( "ritual_loot_blacklist"));
    public static TagKey<Item> RITUAL_TRADE_BLACKLIST = ItemTags.create(ArsNouveau.prefix( "ritual_trade_blacklist"));
    public static TagKey<Item> STORAGE_BLOCKS_QUARTZ = ItemTags.create(Identifier.fromNamespaceAndPath("c", "storage_blocks/quartz"));
    public static TagKey<Item> SHADY_WIZARD_FRUITS = ItemTags.create(ArsNouveau.prefix( "shady_wizard_fruits"));
}
