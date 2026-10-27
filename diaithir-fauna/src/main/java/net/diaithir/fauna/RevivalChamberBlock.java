package net.diaithir.fauna;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Use a studied, cleaned fossil on the chamber (matrix in the other hand) to revive the animal on top of it. */
public class RevivalChamberBlock extends Block {
    public RevivalChamberBlock(Properties props) {
        super(props);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || !stack.is(Fossils.CLEANED.get())) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level instanceof ServerLevel sl && player instanceof ServerPlayer sp) Revival.revive(sp, sl, pos.above());
        return InteractionResult.SUCCESS;
    }
}
