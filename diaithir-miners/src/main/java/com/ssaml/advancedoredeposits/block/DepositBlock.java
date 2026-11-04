package com.ssaml.advancedoredeposits.block;

import com.mojang.serialization.MapCodec;
import com.ssaml.advancedoredeposits.AdvancedOreDeposits;
import com.ssaml.advancedoredeposits.DepositType;
import com.ssaml.advancedoredeposits.ModConfig;
import com.ssaml.advancedoredeposits.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import org.jetbrains.annotations.Nullable;

/** A finite ore-patch tile. Breaking it by hand yields a raw chunk and uses one cycle; it stays until empty. */
public class DepositBlock extends BaseEntityBlock {
    private final DepositType type;
    private final MapCodec<DepositBlock> codec;

    public DepositBlock(DepositType type, BlockBehaviour.Properties properties) {
        super(properties);
        this.type = type;
        this.codec = simpleCodec(props -> new DepositBlock(type, props));
    }

    public DepositType type() {
        return type;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return AdvancedOreDeposits.createDepositBlockEntity(pos, state);
    }

    @Override
    public void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion,
            java.util.function.BiConsumer<ItemStack, BlockPos> dropConsumer) {
        // patches survive explosions
    }

    public ItemStack chunkStack() {
        return new ItemStack(ModItems.rawChunk(type), ModConfig.CHUNK_DROP_COUNT.getAsInt());
    }

    /** Hand break: cancel the vanilla removal, pay out one chunk, keep the tile until it runs dry. */
    public static void onBreak(BreakBlockEvent event) {
        if (!(event.getState().getBlock() instanceof DepositBlock block) || !(event.getLevel() instanceof Level level)
                || level.isClientSide()) {
            return;
        }
        Player player = event.getPlayer();
        if (player.isCreative() || !(level.getBlockEntity(event.getPos()) instanceof DepositBlockEntity deposit)) {
            return;
        }
        event.setCanceled(true);
        if (deposit.remainingBlocks() > 0) {
            Block.popResource(level, event.getPos().above(), block.chunkStack());
        }
        if (!deposit.consumeMineCycle()) {
            level.removeBlock(event.getPos(), false);
        }
    }
}
