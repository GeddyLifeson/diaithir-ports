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

/**
 * A locking rail stops a cart that rolls onto it and keeps it there. Two modes, cycled bare-handed:
 * LATCH holds the cart until the rail goes from unpowered to powered (a pulse); a rail that was already powered when the cart arrived does not let it go.
 * INTERLOCK holds the cart until the track ahead (up to 32 rails) is clear of other carts, then lets it go by itself.
 */
public class LockingRail extends PoweredRailBlock implements RailSpeed {
    public static final MapCodec<PoweredRailBlock> CODEC = simpleCodec(LockingRail::new);
    public static final IntegerProperty MODE = IntegerProperty.create("mode", 0, 1);
    public static final String[] NAMES = {"Latch: holds until a redstone pulse", "Interlock: holds until the track ahead is clear"};
    public static final int AHEAD = 32;

    public LockingRail(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(MODE, 0));
    }

    @Override public MapCodec<PoweredRailBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MODE);
    }

    @Override public double maxSpeed() { return Tier.STEEL.maxSpeed; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        int next = (state.getValue(MODE) + 1) % NAMES.length;
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(MODE, next), 3);
            player.sendOverlayMessage(Component.literal(NAMES[next]));
        }
        return InteractionResult.SUCCESS;
    }
}
