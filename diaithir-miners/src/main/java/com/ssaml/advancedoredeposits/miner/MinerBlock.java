package com.ssaml.advancedoredeposits.miner;

import com.mojang.serialization.MapCodec;
import com.ssaml.advancedoredeposits.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class MinerBlock extends BaseEntityBlock {
    public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");

    private final MinerKind kind;
    private final MapCodec<MinerBlock> codec;

    public MinerBlock(MinerKind kind, BlockBehaviour.Properties properties) {
        super(properties);
        this.kind = kind;
        this.codec = simpleCodec(p -> new MinerBlock(kind, p));
        registerDefaultState(stateDefinition.any().setValue(ENABLED, false));
    }

    public MinerKind kind() {
        return kind;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(ENABLED);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MinerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide() ? null
                : createTickerHelper(type, ModBlockEntities.MINER.get(), MinerBlockEntity::serverTick);
    }

    /** Empty hand takes the output stack; any other item goes in as fuel (burner only). */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MinerBlockEntity miner) {
            ItemStack out = miner.takeOutput();
            if (!out.isEmpty()) {
                player.getInventory().placeItemBackInInventory(out);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (kind == MinerKind.BURNER && !level.isClientSide() && level.getBlockEntity(pos) instanceof MinerBlockEntity miner
                && miner.insertFuel(player, stack)) {
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
}
