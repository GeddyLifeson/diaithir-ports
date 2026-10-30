package net.diaithir.bridges;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** A converter: FACING is the output face, REVERSE picks the second direction. An empty hand flips the direction and reads the buffer. */
final class ConverterBlock extends Block implements EntityBlock {
    static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    static final BooleanProperty REVERSE = BooleanProperty.create("reverse");
    private final ConverterEntity.Kind kind;

    ConverterBlock(Properties props, ConverterEntity.Kind kind) {
        super(props);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(REVERSE, false));
    }

    ConverterEntity.Kind kind() { return kind; }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING, REVERSE); }

    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ConverterEntity(pos, state); }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != DiaithirBridges.CONVERTER.get()) return null;
        return (lvl, pos, st, be) -> ((ConverterEntity) be).serverTick();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof ConverterEntity be)) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) {
            level.setBlock(pos, state.cycle(REVERSE), 3);
            be.modeChanged();
            level.invalidateCapabilities(pos);
        }
        BlockState now = level.getBlockState(pos);
        player.sendSystemMessage(Component.literal(be.describe(now.getValue(REVERSE)) + (player.isShiftKeyDown() ? "" : " (sneak to flip)")));
        return InteractionResult.SUCCESS;
    }
}
