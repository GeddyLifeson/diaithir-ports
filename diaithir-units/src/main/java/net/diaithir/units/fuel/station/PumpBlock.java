package net.diaithir.units.fuel.station;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * Hand pump (Age III, 5 L per use, no power), pump station (Age VI, 1 L/s, automatic) or loading rack (Age VII, 20 L/s for tanker trucks, rail tank cars and ships).
 * {@code autoLitresPerSecond} is 0 for the hand pump.
 */
public final class PumpBlock extends BaseEntityBlock {
    public static final MapCodec<PumpBlock> CODEC = simpleCodec(p -> new PumpBlock(p, 1, 5));
    public final double autoLitresPerSecond;
    public final double manualLitres;

    public PumpBlock(Properties properties, double autoLitresPerSecond, double manualLitres) {
        super(properties);
        this.autoLitresPerSecond = autoLitresPerSecond;
        this.manualLitres = manualLitres;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PumpBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || autoLitresPerSecond <= 0 || type != Station.PUMP_BE.get()) return null;
        return (lvl, pos, st, be) -> {
            if (lvl.getGameTime() % 20 == 0) ((PumpBlockEntity) be).pump(lvl, autoLitresPerSecond);
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (manualLitres <= 0) return InteractionResult.PASS;
        if (level instanceof ServerLevel && level.getBlockEntity(pos) instanceof PumpBlockEntity be) be.pump(level, manualLitres);
        return InteractionResult.SUCCESS;
    }
}
