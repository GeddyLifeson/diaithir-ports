package net.diaithir.elevators;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** A landing. Set it beside the shaft at each stop; use it to call the cabin here, or to send the cabin on when it is already here. */
final class FloorBlock extends Block {
    FloorBlock(Properties props) {
        super(props);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ControllerEntity c = ControllerEntity.findAbove(level, pos);
        String msg;
        if (c == null) msg = "No controller sits above the shaft beside this floor.";
        else if (c.isParkedAt(pos.getY())) msg = c.cycle(player.isShiftKeyDown());
        else msg = c.request(pos.getY());
        player.sendOverlayMessage(Component.literal(msg == null ? "The cabin is on its way." : msg));
        return InteractionResult.SUCCESS;
    }
}
