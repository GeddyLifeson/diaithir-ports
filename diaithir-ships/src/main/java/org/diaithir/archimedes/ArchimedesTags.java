package org.diaithir.archimedes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ArchimedesTags {
    /** Blocks that never become part of a ship (add to this tag from a datapack). */
    public static final TagKey<Block> NOT_ON_SHIPS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ArchimedesMod.MOD_ID, "not_on_ships"));

    private ArchimedesTags() {}
}
