package net.diaithir.portable;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class StorageBlock extends BaseEntityBlock {
    public static final MapCodec<StorageBlock> CODEC = simpleCodec(p -> new StorageBlock(p, Tier.WOOD));
    private final Tier tier;

    public StorageBlock(Properties p, Tier tier) {
        super(p);
        this.tier = tier;
    }

    public Tier tier() {
        return tier;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StorageBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != DiaithirPortable.STORAGE_BE.get()) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof StorageBlockEntity s && lvl instanceof ServerLevel sl) s.tick(sl);
        };
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand,
            BlockHitResult hit) {
        if (held.getItem() instanceof DyeItem && level.getBlockEntity(pos) instanceof StorageBlockEntity be) {
            net.minecraft.world.item.DyeColor color = held.get(net.minecraft.core.component.DataComponents.DYE);
            if (color != null) {
                if (!level.isClientSide()) {
                    be.setColor(color.getTextColor());
                    if (!player.getAbilities().instabuild) held.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.useItemOn(held, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof StorageBlockEntity be && player instanceof net.minecraft.server.level.ServerPlayer sp) {
            sp.openMenu(be, buf -> {
                buf.writeVarInt(be.tier().ordinal());
                buf.writeBoolean(be.locked());
            });
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof StorageBlockEntity be) be.readFrom(stack);
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        ItemStack s = new ItemStack(this);
        if (includeData && level.getBlockEntity(pos) instanceof StorageBlockEntity be) be.writeTo(s);
        return s;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && !player.isCreative() && level.getBlockEntity(pos) instanceof StorageBlockEntity be) {
            ItemStack drop = new ItemStack(this);
            be.writeTo(drop);
            Block.popResource(level, pos, drop);
            be.main().clearContent();
            be.upgrades().clearContent();
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
