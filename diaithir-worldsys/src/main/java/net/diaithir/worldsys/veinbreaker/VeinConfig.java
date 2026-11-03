package net.diaithir.worldsys.veinbreaker;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

final class VeinConfig {
    static final int MAX_BLOCKS = 64;
    static final int RADIUS = 12;
    static final TagKey<Block> ORES = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores"));

    private VeinConfig() {}
}
