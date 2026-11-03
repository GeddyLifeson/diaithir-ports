package net.diaithir.carts;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;

/**
 * Stands over a rail with a container (chest, tank, barrel...) on top. Every 8 ticks it moves up to 8 items, or 250 mB of fluid, between the
 * container and a minecart standing under it: container to cart when loading, cart to container when unloading. Works with anything on a cart that
 * offers an item or fluid handler (chest and hopper carts, tank wagons from the yards). Redstone switches it off. Use it bare-handed to flip the mode.
 */
public final class CartLoader extends Block {
    public static final BooleanProperty UNLOAD = BooleanProperty.create("unload");
    public static final int INTERVAL = 8, ITEMS = 8, FLUID = 250;

    public CartLoader(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(UNLOAD, false));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(UNLOAD); }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (!level.isClientSide() && !old.is(this)) level.scheduleTick(pos, this, INTERVAL);
    }

    /** Moves what it can; returns the amount moved (items plus millibuckets). */
    public static int transfer(ServerLevel level, BlockPos pos, boolean unload) {
        List<AbstractMinecart> carts = level.getEntitiesOfClass(AbstractMinecart.class, new AABB(pos.below()).deflate(0.1));
        if (carts.isEmpty()) return 0;
        AbstractMinecart cart = carts.get(0);
        int moved = move(level.getCapability(Capabilities.Item.BLOCK, pos.above(), Direction.DOWN), cart.getCapability(Capabilities.Item.ENTITY_AUTOMATION, Direction.UP), unload, ITEMS);
        if (moved == 0) moved = move(level.getCapability(Capabilities.Fluid.BLOCK, pos.above(), Direction.DOWN), cart.getCapability(Capabilities.Fluid.ENTITY, Direction.UP), unload, FLUID);
        return moved;
    }

    private static <T extends Resource> int move(ResourceHandler<T> container, ResourceHandler<T> cart, boolean unload, int max) {
        if (container == null || cart == null) return 0;
        try (Transaction tx = Transaction.openRoot()) {
            int n = unload ? ResourceHandlerUtil.move(cart, container, r -> true, max, tx) : ResourceHandlerUtil.move(container, cart, r -> true, max, tx);
            tx.commit();
            return n;
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.hasNeighborSignal(pos)) transfer(level, pos, state.getValue(UNLOAD));
        level.scheduleTick(pos, this, INTERVAL);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockState next = state.cycle(UNLOAD);
            level.setBlock(pos, next, 3);
            player.sendOverlayMessage(Component.literal(next.getValue(UNLOAD) ? "Unloading: cart to container" : "Loading: container to cart"));
        }
        return InteractionResult.SUCCESS;
    }
}
