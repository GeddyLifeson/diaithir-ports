package net.diaithir.harbour;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Harbour office: an age-evolving station. Its stage follows the calendar Age (timber quay, brick quay, iron quay, steel quay) with no input from the
 * player; the stage widens the reach of every mooring bollard within {@link #RANGE} blocks and sets how many cargo crates a boat there may carry.
 * Clean-room (the model is Tinkers' Construct's idea of a station that grows with the Age, nothing copied).
 */
public class Office extends Block {
    public static final MapCodec<Office> CODEC = simpleCodec(Office::new);
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 3);
    public static final String[] NAMES = {"timber quay", "brick quay", "iron quay", "steel quay"};
    public static final int RANGE = 16, PERIOD = 100;

    public Office(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(STAGE, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(STAGE);
    }

    /** Age 0-2 timber, 3-4 brick, 5-6 iron, 7 and up steel. */
    public static int stageFor(int age) {
        return age <= 2 ? 0 : age <= 4 ? 1 : age <= 6 ? 2 : 3;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (!level.isClientSide() && !old.is(state.getBlock())) level.scheduleTick(pos, this, 1);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int stage = stageFor(Ages.current(level.getServer()));
        if (stage != state.getValue(STAGE)) level.setBlock(pos, state.setValue(STAGE, stage), 3);
        level.scheduleTick(pos, this, PERIOD);
    }

    /** Reach of a bollard at {@code pos}: 3 blocks, plus 2 per stage of the best office within range. */
    static int reachNear(Level level, BlockPos pos) {
        int best = -1;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-RANGE, -4, -RANGE), pos.offset(RANGE, 4, RANGE))) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof Office) best = Math.max(best, s.getValue(STAGE));
        }
        return 3 + 2 * Math.max(best, 0);
    }

    /** Cargo crates a boat may carry near this position: 1 without an office, 2 plus one per stage with one. */
    static int crateLimit(Level level, BlockPos pos) {
        int best = -1;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-RANGE, -4, -RANGE), pos.offset(RANGE, 4, RANGE))) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof Office) best = Math.max(best, s.getValue(STAGE));
        }
        return best < 0 ? 1 : 2 + best;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            int s = state.getValue(STAGE);
            player.sendOverlayMessage(Component.literal("Harbour office, " + NAMES[s] + ": bollards within " + RANGE + " blocks reach " + (3 + 2 * s) + " blocks."));
        }
        return InteractionResult.SUCCESS;
    }
}
