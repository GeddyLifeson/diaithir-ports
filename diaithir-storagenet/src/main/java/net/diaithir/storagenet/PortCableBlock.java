package net.diaithir.storagenet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * Import, export and filter cables. Import and export cables run on a scheduled tick every few ticks (config), not every tick.
 * Use an item on one to add it to or remove it from its filter, a stick to flip whitelist and blacklist, sneak with an empty hand to clear it.
 */
public final class PortCableBlock extends CableBlock implements EntityBlock {
    public PortCableBlock(Properties properties, CableKind kind) {
        super(properties, kind);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PortCableBlockEntity(pos, state);
    }

    private boolean works() {
        return kind() == CableKind.IMPORT || kind() == CableKind.EXPORT;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (works() && !level.isClientSide()) level.scheduleTick(pos, this, Config.OPERATION_INTERVAL.get());
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!works()) return;
        if (level.getBlockEntity(pos) instanceof PortCableBlockEntity be) be.work(level);
        level.scheduleTick(pos, this, Config.OPERATION_INTERVAL.get());
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof PortCableBlockEntity be) {
            String msg;
            if (stack.is(Items.STICK)) {
                be.filter().toggleMode();
                msg = "Now a " + (be.filter().blacklist() ? "blacklist" : "whitelist");
            } else {
                msg = be.filter().toggle(stack);
            }
            be.setChanged();
            player.sendOverlayMessage(Component.literal(msg + ". " + be.filter().describe()));
            Networks.markDirty(level);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof PortCableBlockEntity be) {
            if (player.isShiftKeyDown()) {
                be.filter().clear();
                be.setChanged();
                player.sendOverlayMessage(Component.literal("Filter cleared"));
            } else {
                player.sendOverlayMessage(Component.literal(kind().name().toLowerCase() + " cable: " + be.filter().describe()));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
