package net.diaithir.routing.foundry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

/**
 * Sieve: sits on the face of an inventory (it points at the block it was placed against) and shows only the items that pass its filter
 * to everything else that touches it. A redstone signal inverts it. Sneak-use with an item adds it to the filter, sneak-use empty-handed clears it.
 */
public final class SieveBlock extends BaseEntityBlock {
    public static final MapCodec<SieveBlock> CODEC = simpleCodec(SieveBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public SieveBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING); }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new SieveBlockEntity(pos, state); }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getClickedFace().getOpposite());
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty() || !player.isShiftKeyDown()) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof SieveBlockEntity be) {
            boolean added = be.filter.add(ItemResource.of(stack));
            be.setChanged();
            player.sendOverlayMessage(Component.literal((added ? "Added. " : "Not added. ") + be.describe()));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof SieveBlockEntity be) {
            if (player.isShiftKeyDown()) {
                be.filter.clear();
                be.setChanged();
            }
            player.sendOverlayMessage(Component.literal(be.describe()));
        }
        return InteractionResult.CONSUME;
    }
}
