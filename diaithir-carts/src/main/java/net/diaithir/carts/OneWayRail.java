package net.diaithir.carts;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A rail that lets carts through one way only: north or east normally, south or west when reversed (use it bare-handed to flip it).
 * A cart that rolls onto it against the arrow is turned back. Place it on a straight stretch; on a corner it does nothing.
 */
public class OneWayRail extends RailBlock implements RailSpeed {
    public static final MapCodec<RailBlock> CODEC = simpleCodec(OneWayRail::new);
    public static final BooleanProperty REVERSED = BooleanProperty.create("reversed");

    public OneWayRail(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(REVERSED, false));
    }

    @Override public MapCodec<RailBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(REVERSED);
    }

    @Override public double maxSpeed() { return Tier.STEEL.maxSpeed; }

    /** The way carts may travel on this rail, or null on a corner. */
    public static Vec3 allowed(BlockState state) {
        RailShape s = state.getValue(SHAPE);
        Vec3 v;
        switch (s) {
            case NORTH_SOUTH, ASCENDING_NORTH, ASCENDING_SOUTH -> v = new Vec3(0, 0, -1);
            case EAST_WEST, ASCENDING_EAST, ASCENDING_WEST -> v = new Vec3(1, 0, 0);
            default -> { return null; }
        }
        return state.getValue(REVERSED) ? v.scale(-1) : v;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockState next = state.cycle(REVERSED);
            level.setBlock(pos, next, 3);
            Vec3 a = allowed(next);
            String word = a == null ? "none (corner)" : a.z < 0 ? "north" : a.z > 0 ? "south" : a.x > 0 ? "east" : "west";
            player.sendOverlayMessage(Component.literal("Carts may go " + word));
        }
        return InteractionResult.SUCCESS;
    }
}
