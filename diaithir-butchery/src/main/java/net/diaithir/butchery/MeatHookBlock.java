package net.diaithir.butchery;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** A hook a carcass hangs on. Use a carcass on it to hang it; use a knife or cleaver to skin, gut and cut it; use an empty hand to take it down before work starts. */
public class MeatHookBlock extends BaseEntityBlock {
    public static final MapCodec<MeatHookBlock> CODEC = simpleCodec(MeatHookBlock::new);

    public MeatHookBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MeatHookEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof MeatHookEntity hook)) return InteractionResult.PASS;
        if (held.is(Butchery.CARCASS.get()) && hook.isEmpty()) {
            if (!level.isClientSide()) {
                hook.hang(held.copyWithCount(1));
                held.shrink(1);
                level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
            }
            return InteractionResult.SUCCESS;
        }
        if (!hook.isEmpty() && held.is(Butchery.TOOLS)) {
            if (!level.isClientSide() && hook.work((net.minecraft.server.level.ServerLevel) level, pos, player, held)) {
                held.hurtAndBreak(1, player, hand);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof MeatHookEntity hook) || hook.isEmpty()) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            if (hook.stage() > 0) {
                player.sendOverlayMessage(Component.translatable("message.diaithirbutchery.started"));
            } else {
                player.getInventory().placeItemBackInInventory(hook.takeDown());
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }
}
