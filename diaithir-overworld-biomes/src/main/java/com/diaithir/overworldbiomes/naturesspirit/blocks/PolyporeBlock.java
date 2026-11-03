package com.diaithir.overworldbiomes.naturesspirit.blocks;

import com.diaithir.overworldbiomes.naturesspirit.registration.NSTags;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class PolyporeBlock extends MushroomBlock {

  public static final MapCodec<PolyporeBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      ResourceKey.codec(net.minecraft.core.registries.Registries.CONFIGURED_FEATURE).fieldOf("feature").forGetter(block -> block.featureKey),
      propertiesCodec()).apply(instance, (k, p) -> new PolyporeBlock(p, k)));

  public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
  private static final Map<Direction, VoxelShape> FACING_TO_SHAPE = Maps.newEnumMap(
      ImmutableMap.of(Direction.NORTH, Block.box(0.0, 4.0, 10.0, 16.0, 12.0, 16.0), Direction.SOUTH, Block.box(0.0, 4.0, 0.0, 16.0, 12.0, 6.0),
          Direction.WEST, Block.box(10.0, 4.0, 0.0, 16.0, 12.0, 16.0), Direction.EAST, Block.box(0.0, 4.0, 0.0, 6.0, 12.0, 16.0)));

  private final ResourceKey<ConfiguredFeature<?, ?>> featureKey;

  public PolyporeBlock(BlockBehaviour.Properties properties, ResourceKey<ConfiguredFeature<?, ?>> featureKey) {
    super(featureKey, properties);
    this.featureKey = featureKey;
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
  }

  @Override
  @SuppressWarnings({"unchecked","rawtypes"})
  public MapCodec<MushroomBlock> codec() {
    return (MapCodec) CODEC;
  }

  @Override
  protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
    Direction direction = state.getValue(FACING);
    BlockPos blockPos = pos.relative(direction.getOpposite());
    BlockState blockState = level.getBlockState(blockPos);
    return blockState.isFaceSturdy(level, blockPos, direction, SupportType.CENTER) || blockState.is(NSTags.Blocks.SUCCULENT_HORIZONTAL_PLACEMENT_OVERRIDE);
  }

  @Override
  protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return FACING_TO_SHAPE.get(state.getValue(FACING));
  }

  @Override
  protected BlockState rotate(BlockState state, Rotation rotation) {
    return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
  }

  @Override
  protected BlockState mirror(BlockState state, Mirror mirror) {
    return state.rotate(mirror.getRotation(state.getValue(FACING)));
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    BlockState blockState = super.getStateForPlacement(context);
    if (blockState == null) {
      blockState = this.defaultBlockState();
    }
    Level level = context.getLevel();
    BlockPos blockPos = context.getClickedPos();
    for (Direction direction : context.getNearestLookingDirections()) {
      if (!direction.getAxis().isHorizontal() || !(blockState = blockState.setValue(FACING, direction.getOpposite())).canSurvive(level, blockPos)) {
        continue;
      }
      return blockState;
    }
    return null;
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }
}
