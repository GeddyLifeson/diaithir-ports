package untamedwilds.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import untamedwilds.UntamedWilds;

public class ModTags {
    public static class EntityTags {
        // The CAGE_BLACKLIST is provided for mod packs and/or data pack makers, nothing is there by default (players are hard-blacklisted since they will crash the game)
    public static final TagKey<EntityType<?>> CAGE_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, Identifier.tryParse(UntamedWilds.MOD_ID + ":cage_trap_blacklist"));
    }

    public static class ModBlockTags {
    public static final TagKey<Block> REEDS_PLANTABLE_ON = BlockTags.create(Identifier.tryParse(UntamedWilds.MOD_ID + ":reeds_plantable_on"));
    public static final TagKey<Block> ALOE_PLANTABLE_ON = BlockTags.create(Identifier.tryParse(UntamedWilds.MOD_ID + ":aloe_plantable_on"));
    public static final TagKey<Block> GRAZEABLE_BLOCKS = BlockTags.create(Identifier.tryParse(UntamedWilds.MOD_ID + ":grazeable_blocks"));
    public static final TagKey<Block> GRAZEABLE_ALGAE = BlockTags.create(Identifier.tryParse(UntamedWilds.MOD_ID + ":grazeable_algae"));
    public static final TagKey<Block> VALID_REPTILE_NEST = BlockTags.create(Identifier.tryParse(UntamedWilds.MOD_ID + ":valid_reptile_nest"));
    }
    
    public static class ModBiomeTags {
        // Custom biome tags for 1.21.1 (vanilla removed many IS_ tags)
        public static final TagKey<Biome> IS_SWAMP = create("is_swamp");
        public static final TagKey<Biome> IS_JUNGLE = create("is_jungle");
        public static final TagKey<Biome> IS_MOUNTAIN = create("is_mountain");
        public static final TagKey<Biome> IS_PLAINS = create("is_plains");
        public static final TagKey<Biome> IS_DESERT = create("is_desert");
        public static final TagKey<Biome> IS_BADLANDS = create("is_badlands");
        public static final TagKey<Biome> IS_FOREST = create("is_forest");
        public static final TagKey<Biome> IS_TAIGA = create("is_taiga");
        public static final TagKey<Biome> IS_SAVANNA = create("is_savanna");
        public static final TagKey<Biome> IS_RIVER = create("is_river");
        public static final TagKey<Biome> IS_OCEAN = create("is_ocean");
        public static final TagKey<Biome> IS_ICY = create("is_icy");
        public static final TagKey<Biome> IS_WASTELAND = create("is_wasteland");
        public static final TagKey<Biome> IS_UNDERGROUND = create("is_underground");
        
        private static TagKey<Biome> create(String name) {
            return TagKey.create(Registries.BIOME, Identifier.tryParse(UntamedWilds.MOD_ID + ":" + name));
        }
    }
}
