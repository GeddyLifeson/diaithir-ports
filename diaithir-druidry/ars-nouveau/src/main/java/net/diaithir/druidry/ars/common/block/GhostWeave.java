package net.diaithir.druidry.ars.common.block;

import net.diaithir.druidry.ars.common.block.tile.GhostWeaveTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class GhostWeave extends MirrorWeave {


    public GhostWeave(Properties properties) {
        super(properties);
    }

    public GhostWeave(){
        super();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new GhostWeaveTile(pPos, pState);
    }

}
