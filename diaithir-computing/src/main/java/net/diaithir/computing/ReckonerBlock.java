package net.diaithir.computing;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

import java.util.stream.Collectors;

/** The Reckoner. A book and quill or written book used on it becomes its program; an empty hand runs it; a rising redstone signal on any side runs it. */
public final class ReckonerBlock extends BaseEntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final MapCodec<ReckonerBlock> CODEC = simpleCodec(ReckonerBlock::new);

    public ReckonerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(POWERED); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ReckonerEntity(pos, state); }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, Registry.RECKONER_ENTITY.get(), ReckonerEntity::tick);
    }
    @Override protected boolean isSignalSource(BlockState state) { return true; }

    @Override
    protected int getSignal(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, Direction dir) {
        // dir points from this block towards the reader, so the side asked about is the opposite one
        return level.getBlockEntity(pos) instanceof ReckonerEntity be ? be.output[dir.getOpposite().get3DDataValue()] : 0;
    }

    static @Nullable String bookText(ItemStack stack) {
        var writable = stack.get(DataComponents.WRITABLE_BOOK_CONTENT);
        if (writable != null) return writable.getPages(false).collect(Collectors.joining("\n"));
        var written = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (written != null) return written.getPages(false).stream().map(Component::getString).collect(Collectors.joining("\n"));
        return null;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return loadBook(stack, level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return toggle(level, pos, player);
    }

    static InteractionResult loadBook(ItemStack stack, Level level, BlockPos pos, Player player) {
        if (!stack.is(Items.WRITABLE_BOOK) && !stack.is(Items.WRITTEN_BOOK)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        String text = bookText(stack);
        if (text != null && level.getBlockEntity(pos) instanceof ComputerEntity be) {
            player.sendOverlayMessage(Component.literal(be.setProgram(text) ? "Program loaded (" + text.length() + " characters)." : "That program is longer than " + ComputerEntity.MAX_PROGRAM + " characters."));
        }
        return InteractionResult.CONSUME;
    }

    /** Empty hand: runs the program, or stops it if it is still running. */
    static InteractionResult toggle(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof ComputerEntity be) {
            if (be.running()) { be.stop(); player.sendOverlayMessage(Component.literal("Stopped.")); return InteractionResult.CONSUME; }
            if (be.program.isEmpty()) { player.sendOverlayMessage(Component.literal("No program. Use a book and quill on it.")); return InteractionResult.CONSUME; }
            String err = be.run();
            be.log.forEach(l -> player.sendSystemMessage(Component.literal("[Computer] " + l)));
            if (err != null) player.sendSystemMessage(Component.literal("[Computer] error: " + err));
            else if (be.running()) player.sendOverlayMessage(Component.literal("Running. Use it again to stop."));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (level.isClientSide()) return;
        boolean powered = false;
        for (Direction d : Direction.values()) if (level.getSignal(pos.relative(d), d) > 0) { powered = true; break; }
        if (powered == state.getValue(POWERED)) return;
        level.setBlock(pos, state.setValue(POWERED, powered), 2);
        if (powered && level.getBlockEntity(pos) instanceof ComputerEntity be && !be.program.isEmpty()) be.run();
    }
}
