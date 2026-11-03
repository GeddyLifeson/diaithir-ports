package net.diaithir.upgrades;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** The beacon block. Slot modules into it like any machine; it shares speed, efficiency and quality. */
public final class BeaconBlock extends BaseEntityBlock {
    public static final MapCodec<BeaconBlock> CODEC = simpleCodec(BeaconBlock::new);

    public BeaconBlock(Properties properties) { super(properties); }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new BeaconBlockEntity(pos, state); }
}
