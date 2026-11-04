package org.diaithir.mega.site;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Site depot: takes pallets and bulk containers from any item pipe, hopper or storage-network export cable. No GUI, no slots. */
public class DepotBlock extends BaseEntityBlock {
    public static final MapCodec<DepotBlock> CODEC = simpleCodec(DepotBlock::new);

    public DepotBlock(Properties props) { super(props); }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new DepotBlockEntity(pos, state); }
}
