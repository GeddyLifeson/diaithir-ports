package net.diaithir.carts;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * Routing: a rail that gives a redstone signal of 15 while a minecart whose name matches its channel is on it. Channel 0 matches any cart;
 * channels 1 to 16 are the letters A to P, matched against the cart's name (name a cart "C" with a name tag). Use it bare-handed to cycle the channel.
 * Power a junction rail with it (a three-way rail flips when powered) so the carts you name take the branch and the rest go straight on.
 */
public class DestinationRail extends RailBlock implements RailSpeed {
    public static final MapCodec<RailBlock> CODEC = simpleCodec(DestinationRail::new);
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final IntegerProperty CHANNEL = IntegerProperty.create("channel", 0, 16);
    public static final int INTERVAL = 4;

    public DestinationRail(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(POWERED, false).setValue(CHANNEL, 0));
    }

    @Override public MapCodec<RailBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED, CHANNEL);
    }

    @Override public double maxSpeed() { return Tier.STEEL.maxSpeed; }
    @Override protected boolean isSignalSource(BlockState state) { return true; }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) { return state.getValue(POWERED) ? 15 : 0; }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) { return state.getValue(POWERED) && dir == Direction.UP ? 15 : 0; }

    public static String letter(int channel) { return channel == 0 ? "any" : String.valueOf((char) ('A' + channel - 1)); }

    public static boolean matches(int channel, AbstractMinecart cart) {
        if (channel == 0) return true;
        Component name = cart.getCustomName();
        return name != null && name.getString().trim().equalsIgnoreCase(letter(channel));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (!level.isClientSide() && !old.is(this)) level.scheduleTick(pos, this, INTERVAL);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.is(this)) return;
        List<AbstractMinecart> carts = level.getEntitiesOfClass(AbstractMinecart.class, new AABB(pos).deflate(0.2).setMaxY(pos.getY() + 0.9));
        int ch = state.getValue(CHANNEL);
        boolean on = carts.stream().anyMatch(c -> matches(ch, c));
        if (on != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, on), 3);
            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.below(), this);
        }
        level.scheduleTick(pos, this, INTERVAL);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            int next = (state.getValue(CHANNEL) + 1) % 17;
            level.setBlock(pos, state.setValue(CHANNEL, next).setValue(POWERED, false), 3);
            player.sendOverlayMessage(Component.literal(next == 0 ? "Any cart" : "Carts named " + letter(next)));
        }
        return InteractionResult.SUCCESS;
    }
}
