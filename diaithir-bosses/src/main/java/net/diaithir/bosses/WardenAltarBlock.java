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

/**
 * The altar of the Age Wardens: a stone altar in Age I that takes on the look of each Age as the world reaches it (the state
 * follows the world's Age whenever someone uses it). It opens the rite of the first Warden you have not beaten whose Age has come;
 * a Warden's trophy starts a rematch. The rites are plain boss rites (ritual/age_warden_N.json) run by {@link Ritual}.
 */
public class WardenAltarBlock extends Block {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 1, Ages.RODOS);

    public WardenAltarBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(AGE, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(AGE); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level instanceof ServerLevel sl) {
            int age = Ages.current(sl.getServer());
            if (state.getValue(AGE) != age) level.setBlock(pos, state.setValue(AGE, age), 3);
            Ritual.use(sl, pos, player, player.getMainHandItem());
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}
