package com.github.alexmodguy.alexscaves.server.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;

public class CandyCanePoleBlock extends CrossCollisionBlock {

    public CandyCanePoleBlock() {
        super(2.0F, 2.0F, 16.0F, 16.0F, 16.0F, com.github.alexmodguy.alexscaves.server.misc.ACProps.block().mapColor(MapColor.COLOR_RED).noOcclusion().noCollision().pushReaction(PushReaction.DESTROY).instabreak().sound(ACSoundTypes.HARD_CANDY).dynamicShape());
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false).setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false));
    }

    @Override
    protected MapCodec<? extends CrossCollisionBlock> codec() {
        return MapCodec.unit(this);
    }


    public boolean connectsTo(BlockGetter level, BlockPos ourPos, BlockPos posInQuestion) {
        BlockState stateAbove = level.getBlockState(ourPos.above());
        BlockState theirState = level.getBlockState(posInQuestion);
        BlockState stateAboveThem = level.getBlockState(posInQuestion.above());
        return theirState.getBlock() instanceof CandyCanePoleBlock && !(stateAbove.getBlock() instanceof CandyCanePoleBlock) && !(stateAboveThem.getBlock() instanceof CandyCanePoleBlock);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockGetter blockgetter = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        BlockPos blockpos1 = blockpos.north();
        BlockPos blockpos2 = blockpos.east();
        BlockPos blockpos3 = blockpos.south();
        BlockPos blockpos4 = blockpos.west();
        return super.getStateForPlacement(context).setValue(NORTH, this.connectsTo(blockgetter, blockpos, blockpos1)).setValue(EAST, this.connectsTo(blockgetter, blockpos, blockpos2)).setValue(SOUTH, this.connectsTo(blockgetter, blockpos, blockpos3)).setValue(WEST, this.connectsTo(blockgetter, blockpos, blockpos4)).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
    }

    public BlockState updateShape(BlockState blockState, net.minecraft.world.level.LevelReader levelAccessor, net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos ourPos, Direction direction, BlockPos updatePos, BlockState blockState1, net.minecraft.util.RandomSource randomSource) {
        if (blockState.getValue(WATERLOGGED)) {
            ticks.scheduleTick(ourPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }

        return direction.getAxis().getPlane() == Direction.Plane.HORIZONTAL ? blockState.setValue(PROPERTY_BY_DIRECTION.get(direction), this.connectsTo(levelAccessor, ourPos, updatePos)) : super.updateShape(blockState, levelAccessor, ticks, ourPos, direction, updatePos, blockState1, randomSource);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
    }

    public static BooleanProperty getPropertyByDirection(Direction direction){
        return PROPERTY_BY_DIRECTION.get(direction);
    }

    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility toolAction, boolean simulate) {
        ItemStack itemStack = context.getItemInHand();
        if (!itemStack.canPerformAction(toolAction))
            return null;
        if (ItemAbilities.AXE_STRIP == toolAction && this == ACBlockRegistry.CANDY_CANE_POLE.get()) {
            return ACBlockRegistry.STRIPPED_CANDY_CANE_POLE.get().defaultBlockState().setValue(WATERLOGGED, state.getValue(WATERLOGGED)).setValue(NORTH, state.getValue(NORTH)).setValue(EAST, state.getValue(EAST)).setValue(WEST, state.getValue(WEST)).setValue(SOUTH, state.getValue(SOUTH));
        }
        return super.getToolModifiedState(state, context, toolAction, simulate);
    }
}
