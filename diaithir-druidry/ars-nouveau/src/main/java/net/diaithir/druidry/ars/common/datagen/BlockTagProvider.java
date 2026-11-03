package net.diaithir.druidry.ars.common.datagen;

import net.diaithir.druidry.ars.ArsNouveau;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/** Tag keys only (datagen provider removed in the port). */
public class BlockTagProvider {
    public static final TagKey<Block> IGNORE_TILE = BlockTags.create(ArsNouveau.prefix("ignore_tile"));
    public static final TagKey<Block> SUMMON_BED = BlockTags.create(ArsNouveau.prefix("summon_bed"));
    public static final TagKey<Block> SUMMON_SLEEPABLE = BlockTags.create(ArsNouveau.prefix("summon_sleepable"));
    public static final TagKey<Block> DECORATIVE_AN = BlockTags.create(ArsNouveau.prefix("an_decorative"));
    public static final TagKey<Block> MAGIC_SAPLINGS = BlockTags.create(ArsNouveau.prefix("magic_saplings"));
    public static final TagKey<Block> MAGIC_PLANTS = BlockTags.create(ArsNouveau.prefix("magic_plants"));
    public static final TagKey<Block> HARVEST_FOLIAGE = BlockTags.create(ArsNouveau.prefix("harvest/foliage"));
    public static final TagKey<Block> HARVEST_STEMS = BlockTags.create(ArsNouveau.prefix("harvest/stems"));
    public static final TagKey<Block> BREAK_BLACKLIST = BlockTags.create(ArsNouveau.prefix("break_blacklist"));
    public static final TagKey<Block> GRAVITY_BLACKLIST = BlockTags.create(ArsNouveau.prefix("gravity_blacklist"));
    public static final TagKey<Block> NO_BREAK_DROP = BlockTags.create(ArsNouveau.prefix("no_break_drop"));
    public static final TagKey<Block> FELLABLE = BlockTags.create(ArsNouveau.prefix("harvest/fellable"));
    public static final TagKey<Block> BUDDING_BLOCKS = BlockTags.create(ArsNouveau.prefix("golem/budding"));
    public static final TagKey<Block> CLUSTER_BLOCKS = BlockTags.create(ArsNouveau.prefix("golem/cluster"));
    public static final TagKey<Block> BREAK_WITH_PICKAXE = BlockTags.create(ArsNouveau.prefix("break_with_pickaxe"));
    public static final TagKey<Block> AUTOPULL_DISABLED = BlockTags.create(ArsNouveau.prefix("storage/autopull_disabled"));
    public static final TagKey<Block> RELOCATION_NOT_SUPPORTED = BlockTags.create(Identifier.fromNamespaceAndPath("c", "relocation_not_supported"));
    public static final TagKey<Block> OCCLUDES_SPELL_SENSOR = BlockTags.create(ArsNouveau.prefix("occludes_spell_sensor"));
    public static final TagKey<Block> INTERACT_BLACKLIST = BlockTags.create(ArsNouveau.prefix("interact_blacklist"));
    public static final TagKey<Block> CASCADING_LOGS = BlockTags.create(ArsNouveau.prefix("cascading_logs"));
    public static final TagKey<Block> FLOURISHING_LOGS = BlockTags.create(ArsNouveau.prefix("flourishing_logs"));
    public static final TagKey<Block> VEXING_LOGS = BlockTags.create(ArsNouveau.prefix("vexing_logs"));
    public static final TagKey<Block> BLAZING_LOGS = BlockTags.create(ArsNouveau.prefix("blazing_logs"));
    public static final TagKey<Block> DOWSING_ROD = BlockTags.create(ArsNouveau.prefix("dowsing_rod"));
    public static final TagKey<Block> BUSHES = BlockTags.create(Identifier.fromNamespaceAndPath("c", "bushes"));
}
