package com.diaithir.overworldbiomes.naturesspirit.blocks;

import com.diaithir.overworldbiomes.naturesspirit.registration.NSMiscBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WaterFlowerbedBlock extends BushBlock implements BonemealableBlock {

  public static final VoxelShape COLLISION_SHAPE = Block.box(0D, 0D, 0D, 16D, 1.5, 16D);
  public static final VoxelShape OUTLINE_SHAPE = Block.box(0D, 0D, 0D, 16D, 3D, 16D);
  public static final IntegerProperty FLOWER_AMOUNT = IntegerProperty.create("water_flower_amount", 0, 4);
  public static final EnumProperty<net.minecraft.core.Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

  public static final MapCodec<WaterFlowerbedBlock> CODEC = simpleCodec(WaterFlowerbedBlock::new);

  public WaterFlowerbedBlock(Properties settings) {
    super(settings);
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FLOWER_AMOUNT, 0));
  }

  @Override
  @SuppressWarnings({"unchecked","rawtypes"})
  public MapCodec<BushBlock> codec() {
    return (MapCodec) CODEC;
  }

  @Override
  public BlockState rotate(BlockState state, Rotation rotation) {
    return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
  }

  @Override
  public BlockState mirror(BlockState state, Mirror mirror) {
    return state.rotate(mirror.getRotation(state.getValue(FACING)));
  }

  @Override
  public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
    return COLLISION_SHAPE;
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
    return OUTLINE_SHAPE;
  }

  @Override
  protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
    InteractionHand hand = player.getUsedItemHand();
    boolean bl = player.getItemInHand(hand).is(NSMiscBlocks.HELVOLA_FLOWER_ITEM) && state.getValue(FLOWER_AMOUNT) < 4;
    if (bl) {
      world.setBlockAndUpdate(pos, state.setValue(FLOWER_AMOUNT, Math.min(4, state.getValue(FLOWER_AMOUNT) + 1)));
      if (!player.isCreative() && !player.isSpectator()) {
        player.getItemInHand(hand).shrink(1);
      }
      return InteractionResult.SUCCESS;
    }
    return super.useWithoutItem(state, world, pos, player, hit);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext ctx) {
    return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
  }

  @Override
  protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
    FluidState fluidState = world.getFluidState(pos);
    FluidState fluidState2 = world.getFluidState(pos.above());
    return (fluidState.getType() == Fluids.WATER || floor.getBlock() instanceof IceBlock) && fluidState2.getType() == Fluids.EMPTY;
  }

  @Override
  protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier fx, boolean precise) {
    super.entityInside(state, world, pos, entity, fx, precise);
    if (world instanceof ServerLevel && entity instanceof AbstractBoat) {
      world.destroyBlock(new BlockPos(pos), true, entity);
    }
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING, FLOWER_AMOUNT);
  }

  @Override
  public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
    return true;
  }

  @Override
  public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
    return true;
  }

  @Override
  public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
    int i = state.getValue(FLOWER_AMOUNT);
    if (i < 4) {
      world.setBlock(pos, state.setValue(FLOWER_AMOUNT, i + 1), Block.UPDATE_CLIENTS);
    } else {
      Block.popResource(world, pos, new ItemStack(this));
    }
  }
}
