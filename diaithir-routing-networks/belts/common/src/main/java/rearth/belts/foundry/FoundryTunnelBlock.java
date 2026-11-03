package rearth.belts.foundry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class FoundryTunnelBlock extends HorizontalDirectionalBlock implements EntityBlock {
    
    private static final Map<UUID, PendingLink> PENDING = new HashMap<>();
    
    private record PendingLink(ResourceKey<Level> dimension, BlockPos pos) {
    }
    
    public FoundryTunnelBlock(BlockBehaviour.Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }
    
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        
        var candidate = world.getBlockEntity(pos, BlockEntitiesContent.FOUNDRY_TUNNEL.get());
        if (candidate.isEmpty() || !FoundryTiers.isUpgradeItem(stack)) return super.useItemOn(stack, state, world, pos, player, hand, hit);
        
        if (!world.isClientSide())
            candidate.get().upgrade(player, stack);
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        
        if (!player.isShiftKeyDown() || !player.getMainHandItem().isEmpty()) return super.useWithoutItem(state, world, pos, player, hit);
        if (world.isClientSide()) return InteractionResult.SUCCESS;
        
        var candidate = world.getBlockEntity(pos, BlockEntitiesContent.FOUNDRY_TUNNEL.get());
        if (candidate.isEmpty()) return super.useWithoutItem(state, world, pos, player, hit);
        var tunnel = candidate.get();
        
        if (tunnel.isLinked()) {
            tunnel.unlink(world);
            player.sendSystemMessage(Component.translatable("message.belts.tunnel_unlinked"));
            return InteractionResult.SUCCESS;
        }
        
        var pending = PENDING.remove(player.getUUID());
        if (pending == null || !pending.dimension().equals(world.dimension())) {
            PENDING.put(player.getUUID(), new PendingLink(world.dimension(), pos.immutable()));
            player.sendSystemMessage(Component.translatable("message.belts.tunnel_entry_stored"));
            return InteractionResult.SUCCESS;
        }
        
        if (pending.pos().equals(pos)) {
            player.sendSystemMessage(Component.translatable("message.belts.tunnel_cancelled"));
            return InteractionResult.SUCCESS;
        }
        
        var error = FoundryTunnelBlockEntity.tryLink(world, pending.pos(), pos);
        player.sendSystemMessage(Component.translatable(error == null ? "message.belts.tunnel_linked" : error));
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
        return simpleCodec(FoundryTunnelBlock::new);
    }
    
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FoundryTunnelBlockEntity(pos, state);
    }
    
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide()) return null;
        return (world1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof FoundryTunnelBlockEntity tunnel)
                tunnel.tick(world1, pos, state1);
        };
    }
    
    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        
        if (world.isClientSide()) return super.playerWillDestroy(world, pos, state, player);
        
        var tunnel = world.getBlockEntity(pos, BlockEntitiesContent.FOUNDRY_TUNNEL.get());
        tunnel.ifPresent(entity -> entity.unlink(world));
        
        return super.playerWillDestroy(world, pos, state, player);
    }
}
