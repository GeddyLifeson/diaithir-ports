package vazkii.botania.slice;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ManaPoolBlock extends Block implements EntityBlock {
    /** Mana in one pinch of powder, same as magic_fuel.csv. */
    public static final int POWDER_MANA = 40;

    public ManaPoolBlock(Properties props) { super(props); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ManaPoolBlockEntity(pos, state); }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ManaPoolBlockEntity pool)) return InteractionResult.PASS;
        if (stack.is(BotaniaSlice.MANA_POWDER.get())) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                int got = pool.energy().insert(POWDER_MANA * BotaniaSlice.J_PER_MANA, tx);
                if (got == 0) return InteractionResult.FAIL;
                tx.commit();
            }
            stack.consume(1, player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof ManaPoolBlockEntity pool)
            player.sendSystemMessage(Component.literal("Mana: " + BotaniaSlice.mana(pool.energy().getAmountAsInt()) + " / "
                    + ManaPoolBlockEntity.CAPACITY_MANA));
        return InteractionResult.SUCCESS;
    }
}
