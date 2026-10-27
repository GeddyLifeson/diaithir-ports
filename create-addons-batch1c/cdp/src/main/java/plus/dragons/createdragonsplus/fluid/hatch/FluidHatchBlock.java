/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.fluid.hatch;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.fluids.spout.FillingBySpout;
import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventoryProvider;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import plus.dragons.createdragonsplus.registry.CDPBlockEntities;

public final class FluidHatchBlock extends HorizontalDirectionalBlock
        implements IBE<FluidHatchBlockEntity>, FluidInventoryProvider<FluidHatchBlockEntity>, IWrenchable, ProperWaterloggedBlock {
    public static final MapCodec<FluidHatchBlock> CODEC = simpleCodec(FluidHatchBlock::new);

    public FluidHatchBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING, WATERLOGGED));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null || context.getClickedFace().getAxis().isVertical()) {
            return null;
        }
        return withWater(state.setValue(FACING, context.getClickedFace().getOpposite()), context);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess tickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random) {
        updateWater(level, tickAccess, state, pos);
        return state;
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (FakePlayerHandler.has(player)) {
            return InteractionResult.SUCCESS;
        }

        Direction facing = state.getValue(FACING);
        BlockPos targetPos = pos.relative(facing);
        BlockEntity targetBlockEntity = level.getBlockEntity(targetPos);
        if (!(targetBlockEntity instanceof SmartBlockEntity target)
                || FluidHelper.getFluidInventory(level, targetPos, facing.getOpposite()) == null) {
            return InteractionResult.FAIL;
        }
        boolean moved;
        if (player.isShiftKeyDown()) {
            moved = FluidHelper.tryFillItemFromBE(level, player, hand, stack, target)
                    || FluidHelper.tryEmptyItemIntoBE(level, player, hand, stack, target);
        } else {
            moved = FluidHelper.tryEmptyItemIntoBE(level, player, hand, stack, target)
                    || FluidHelper.tryFillItemFromBE(level, player, hand, stack, target);
        }
        if (!moved) {
            return InteractionResult.SUCCESS;
        }
        targetBlockEntity.setChanged();
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.5F,
                0.9F + level.getRandom().nextFloat() * 0.2F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public FluidInventory getFluidInventory(
            LevelAccessor world, BlockPos pos, BlockState self, FluidHatchBlockEntity blockEntity,
            @Nullable Direction context) {
        Direction facing = self.getValue(FACING);
        if (context != null && context != facing.getOpposite()) {
            return null;
        }
        BlockPos targetPos = pos.relative(facing);
        if (world.getBlockState(targetPos).is(this) || !(world instanceof Level level)) {
            return null;
        }
        return FluidHelper.getFluidInventory(level, targetPos, facing.getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.ITEM_HATCH.get(state.getValue(FACING).getOpposite());
    }

    @Override
    public Class<FluidHatchBlockEntity> getBlockEntityClass() {
        return FluidHatchBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FluidHatchBlockEntity> getBlockEntityType() {
        return CDPBlockEntities.FLUID_HATCH.get();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

}
