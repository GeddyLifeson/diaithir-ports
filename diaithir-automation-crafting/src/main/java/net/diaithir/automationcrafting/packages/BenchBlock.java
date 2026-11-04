package net.diaithir.automationcrafting.packages;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.diaithir.automationcrafting.DiaithirAutomationCrafting;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * Researcher, Packager or Unpackager. No GUI, like the original: hoppers and pipes feed it; by hand, use items on it, use the Package Reader to pick
 * the package, and use it empty-handed to take what it made (sneak to read its status).
 */
public final class BenchBlock extends BaseEntityBlock {
    public static final MapCodec<BenchBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(propertiesCodec(), Kind.CODEC.fieldOf("kind").forGetter(b -> b.kind)).apply(i, BenchBlock::new));
    public final Kind kind;

    public BenchBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
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
        return new BenchEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, DiaithirAutomationCrafting.BENCH_BE.get(), BenchEntity::tick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BenchEntity be)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (stack.is(DiaithirAutomationCrafting.READER.get())) {
            if (!level.isClientSide()) player.sendOverlayMessage(Component.literal(player.isShiftKeyDown() ? be.clear() : be.cycle()));
            return InteractionResult.SUCCESS;
        }
        if (be.insertByHand(stack)) return InteractionResult.SUCCESS;
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BenchEntity be)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!player.isShiftKeyDown() && be.hasOutput()) {
            for (ItemStack s : be.takeOutput()) {
                if (!player.getInventory().add(s)) player.drop(s, false);
            }
        } else {
            player.sendOverlayMessage(Component.literal(be.describe()));
        }
        return InteractionResult.SUCCESS;
    }
}
