package com.diaithir.overworldbiomes.naturesspirit.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ParticleLeavesBlock extends LeavesBlock {

  ParticleOptions particle;
  int chance;

  public ParticleLeavesBlock(BlockBehaviour.Properties settings, ParticleOptions particle, int chance) {
    super(0.01F, settings);
    this.particle = particle;
    this.chance = chance;
  }

  @Override
  public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
    super.animateTick(state, world, pos, random);
    if (random.nextInt(chance) == 0) {
      BlockPos blockPos = pos.below();
      BlockState blockState = world.getBlockState(blockPos);
      if (!Block.isFaceFull(blockState.getCollisionShape(world, blockPos), Direction.UP)) {
        ParticleUtils.spawnParticleBelow(world, pos, random, particle);
      }
    }
  }
}
