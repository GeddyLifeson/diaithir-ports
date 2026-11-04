package net.diaithir.storagenet;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * Wireless transmitter: a network block with a frequency. Every network that has a transmitter on the same frequency sees the others' storage,
 * in any dimension. Use bare-handed to read the frequency and step it up; sneak to step it down.
 */
public final class WirelessBlock extends BaseEntityBlock implements NetBlock {
    public static final MapCodec<WirelessBlock> CODEC = simpleCodec(WirelessBlock::new);

    public WirelessBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WirelessBlockEntity(pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        Networks.markDirtyEverywhere();
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        Networks.markDirtyEverywhere();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof WirelessBlockEntity be) {
            be.setFrequency(be.frequency() + (player.isShiftKeyDown() ? -1 : 1));
            int others = Links.count(be.frequency()) - 1;
            player.sendOverlayMessage(Component.literal("Frequency " + be.frequency() + ", " + others + " other transmitter" + (others == 1 ? "" : "s") + " loaded on it"));
        }
        return InteractionResult.SUCCESS;
    }
}
