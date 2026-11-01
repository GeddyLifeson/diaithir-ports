package net.diaithir.carts;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/** A station rail: a cart that arrives is stopped and held until the rail is powered, or until its timer runs out. Use with an empty hand to cycle the timer. */
public class StationRail extends PoweredRailBlock implements RailSpeed {
    public static final MapCodec<PoweredRailBlock> CODEC = simpleCodec(StationRail::new);
    public static final IntegerProperty DELAY = IntegerProperty.create("delay", 0, 4);
    /** Seconds a cart waits, by DELAY. 0 means it waits for a redstone signal only. */
    public static final int[] SECONDS = {0, 2, 5, 15, 60};

    public StationRail(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(DELAY, 0));
    }

    @Override
    public MapCodec<PoweredRailBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(DELAY);
    }

    @Override
    public double maxSpeed() {
        return Tier.STEEL.maxSpeed;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        int next = (state.getValue(DELAY) + 1) % SECONDS.length;
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(DELAY, next), 3);
            player.sendOverlayMessage(Component.literal(SECONDS[next] == 0 ? "Holds until a redstone signal" : "Holds " + SECONDS[next] + " s, or until a redstone signal"));
        }
        return InteractionResult.SUCCESS;
    }
}
