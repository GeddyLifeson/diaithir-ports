package net.diaithir.unrest;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** The Meter. Use it to read the Unrest of its chunk and of the four around it. */
public final class MeterBlock extends Block {
    public MeterBlock(Properties p) {
        super(p);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level instanceof ServerLevel sl) {
            Field f = Unrest.field(sl);
            ChunkPos c = ChunkPos.containing(pos);
            player.sendOverlayMessage(Component.translatable("diaithirunrest.meter", Math.round(f.get(c.x(), c.z())),
                    Math.round(f.get(c.x(), c.z() - 1)), Math.round(f.get(c.x() + 1, c.z())), Math.round(f.get(c.x(), c.z() + 1)), Math.round(f.get(c.x() - 1, c.z()))));
        }
        return InteractionResult.SUCCESS;
    }
}
