package net.id.paradise_lost.block.util;

import net.id.paradise_lost.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;

public abstract class SpreadableParadiseLostBlock extends net.minecraft.world.level.block.SpreadingSnowyBlock {
    protected SpreadableParadiseLostBlock(Properties settings) {
        super(settings, net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, net.id.paradise_lost.ModConstants.locate("dirt")));
    }

}
