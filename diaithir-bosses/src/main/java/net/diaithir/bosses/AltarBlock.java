package net.diaithir.bosses;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** The altar of a boss arena. Use it (bare-handed) to wake the boss, once the Age allows and the rest rule is met. */
public class AltarBlock extends BaseEntityBlock {
    public static final MapCodec<AltarBlock> CODEC = simpleCodec(AltarBlock::new);

    public AltarBlock(Properties props) { super(props); }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AltarBlockEntity(pos, state); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof AltarBlockEntity be) be.activate(player);
        return InteractionResult.SUCCESS_SERVER;
    }
}
