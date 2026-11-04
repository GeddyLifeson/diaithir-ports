package net.diaithir.bosses;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The altar of the Ascendant tier. It opens a rite (see Ritual): a champion token starts the next boss you have not beaten, a boss's
 * trophy starts a rematch. Use it again, with an offering or bare-handed, to work through the steps.
 */
public class AscendantAltarBlock extends Block {
    public AscendantAltarBlock(Properties props) { super(props); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level instanceof ServerLevel sl) activate(sl, pos, player);
        return InteractionResult.SUCCESS_SERVER;
    }

    private static void activate(ServerLevel sl, BlockPos pos, Player player) {
        Ritual.use(sl, pos, player, player.getMainHandItem());
    }
}
