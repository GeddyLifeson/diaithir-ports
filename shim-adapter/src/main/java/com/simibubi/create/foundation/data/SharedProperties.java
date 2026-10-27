package com.simibubi.create.foundation.data;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Clean-room shim of Create's SharedProperties (values identical to upstream: vanilla blocks to copy properties from). */
public class SharedProperties {
    public static Block wooden() { return Blocks.STRIPPED_SPRUCE_WOOD; }
    public static Block stone() { return Blocks.ANDESITE; }
    public static Block softMetal() { return Blocks.GOLD_BLOCK; }
    public static Block copperMetal() { return Blocks.COPPER_BLOCK; }
    public static Block netheriteMetal() { return Blocks.NETHERITE_BLOCK; }
}
