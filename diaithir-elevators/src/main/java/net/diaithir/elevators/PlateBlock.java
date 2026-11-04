package net.diaithir.elevators;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The parked cabin deck. While the cabin moves it is a display entity instead. Use it to ride to the next floor up, sneak for down. */
final class PlateBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0, 14, 0, 16, 16, 16);

    PlateBlock(Properties props) {
        super(props);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ControllerEntity c = ControllerEntity.findInColumn(level, pos);
        String msg = c == null ? "This cabin has lost its controller." : c.cycle(player.isShiftKeyDown());
        player.sendOverlayMessage(Component.literal(msg == null ? "Going " + (player.isShiftKeyDown() ? "down." : "up.") : msg));
        return InteractionResult.SUCCESS;
    }
}
