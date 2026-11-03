package net.diaithir.carts;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * Cart loader. Its front faces the rail, its back faces a container (chest, barrel, hopper...). In load mode it moves items from that container into a
 * container cart (chest, hopper, furnace-less carts with an inventory) standing on the rail in front; in unload mode the other way. One item stack per
 * 8 ticks; a redstone signal switches it off. Use it bare-handed to switch mode. Clean-room: written from the behaviour only, with vanilla containers.
 */
public class CartLoader extends HorizontalDirectionalBlock {
    public static final MapCodec<CartLoader> CODEC = simpleCodec(CartLoader::new);
    public static final BooleanProperty UNLOAD = BooleanProperty.create("unload");
    public static final int PERIOD = 8;

    public CartLoader(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(UNLOAD, false).setValue(BlockStateProperties.POWERED, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, UNLOAD, BlockStateProperties.POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection()).setValue(BlockStateProperties.POWERED, ctx.getLevel().hasNeighborSignal(ctx.getClickedPos()));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (!level.isClientSide() && !old.is(state.getBlock())) level.scheduleTick(pos, this, PERIOD);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, net.minecraft.world.level.redstone.Orientation orientation, boolean moved) {
        if (level.isClientSide()) return;
        boolean p = level.hasNeighborSignal(pos);
        if (p != state.getValue(BlockStateProperties.POWERED)) level.setBlock(pos, state.setValue(BlockStateProperties.POWERED, p), 2);
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

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(BlockStateProperties.POWERED)) run(state, level, pos);
        level.scheduleTick(pos, this, PERIOD);
    }

    /** The container cart in front of the loader, or null. */
    static Container cartAt(Level level, BlockPos front) {
        AABB box = new AABB(front);
        List<AbstractMinecart> carts = level.getEntitiesOfClass(AbstractMinecart.class, box, c -> c instanceof Container);
        return carts.isEmpty() ? null : (Container) carts.get(0);
    }

    /** Moves one stack (or the part of it that fits). Returns true if anything moved. */
    static boolean run(BlockState state, ServerLevel level, BlockPos pos) {
        Direction f = state.getValue(FACING);
        Container cart = cartAt(level, pos.relative(f));
        Container box = HopperBlockEntity.getContainerAt(level, pos.relative(f.getOpposite()));
        if (cart == null || box == null) return false;
        return state.getValue(UNLOAD) ? move(cart, box) : move(box, cart);
    }

    public static boolean move(Container from, Container to) {
        for (int i = 0; i < from.getContainerSize(); i++) {
            ItemStack s = from.getItem(i);
            if (s.isEmpty()) continue;
            int before = s.getCount();
            ItemStack rest = insert(to, s.copy());
            if (rest.getCount() != before) {
                from.setItem(i, rest.isEmpty() ? ItemStack.EMPTY : rest);
                from.setChanged();
                to.setChanged();
                return true;
            }
        }
        return false;
    }

    private static ItemStack insert(Container to, ItemStack s) {
        for (int pass = 0; pass < 2 && !s.isEmpty(); pass++) {
            for (int j = 0; j < to.getContainerSize() && !s.isEmpty(); j++) {
                ItemStack t = to.getItem(j);
                if (pass == 0 ? (!t.isEmpty() && ItemStack.isSameItemSameComponents(t, s) && t.getCount() < Math.min(t.getMaxStackSize(), to.getMaxStackSize()))
                        : (t.isEmpty() && to.canPlaceItem(j, s))) {
                    if (pass == 0) {
                        int n = Math.min(s.getCount(), Math.min(t.getMaxStackSize(), to.getMaxStackSize()) - t.getCount());
                        t.grow(n);
                        s.shrink(n);
                    } else {
                        int n = Math.min(s.getCount(), Math.min(s.getMaxStackSize(), to.getMaxStackSize()));
                        to.setItem(j, s.split(n));
                    }
                }
            }
        }
        return s;
    }
}
