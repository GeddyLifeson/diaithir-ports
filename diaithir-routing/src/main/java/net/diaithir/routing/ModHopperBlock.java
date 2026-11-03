package net.diaithir.routing;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** The hopper family. All four kinds are one block class facing a direction; the kind decides speed, filtering and pickup. */
public final class ModHopperBlock extends BaseEntityBlock {
    public static final MapCodec<ModHopperBlock> CODEC = simpleCodec(p -> new ModHopperBlock(p, HopperKind.FILTER));
    public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class);

    private final HopperKind kind;

    public ModHopperBlock(Properties properties, HopperKind kind) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN));
    }

    public HopperKind kind() {
        return kind;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction d = context.getClickedFace().getOpposite();
        return defaultBlockState().setValue(FACING, d);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ModHopperBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, RoutingRefs.hopperBe(), (l, p, s, be) -> be.serverTick((ServerLevel) l, s));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ModHopperBlockEntity be)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (stack.getItem() instanceof FilterItem && kind != HopperKind.FAST) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            be.setFilter(FilterItem.read(stack));
            player.sendOverlayMessage(Component.literal("Hopper filter: " + be.filter().describe()));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof ModHopperBlockEntity be)) return InteractionResult.PASS;
        if (kind == HopperKind.COLLECTOR) {
            be.cycleRange();
            player.sendOverlayMessage(Component.literal("Collector range " + be.collectRange()));
        } else {
            player.sendOverlayMessage(Component.literal(kind + " hopper, filter " + be.filter().describe()));
        }
        return InteractionResult.SUCCESS;
    }
}
