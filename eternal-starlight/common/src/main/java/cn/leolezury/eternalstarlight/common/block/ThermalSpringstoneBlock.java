package cn.leolezury.eternalstarlight.common.block;

import net.minecraft.world.level.ScheduledTickAccess;

import net.minecraft.world.level.LevelReader;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ThermalSpringstoneBlock extends Block {
	public static final MapCodec<ThermalSpringstoneBlock> CODEC = simpleCodec(ThermalSpringstoneBlock::new);

	public ThermalSpringstoneBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<ThermalSpringstoneBlock> codec() {
		return CODEC;
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource source) {
		BubbleColumnBlock.updateColumn(net.minecraft.world.level.block.Blocks.BUBBLE_COLUMN, level, pos.above(), state);
	}

	@Override
	public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos pos1, BlockState newState, RandomSource randomSource) {
		if (direction == Direction.UP && newState.is(Blocks.WATER)) {
			scheduledTickAccess.scheduleTick(pos, this, 20);
		}

		return super.updateShape(state, level, scheduledTickAccess, pos, direction, pos1, newState, randomSource);
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState state1, boolean b) {
		level.scheduleTick(pos, this, 20);
	}
}
