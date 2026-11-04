// SPDX-License-Identifier: LGPL-3.0-only
// Design derived from Not Logistics Pipes (c) Contributors to Not Logistics Pipes <https://nlp.rs485.network>, reimplemented in Java for NeoForge 26.1.2 (Diaithir).
package net.diaithir.foundry.nlp;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * A routed pipe. Pipes join every adjacent pipe and show an arm to inventories. Sink pipes take items by priority and filter (use an item to
 * list it, a stick to change priority, bare hand while sneaking to take everything), extractor pipes pull from touching inventories, speed pipes are faster.
 */
public final class LogisticsPipeBlock extends BaseEntityBlock {
    public static final MapCodec<LogisticsPipeBlock> CODEC = simpleCodec(p -> new LogisticsPipeBlock(p, PipeKind.PLAIN));
    public static final Map<Direction, BooleanProperty> CONN = PipeBlock.PROPERTY_BY_DIRECTION;

    private static final VoxelShape CORE = box(5, 5, 5, 11, 11, 11);
    private static final VoxelShape[] SHAPES = new VoxelShape[64];

    static {
        VoxelShape[] arms = {
                box(5, 0, 5, 11, 5, 11), box(5, 11, 5, 11, 16, 11), box(5, 5, 0, 11, 11, 5),
                box(5, 5, 11, 11, 11, 16), box(0, 5, 5, 5, 11, 11), box(11, 5, 5, 16, 11, 11)};
        for (int m = 0; m < 64; m++) {
            VoxelShape s = CORE;
            for (Direction d : Direction.values()) if ((m & (1 << d.ordinal())) != 0) s = Shapes.or(s, arms[d.ordinal()]);
            SHAPES[m] = s;
        }
    }

    private final PipeKind kind;

    public LogisticsPipeBlock(Properties properties, PipeKind kind) {
        super(properties);
        this.kind = kind;
        BlockState s = stateDefinition.any();
        for (Direction d : Direction.values()) s = s.setValue(CONN.get(d), false);
        registerDefaultState(s);
    }

    public PipeKind kind() {
        return kind;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        for (Direction d : Direction.values()) builder.add(CONN.get(d));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    private static boolean connects(BlockState neighbour) {
        return neighbour.getBlock() instanceof LogisticsPipeBlock || neighbour.hasBlockEntity();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState s = defaultBlockState();
        for (Direction d : Direction.values()) s = s.setValue(CONN.get(d), connects(level.getBlockState(pos.relative(d))));
        return s;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighbourPos,
            BlockState neighbourState, RandomSource random) {
        return state.setValue(CONN.get(dir), connects(neighbourState));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int m = 0;
        for (Direction d : Direction.values()) if (state.getValue(CONN.get(d))) m |= 1 << d.ordinal();
        return SHAPES[m];
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PipeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != LogisticsMod.PIPE_BE.get()) return null;
        return (l, p, s, be) -> ((PipeBlockEntity) be).serverTick((net.minecraft.server.level.ServerLevel) l);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (kind != PipeKind.SINK || stack.isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof PipeBlockEntity be) {
            SinkConfig c = be.sink();
            String msg;
            if (stack.is(Items.STICK)) {
                c.priority = player.isShiftKeyDown() ? Math.max(0, c.priority - 10) : Math.min(100, c.priority + 10);
                msg = "Priority " + c.priority;
            } else {
                msg = c.toggle(stack) ? "Takes " + stack.getHoverName().getString() : "No longer takes " + stack.getHoverName().getString();
            }
            be.setChanged();
            player.sendOverlayMessage(Component.literal(msg + ". " + c.describe()));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof PipeBlockEntity be) {
            if (kind == PipeKind.SINK && player.isShiftKeyDown()) {
                be.sink().all = !be.sink().all;
                be.setChanged();
            }
            String text = kind == PipeKind.SINK ? "Sink pipe: " + be.sink().describe() : kind.name().toLowerCase() + " pipe";
            player.sendOverlayMessage(Component.literal(text + ", carrying " + be.travelling()));
        }
        return InteractionResult.SUCCESS;
    }
}
