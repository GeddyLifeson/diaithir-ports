package cn.leolezury.eternalstarlight.common.block;

import net.minecraft.util.RandomSource;

import net.minecraft.world.level.ScheduledTickAccess;

import cn.leolezury.eternalstarlight.common.registry.ESBlocks;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class TorreyaVinesPlantBlock extends GrowingPlantBodyBlock {
	public static final MapCodec<TorreyaVinesPlantBlock> CODEC = simpleCodec(TorreyaVinesPlantBlock::new);
	public static final BooleanProperty TOP = BooleanProperty.create("top");

	public TorreyaVinesPlantBlock(Properties properties) {
		super(properties, Direction.DOWN, TorreyaVinesBlock.SHAPE, false);
		this.registerDefaultState(this.stateDefinition.any().setValue(TOP, false));
	}

	@Override
	protected MapCodec<TorreyaVinesPlantBlock> codec() {
		return CODEC;
	}

	@Override
	protected GrowingPlantHeadBlock getHeadBlock() {
		return ESBlocks.TORREYA_VINES.get();
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos attachPos = pos.relative(this.growthDirection.getOpposite());
		BlockState attachState = level.getBlockState(attachPos);
		if (!this.canAttachTo(attachState)) {
			return false;
		} else {
			return attachState.is(this.getHeadBlock()) || attachState.is(this.getBodyBlock()) || attachState.is(BlockTags.LEAVES) || attachState.isFaceSturdy(level, attachPos, this.growthDirection);
		}
	}

	@Override
	public BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
		BlockState state = super.updateShape(blockState, level, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
		if (!level.getBlockState(blockPos.above()).is(this) && state.hasProperty(TOP)) {
			state = state.setValue(TOP, true);
		}
		return state;
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean includeData) {
		return new ItemStack(ESItems.TORREYA_VINES.get());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(TOP);
	}
}
