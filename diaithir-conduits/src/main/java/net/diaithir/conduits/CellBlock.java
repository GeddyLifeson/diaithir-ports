package net.diaithir.conduits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** A fluid cell (kind 0) or energy cell (kind 1): a plain buffer that every face can fill and empty. Use it to read the contents. */
public final class CellBlock extends BaseEntityBlock {
    public static final MapCodec<CellBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(propertiesCodec(), Codec.INT.fieldOf("kind").forGetter(b -> b.kind)).apply(i, CellBlock::new));
    public final int kind;

    public CellBlock(Properties properties, int kind) {
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
        return new CellBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CellBlockEntity be) player.sendOverlayMessage(Component.literal(be.describe()));
        return InteractionResult.SUCCESS;
    }
}
