package net.diaithir.furnishings;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A rope hangs from a solid ceiling or another rope, can be climbed, and grows downward when used by hand. */
public final class RopeBlock extends Block {
    public static final MapCodec<RopeBlock> CODEC = simpleCodec(RopeBlock::new);
    private static final VoxelShape SHAPE = Block.box(6, 0, 6, 10, 16, 10);

    public RopeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        return level.getBlockState(above).is(this) || level.getBlockState(above).isFaceSturdy(level, above, Direction.DOWN);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
                                     BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        return direction == Direction.UP && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
            : super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
    }

    /** Using a rope on a rope lengthens it at the bottom, so it can be paid out without aiming at the end. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(DiaithirFurnishings.ROPE_ITEM.get())) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        BlockPos end = pos;
        while (level.getBlockState(end.below()).is(this)) {
            end = end.below();
        }
        BlockPos next = end.below();
        if (!level.getBlockState(next).canBeReplaced() || level.isOutsideBuildHeight(next)) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            level.setBlockAndUpdate(next, defaultBlockState());
            level.playSound(null, next, getSoundType(state).getPlaceSound(), net.minecraft.sounds.SoundSource.BLOCKS, 1F, 1F);
            stack.consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }
}
