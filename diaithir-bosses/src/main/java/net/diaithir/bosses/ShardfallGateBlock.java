package net.diaithir.bosses;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** The Shardfall Gate (graph node win.gate). Use it to see what it still wants; sneak-use with an item to give it as a keystone. */
public class ShardfallGateBlock extends Block {
    public ShardfallGateBlock(Properties props) { super(props); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level instanceof ServerLevel sl) Gate.use(sl, pos, player, player.getMainHandItem());
        return InteractionResult.SUCCESS_SERVER;
    }
}
