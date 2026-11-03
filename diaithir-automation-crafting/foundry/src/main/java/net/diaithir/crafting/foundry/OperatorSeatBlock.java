package net.diaithir.crafting.foundry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

/**
 * Operator Seat: a worker's chair. Machines that need an operator draw seconds of work from seats in range through {@link Operators}.
 * The worker eats food: use the seat with food to feed it, or pipe food in. The lamp is lit while the worker has food or work in hand.
 */
public final class OperatorSeatBlock extends BaseEntityBlock {
    public static final MapCodec<OperatorSeatBlock> CODEC = simpleCodec(OperatorSeatBlock::new);
    public static final BooleanProperty STAFFED = BooleanProperty.create("staffed");

    public OperatorSeatBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(STAFFED, false));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new OperatorSeatBlockEntity(pos, state); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(STAFFED);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty() || FoodStore.nutrition(ItemResource.of(stack)) <= 0) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof OperatorSeatBlockEntity be) {
            int in;
            try (Transaction tx = Transaction.openRoot()) {
                in = be.food.view().insert(ItemResource.of(stack), stack.getCount(), tx);
                tx.commit();
            }
            if (in > 0) stack.shrink(in);
            player.sendOverlayMessage(Component.literal(be.describe()));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof OperatorSeatBlockEntity be) player.sendOverlayMessage(Component.literal(be.describe()));
        return InteractionResult.CONSUME;
    }
}
