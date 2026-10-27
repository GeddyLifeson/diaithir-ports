package com.github.alexmodguy.alexscaves.server.block;

import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;

public class BioluminescentTorch extends TorchBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public BioluminescentTorch() {
        super(ParticleTypes.SMOKE, com.github.alexmodguy.alexscaves.server.misc.ACProps.block().mapColor(MapColor.SAND).sound(SoundType.WOOD).noCollision().lightLevel(state -> 15).instabreak().noOcclusion());
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
    }

    public BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader levelAccessor, net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos blockPos, Direction direction, BlockPos blockPos1, BlockState state1, net.minecraft.util.RandomSource randomSource) {
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        return super.updateShape(state, levelAccessor, ticks, blockPos, direction, blockPos1, state1, randomSource);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelAccessor levelaccessor = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState prev = super.getStateForPlacement(context);
        return prev == null ? null : prev.setValue(WATERLOGGED, Boolean.valueOf(levelaccessor.getFluidState(blockpos).isSourceOfType(Fluids.WATER)));
    }


    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
        super.createBlockStateDefinition(blockStateBuilder);
        blockStateBuilder.add(WATERLOGGED);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos blockPos, RandomSource randomSource) {
        double d0 = (double)blockPos.getX() + 0.5D;
        double d1 = (double)blockPos.getY() + 0.7D;
        double d2 = (double)blockPos.getZ() + 0.5D;
        level.addParticle(ACParticleRegistry.BIO_POP.get(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }
}
