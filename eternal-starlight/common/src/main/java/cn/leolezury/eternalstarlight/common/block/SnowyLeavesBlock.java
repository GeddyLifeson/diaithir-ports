package cn.leolezury.eternalstarlight.common.block;

import net.minecraft.util.RandomSource;

import net.minecraft.world.level.ScheduledTickAccess;

import net.minecraft.world.level.LevelReader;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class SnowyLeavesBlock extends LeavesBlock {
	public static final MapCodec<SnowyLeavesBlock> CODEC = simpleCodec(SnowyLeavesBlock::new);
	public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;

	public SnowyLeavesBlock(Properties properties) {
		super(0.01F, properties);
		this.registerDefaultState(this.defaultBlockState().setValue(SNOWY, false));
	}

	@Override
	protected void spawnFallingLeavesParticle(net.minecraft.world.level.Level level, BlockPos pos, RandomSource random) {
	}

	@Override
	public MapCodec<SnowyLeavesBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockState updateShape(BlockState blockState, LevelReader levelAccessor, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
		return direction == Direction.UP ? blockState.setValue(SNOWY, blockState2.is(BlockTags.SNOW)) : super.updateShape(blockState, levelAccessor, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(SNOWY);
	}
}
