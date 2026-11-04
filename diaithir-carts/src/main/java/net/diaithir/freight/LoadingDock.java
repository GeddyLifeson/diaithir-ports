package net.diaithir.freight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * A dock faces a container (up to three blocks ahead) and offers its cargo as an ordinary item and fluid handler, so storage network cables, hoppers, pipes,
 * circuit sensors and depot workers use it with no extra code. The loading dock (Age V) serves the container it faces. The rift dock (Age VIII) serves the
 * container at its partner dock, which is any other rift dock on the same channel in any dimension: one side loads, the other unloads. Bare hand on a rift dock
 * cycles the channel (sneak goes back). The comparator reads how full the container is.
 */
public final class LoadingDock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public final boolean rift;

    public LoadingDock(Properties props, boolean rift) {
        super(props);
        this.rift = rift;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING); }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new DockBlockEntity(pos, state); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!rift) return InteractionResult.PASS;
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof DockBlockEntity be) {
            player.sendOverlayMessage(Component.literal("Rift channel " + be.cycleChannel(player.isShiftKeyDown() ? -1 : 1)));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != Freight.DOCK_BE.get()) return null;
        return (l, pos, st, be) -> ((DockBlockEntity) be).serverTick();
    }

    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction dir) {
        return level.getBlockEntity(pos) instanceof DockBlockEntity be ? be.fillSignal() : 0;
    }
}
