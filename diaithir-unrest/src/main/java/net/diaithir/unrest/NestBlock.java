package net.diaithir.unrest;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** The nest block. Nests are placed by worldgen/creature code far from claimed ground; here it only has to exist, absorb and report. */
public final class NestBlock extends BaseEntityBlock {
    public static final MapCodec<NestBlock> CODEC = simpleCodec(NestBlock::new);

    public NestBlock(Properties p) {
        super(p);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NestBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof NestBlockEntity n) {
            player.sendOverlayMessage(Component.translatable("diaithirunrest.nest", n.tier(), Math.round(n.store()), Math.round(n.lifetime())));
        }
        return InteractionResult.SUCCESS;
    }
}
