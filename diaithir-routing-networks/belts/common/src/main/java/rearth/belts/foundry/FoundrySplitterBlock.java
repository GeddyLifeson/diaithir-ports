package rearth.belts.foundry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import rearth.belts.BlockEntitiesContent;

import java.util.Objects;

public class FoundrySplitterBlock extends HorizontalDirectionalBlock implements EntityBlock {
    
    public FoundrySplitterBlock(BlockBehaviour.Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }
    
    // 0 = left output, 1 = right output, -1 = not an output face
    private static int outputIndex(Direction facing, Direction face) {
        if (face == facing.getCounterClockWise()) return 0;
        if (face == facing.getClockWise()) return 1;
        return -1;
    }
    
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        
        var candidate = world.getBlockEntity(pos, BlockEntitiesContent.FOUNDRY_SPLITTER.get());
        if (candidate.isEmpty() || stack.isEmpty()) return super.useItemOn(stack, state, world, pos, player, hand, hit);
        
        var entity = candidate.get();
        var output = outputIndex(state.getValue(FACING), hit.getDirection());
        if (output >= 0) {
            if (!world.isClientSide())
                entity.assignFilter(output, stack, player);
            return InteractionResult.SUCCESS;
        }
        
        if (FoundryTiers.isUpgradeItem(stack)) {
            if (!world.isClientSide())
                entity.upgrade(player, stack);
            return InteractionResult.SUCCESS;
        }
        
        return super.useItemOn(stack, state, world, pos, player, hand, hit);
    }
    
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        
        var candidate = world.getBlockEntity(pos, BlockEntitiesContent.FOUNDRY_SPLITTER.get());
        if (candidate.isEmpty() || !player.getMainHandItem().isEmpty()) return super.useWithoutItem(state, world, pos, player, hit);
        
        var entity = candidate.get();
        if (!world.isClientSide()) {
            var output = outputIndex(state.getValue(FACING), hit.getDirection());
            if (!player.isShiftKeyDown()) {
                entity.cycleInputPriority(player);
            } else if (output >= 0) {
                entity.resetFilter(output, player);
            } else {
                entity.cycleOutputPriority(player);
            }
        }
        return InteractionResult.SUCCESS;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return Objects.requireNonNull(super.getStateForPlacement(ctx)).setValue(FACING, ctx.getHorizontalDirection());
    }
    
    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return simpleCodec(FoundrySplitterBlock::new);
    }
    
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FoundrySplitterBlockEntity(pos, state);
    }
    
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide()) return null;
        return (world1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof FoundrySplitterBlockEntity splitter)
                splitter.tick(world1, pos, state1);
        };
    }
    
    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        
        if (world.isClientSide()) return super.playerWillDestroy(world, pos, state, player);
        
        var splitter = world.getBlockEntity(pos, BlockEntitiesContent.FOUNDRY_SPLITTER.get());
        splitter.ifPresent(entity -> entity.dropContent(world, pos));
        
        return super.playerWillDestroy(world, pos, state, player);
    }
}
