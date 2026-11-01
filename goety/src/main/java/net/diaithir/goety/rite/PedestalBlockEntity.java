package net.diaithir.goety.rite;

import net.diaithir.goety.GoetyBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** A pedestal: carries one offering for the rite. */
public class PedestalBlockEntity extends HolderBlockEntity {
    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(GoetyBlockEntities.PEDESTAL.get(), pos, state);
    }
}
