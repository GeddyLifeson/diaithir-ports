package net.diaithir.vehicles;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** A steel fuel tank: 8,000 L, filled and emptied by jerry cans, pumps and any fluid pipe. */
public class FuelTankBlock extends BaseEntityBlock {
    public static final MapCodec<FuelTankBlock> CODEC = simpleCodec(FuelTankBlock::new);

    public FuelTankBlock(Properties p) {
        super(p);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FuelTankEntity(pos, state);
    }
}
