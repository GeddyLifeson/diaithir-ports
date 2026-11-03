package com.diaithir.overworldbiomes.naturesspirit.blocks;

import com.diaithir.overworldbiomes.naturesspirit.util.NSCauldronBehavior;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CheeseCauldronBlock extends AbstractCauldronBlock {

  public static final MapCodec<CheeseCauldronBlock> CODEC = simpleCodec(CheeseCauldronBlock::new);

  public CheeseCauldronBlock(BlockBehaviour.Properties settings) {
    super(settings, NSCauldronBehavior.CHEESE_CAULDRON_BEHAVIOR);
  }

  @Override
  protected MapCodec<? extends AbstractCauldronBlock> codec() {
    return CODEC;
  }

  @Override
  public boolean isFull(BlockState state) {
    return true;
  }

  @Override
  protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
    return Shapes.block();
  }

  @Override
  public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
    return new ItemStack(Blocks.CAULDRON);
  }

  @Override
  protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos, net.minecraft.core.Direction direction) {
    return 4;
  }
}
