package com.diaithir.overworldbiomes.naturesspirit.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PaperLanternBlock extends LanternBlock {

  public static final MapCodec<PaperLanternBlock> CODEC = simpleCodec(PaperLanternBlock::new);

  protected static final VoxelShape STANDING_SHAPE = Shapes.or(
      Block.box(5D, 0D, 5D, 11D, 14D, 11D),
      Block.box(2D, 2D, 2D, 14D, 12D, 14D)
  );

  public PaperLanternBlock(BlockBehaviour.Properties settings) {
    super(settings);
  }

  @Override
  public MapCodec<? extends LanternBlock> codec() {
    return CODEC;
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
    return STANDING_SHAPE;
  }

  @Override
  public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
    Direction direction = LanternBlock.getConnectedDirection(state).getOpposite();
    return Block.canSupportCenter(world, pos.relative(direction), direction.getOpposite()) || world.getBlockState(pos.relative(direction)).is(BlockTags.LEAVES);
  }
}
