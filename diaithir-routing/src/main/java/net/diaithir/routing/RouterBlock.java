package net.diaithir.routing;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** The router. Hold a module to install it, a range card to upgrade range, a filter to attach it, or use empty-handed to list contents; sneak with an empty hand pops the last module. */
public final class RouterBlock extends BaseEntityBlock {
    public static final MapCodec<RouterBlock> CODEC = simpleCodec(RouterBlock::new);

    public RouterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RouterBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, RoutingRefs.routerBe(), (l, p, s, be) -> be.serverTick((ServerLevel) l));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof RouterBlockEntity router)) return InteractionResult.PASS;
        if (level.isClientSide()) return stack.isEmpty() ? InteractionResult.PASS : InteractionResult.SUCCESS;
        if (stack.getItem() instanceof ModuleItem m) {
            boolean ok = router.installModule(m.kind(), stack.getOrDefault(RoutingRefs.filterData(), FilterData.EMPTY));
            if (ok && !player.hasInfiniteMaterials()) stack.shrink(1);
            player.sendOverlayMessage(Component.literal(ok ? "Installed " + m.kind().id() + " module" : "Router is full (9 modules)"));
            return InteractionResult.SUCCESS;
        }
        if (RoutingRefs.isRangeCard(stack)) {
            router.addRangeCard();
            if (!player.hasInfiniteMaterials()) stack.shrink(1);
            player.sendOverlayMessage(Component.literal("Range " + router.range() + " blocks"));
            return InteractionResult.SUCCESS;
        }
        if (stack.getItem() instanceof FilterItem) {
            router.setFilter(FilterItem.read(stack));
            player.sendOverlayMessage(Component.literal("Router filter: " + router.filter().describe()));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof RouterBlockEntity router)) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) {
            ItemStack back = router.popModule();
            if (!back.isEmpty()) {
                if (!player.getInventory().add(back)) player.drop(back, false);
                player.sendOverlayMessage(Component.literal("Module removed"));
            }
        } else {
            player.sendOverlayMessage(Component.literal(router.describe()));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof RouterBlockEntity router) router.dropContents((ServerLevel) level, pos);
        return super.playerWillDestroy(level, pos, state, player);
    }
}
