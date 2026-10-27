package net.id.paradise_lost.block.mechanical;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public abstract class ParadiseLostBlockWithEntity extends BaseEntityBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private final boolean loggable;

    protected ParadiseLostBlockWithEntity(Properties settings, boolean loggable) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false).setValue(POWERED, false));
        this.loggable = loggable;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return ctx.getLevel().isWaterAt(ctx.getClickedPos()) ? super.getStateForPlacement(ctx).setValue(WATERLOGGED, true) : super.getStateForPlacement(ctx);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel world, BlockPos pos, boolean moved) {
        Containers.updateNeighboursAfterDestroy(state, world, pos);
    }

    @Override
    public boolean canPlaceLiquid(@Nullable net.minecraft.world.entity.LivingEntity player, BlockGetter world, BlockPos pos, BlockState state, Fluid fluid) {
        return loggable && !(Boolean) state.getValue(BlockStateProperties.WATERLOGGED) && fluid == Fluids.WATER;
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, @org.jetbrains.annotations.Nullable net.minecraft.world.level.redstone.Orientation orientation, boolean notify) {
        boolean powered = world.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            world.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
            pulseUpdate(state, world, pos, powered);
        }
        super.neighborChanged(state, world, pos, block, orientation, notify);
    }

    protected void pulseUpdate(BlockState state, Level world, BlockPos pos, boolean on) {
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos, net.minecraft.core.Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, WATERLOGGED);
    }
}
