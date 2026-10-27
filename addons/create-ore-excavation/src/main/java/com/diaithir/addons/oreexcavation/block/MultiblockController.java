package com.diaithir.addons.oreexcavation.block;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.diaithir.addons.oreexcavation.block.MultiblockPart.MultiblockMainPart;
import com.diaithir.addons.oreexcavation.block.entity.MultiblockCapHandler;

public abstract class MultiblockController extends Block implements MultiblockMainPart {

	public MultiblockController(Properties p_49795_) {
		super(p_49795_);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(new Property[]{HORIZONTAL_FACING});
		super.createBlockStateDefinition(builder);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(HORIZONTAL_FACING, rot.rotate(state.getValue(HORIZONTAL_FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(HORIZONTAL_FACING)));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection());
	}

	@Override
	protected VoxelShape getVisualShape(BlockState pState, BlockGetter pReader, BlockPos pPos, CollisionContext pContext) {
		return Shapes.empty();
	}

	@Override
	protected float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return 1.0F;
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState pState) {
		return true;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack item, BlockState state, Level level,
			BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		return onActivate(state, level, pos, player, hand, hit);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		return onActivate(state, level, pos, player, hit);
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return pickBlock(level, pos, state);
	}

	@Override
	public ItemStack pickBlock(BlockGetter level, BlockPos pos, BlockState state) {
		return new ItemStack(this);
	}
}
