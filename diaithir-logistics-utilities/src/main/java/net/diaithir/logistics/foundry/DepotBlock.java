package net.diaithir.logistics.foundry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** Courier depot. Chests and barrels touching it are its stock. Use it bare-handed to see couriers out and items in stock. */
public final class DepotBlock extends BaseEntityBlock {
    public static final MapCodec<DepotBlock> CODEC = simpleCodec(DepotBlock::new);

    public DepotBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DepotBlockEntity(pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) level.scheduleTick(pos, this, Config.RUN_INTERVAL.get());
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof DepotBlockEntity be) be.work(level, level.getGameTime());
        level.scheduleTick(pos, this, Config.RUN_INTERVAL.get());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof DepotBlockEntity be && level instanceof ServerLevel sl) {
            player.sendOverlayMessage(Component.literal("Depot: " + be.couriersOut() + " of " + Config.COURIERS_PER_DEPOT.get() + " couriers out, "
                    + be.stockCount(sl) + " items in stock, reach " + Config.SERVICE_RADIUS.get()));
        }
        return InteractionResult.SUCCESS;
    }
}
