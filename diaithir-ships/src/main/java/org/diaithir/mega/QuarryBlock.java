package org.diaithir.mega;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import org.diaithir.mega.cleanroom.DrillHead;

/** Quarry: clean-room cursor (mega.cleanroom.QuarryPlan) driven by a catalog machine's dig rate and a drill head tier. */
public class QuarryBlock extends BaseEntityBlock {
    public static final MapCodec<QuarryBlock> CODEC = simpleCodec(QuarryBlock::new);
    public QuarryBlock(Properties p) { super(p); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new QuarryBlockEntity(pos, state); }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != MegaBlocks.QUARRY_BE.get()) return null;
        return (lv, p, st, be) -> ((QuarryBlockEntity) be).serverTick();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        DrillHead tier = MegaBlocks.headOf(held);
        if (tier == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof QuarryBlockEntity q && q.installHead(tier)) {
            if (!player.getAbilities().instabuild) held.shrink(1);
            player.sendOverlayMessage(Component.translatable("megamachines.quarry.head_fitted", tier.name()));
            return InteractionResult.CONSUME;
        }
        player.sendOverlayMessage(Component.translatable("megamachines.quarry.head_present"));
        return InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof QuarryBlockEntity q) {
            int moved = q.give(player);
            player.sendOverlayMessage(Component.translatable("megamachines.quarry.status." + q.status().name().toLowerCase(), moved, q.dug(), q.total()));
        }
        return InteractionResult.CONSUME;
    }
}
