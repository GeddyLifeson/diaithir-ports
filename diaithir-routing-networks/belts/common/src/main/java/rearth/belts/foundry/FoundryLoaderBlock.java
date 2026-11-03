package rearth.belts.foundry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import rearth.belts.BlockEntitiesContent;

import java.util.Objects;

public class FoundryLoaderBlock extends HorizontalDirectionalBlock implements EntityBlock {
    
    public static final EnumProperty<FoundryLaneMode> LANE = EnumProperty.create("lane", FoundryLaneMode.class);
    public static final IntegerProperty TIER = IntegerProperty.create("tier", FoundryTiers.MIN_TIER, FoundryTiers.MAX_TIER);
    
    public FoundryLoaderBlock(BlockBehaviour.Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState()
          .setValue(FACING, Direction.NORTH)
          .setValue(LANE, FoundryLaneMode.AUTO)
          .setValue(TIER, FoundryTiers.MIN_TIER));
    }
    
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        
        if (stack.isEmpty()) return super.useItemOn(stack, state, world, pos, player, hand, hit);
        
        if (FoundryTiers.isUpgradeItem(stack)) {
            if (!world.isClientSide()) {
                var tier = state.getValue(TIER);
                var upgraded = FoundryTiers.upgrade(tier, player, stack);
                if (upgraded != tier)
                    world.setBlockAndUpdate(pos, state.setValue(TIER, upgraded));
            }
            return InteractionResult.SUCCESS;
        }
        
        var candidate = world.getBlockEntity(pos, BlockEntitiesContent.FOUNDRY_LOADER.get());
        if (candidate.isEmpty()) return super.useItemOn(stack, state, world, pos, player, hand, hit);
        
        if (!world.isClientSide())
            candidate.get().assignFilter(stack, player);
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        
        if (!player.getMainHandItem().isEmpty()) return super.useWithoutItem(state, world, pos, player, hit);
        
        if (!world.isClientSide()) {
            if (player.isShiftKeyDown()) {
                world.getBlockEntity(pos, BlockEntitiesContent.FOUNDRY_LOADER.get()).ifPresent(entity -> entity.resetFilter(player));
            } else {
                var mode = state.getValue(LANE).next();
                world.setBlockAndUpdate(pos, state.setValue(LANE, mode));
                player.sendSystemMessage(Component.translatable("message.belts.foundry_lane",
                  Component.translatable("message.belts.foundry_lane." + mode.getSerializedName())));
            }
        }
        return InteractionResult.SUCCESS;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LANE, TIER);
    }
    
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return Objects.requireNonNull(super.getStateForPlacement(ctx)).setValue(FACING, ctx.getHorizontalDirection());
    }
    
    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return simpleCodec(FoundryLoaderBlock::new);
    }
    
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FoundryLoaderBlockEntity(pos, state);
    }
    
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide()) return null;
        return (world1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof FoundryLoaderBlockEntity loader)
                loader.tick(world1, pos, state1);
        };
    }
}
