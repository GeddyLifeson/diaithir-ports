package com.diaithir.overworldbiomes.naturesspirit.blocks;

import net.minecraft.world.level.block.TallGrassBlock;

import com.diaithir.overworldbiomes.naturesspirit.registration.NSMiscBlocks;
import com.diaithir.overworldbiomes.naturesspirit.registration.NSTags;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SedgeGrassBlock extends net.minecraft.world.level.block.TallGrassBlock implements SimpleWaterloggedBlock {

  public static final MapCodec<SedgeGrassBlock> CODEC = simpleCodec(SedgeGrassBlock::new);
  public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
  protected static final VoxelShape SHAPE = Block.box(2D, 0D, 2D, 14D, 16D, 14D);

  public SedgeGrassBlock(BlockBehaviour.Properties settings) {
    super(settings);
    this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
  }

  @Override
  @SuppressWarnings({"unchecked","rawtypes"})
  public MapCodec<TallGrassBlock> codec() {
    return (MapCodec) CODEC;
  }

  @Override
  protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
    if (world.getFluidState(pos.above()).is(FluidTags.WATER)) {
      return floor.isFaceSturdy(world, pos, Direction.UP) && !floor.is(Blocks.MAGMA_BLOCK);
    } else {
      return floor.is(NSTags.Blocks.TURNIP_STEM_GROWS_ON) || floor.is(Blocks.FARMLAND);
    }
  }

  @Override
  public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
    return !useContext.getItemInHand().is(NSMiscBlocks.AZOLLA_ITEM);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
    return this.defaultBlockState().setValue(WATERLOGGED, fluidState.is(FluidTags.WATER) && fluidState.getAmount() == 8);
  }

  @Override
  public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
    DoublePlantBlock tallPlantBlock = (DoublePlantBlock) NSMiscBlocks.TALL_SEDGE_GRASS;
    if (tallPlantBlock.defaultBlockState().canSurvive(world, pos) && world.isEmptyBlock(pos.above())) {
      DoublePlantBlock.placeAt(world, tallPlantBlock.defaultBlockState(), pos, 2);
    }
  }

  @Override
  public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
    BlockPos blockPos = pos.below();
    BlockPos blockPos2 = pos.above();
    if (state.getValue(WATERLOGGED)) {
      return super.canSurvive(state, level, pos) && level.getBlockState(blockPos).isFaceSturdy(level, blockPos, Direction.UP) && !level.getFluidState(blockPos2)
          .is(FluidTags.WATER);
    } else {
      return super.canSurvive(state, level, pos) && this.mayPlaceOn(level.getBlockState(blockPos), level, blockPos);
    }
  }

  @Override
  protected BlockState updateShape(BlockState state, LevelReader level, net.minecraft.world.level.ScheduledTickAccess tickAccess, BlockPos pos, Direction direction,
      BlockPos neighborPos, BlockState neighborState, RandomSource random) {
    if (state.getValue(WATERLOGGED)) {
      tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
    }

    return direction == Direction.DOWN && !this.canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState()
        : super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(WATERLOGGED);
  }

  @Override
  protected FluidState getFluidState(BlockState state) {
    return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
  }
}
