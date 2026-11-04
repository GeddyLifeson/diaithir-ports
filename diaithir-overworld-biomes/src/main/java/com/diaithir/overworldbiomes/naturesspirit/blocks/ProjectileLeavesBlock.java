package com.diaithir.overworldbiomes.naturesspirit.blocks;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ProjectileLeavesBlock extends net.minecraft.world.level.block.TintedParticleLeavesBlock {

  private final Block END_BLOCK;

  public ProjectileLeavesBlock(BlockBehaviour.Properties settings, Block block) {
    super(0.01F, settings);
    this.END_BLOCK = block;
  }

  @Override
  public MapCodec<? extends net.minecraft.world.level.block.TintedParticleLeavesBlock> codec() {
    // Codec is not used for data-driven construction in this port; provide a simple fallback.
    return simpleCodec(props -> new ProjectileLeavesBlock(props, Blocks.AIR));
  }

  @Override
  protected void onProjectileHit(Level world, BlockState state, BlockHitResult hit, Projectile projectile) {
    if (projectile instanceof Snowball) {
      Entity entity = projectile.getOwner();
      if (entity instanceof ServerPlayer serverPlayerEntity) {
        serverPlayerEntity.awardStat(Stats.TARGET_HIT);
      }
      world.setBlockAndUpdate(hit.getBlockPos(), this.END_BLOCK.withPropertiesOf(state));
    }
  }
}
