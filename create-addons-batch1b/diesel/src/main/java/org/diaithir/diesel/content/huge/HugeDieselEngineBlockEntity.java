package org.diaithir.diesel.content.huge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.diaithir.diesel.Fuels;
import org.diaithir.diesel.content.DieselEngineBlockEntity;
import org.diaithir.diesel.registries.CDGBlockEntityTypes;

/** The big engine: runs on the huge row of the fuel table with a four-bucket tank. */
public class HugeDieselEngineBlockEntity extends DieselEngineBlockEntity {
    public HugeDieselEngineBlockEntity(BlockPos pos, BlockState state) {
        super(CDGBlockEntityTypes.HUGE_DIESEL_ENGINE.get(), pos, state);
    }

    @Override
    protected Fuels.Per select(Fuels.Fuel fuel) {
        return fuel.huge();
    }

    @Override
    protected int tankCapacity() {
        return 4 * CAPACITY;
    }
}
