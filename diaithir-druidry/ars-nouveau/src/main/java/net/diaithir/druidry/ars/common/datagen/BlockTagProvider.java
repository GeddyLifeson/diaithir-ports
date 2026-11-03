package net.diaithir.druidry.ars.common.datagen;

import net.diaithir.druidry.ars.ArsNouveau;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/** Tag keys only (datagen provider removed in the port). */
public class BlockTagProvider {
    public static final TagKey<Block> IGNORE_TILE = BlockTags.create(ArsNouveau.prefix("ignore_tile"));
    public static final TagKey<Block> BREAK_BLACKLIST = BlockTags.create(ArsNouveau.prefix("break_blacklist"));
    public static final TagKey<Block> GRAVITY_BLACKLIST = BlockTags.create(ArsNouveau.prefix("gravity_blacklist"));
    public static final TagKey<Block> BREAK_WITH_PICKAXE = BlockTags.create(ArsNouveau.prefix("break_with_pickaxe"));
    public static final TagKey<Block> CASCADING_LOGS = BlockTags.create(ArsNouveau.prefix("cascading_logs"));
    public static final TagKey<Block> FLOURISHING_LOGS = BlockTags.create(ArsNouveau.prefix("flourishing_logs"));
    public static final TagKey<Block> VEXING_LOGS = BlockTags.create(ArsNouveau.prefix("vexing_logs"));
    public static final TagKey<Block> BLAZING_LOGS = BlockTags.create(ArsNouveau.prefix("blazing_logs"));
    public static final TagKey<Block> RELOCATION_NOT_SUPPORTED = BlockTags.create(Identifier.fromNamespaceAndPath("c", "relocation_not_supported"));
}
