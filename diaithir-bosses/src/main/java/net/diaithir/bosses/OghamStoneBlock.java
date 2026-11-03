package net.diaithir.bosses;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/** A standing stone carved with one of nine runes. Use to turn it to the next rune; sneak-use to seal the rune it shows into a rite. */
public class OghamStoneBlock extends Block {
    public static final IntegerProperty RUNE = IntegerProperty.create("rune", 0, Ritual.RUNES.length - 1);

    public OghamStoneBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(RUNE, 0));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(RUNE); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        int r = state.getValue(RUNE);
        if (player.isShiftKeyDown()) {
            if (level instanceof ServerLevel sl) Ritual.seal(sl, pos, r, player);
        } else {
            r = (r + 1) % Ritual.RUNES.length;
            level.setBlock(pos, state.setValue(RUNE, r), 3);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(Ritual.RUNES[r] + " " + Ritual.TREES[r]));
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}
