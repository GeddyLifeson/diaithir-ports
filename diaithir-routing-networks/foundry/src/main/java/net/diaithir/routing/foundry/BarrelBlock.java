package net.diaithir.routing.foundry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

/**
 * Day Barrel: one kind of item, up to 64 stacks. Use with an item to store it, empty-handed to take a stack (sneak: one item).
 * Pipes and inserters put items in from the top and sides and take them from the bottom.
 */
public final class BarrelBlock extends BaseEntityBlock {
    public static final MapCodec<BarrelBlock> CODEC = simpleCodec(BarrelBlock::new);

    public BarrelBlock(Properties properties) {
        super(properties);
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new BarrelBlockEntity(pos, state); }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, net.minecraft.core.Direction direction) {
        if (!(level.getBlockEntity(pos) instanceof BarrelBlockEntity be) || be.store.resource().isEmpty()) return 0;
        return 1 + (int) (14L * be.store.count() / BarrelStore.capacity(be.store.resource()));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof BarrelBlockEntity be) {
            ItemResource r = ItemResource.of(stack);
            int in;
            try (Transaction tx = Transaction.openRoot()) {
                in = be.store.view(true, false).insert(r, stack.getCount(), tx);
                tx.commit();
            }
            if (in > 0) stack.shrink(in);
            player.sendOverlayMessage(Component.literal(be.describe()));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof BarrelBlockEntity be) {
            ItemResource r = be.store.resource();
            if (!r.isEmpty()) {
                int want = player.isShiftKeyDown() ? 1 : r.getMaxStackSize();
                int out;
                try (Transaction tx = Transaction.openRoot()) {
                    out = be.store.view(false, true).extract(r, want, tx);
                    tx.commit();
                }
                if (out > 0) player.getInventory().placeItemBackInInventory(r.toStack(out));
            }
            player.sendOverlayMessage(Component.literal(be.describe()));
        }
        return InteractionResult.CONSUME;
    }
}
