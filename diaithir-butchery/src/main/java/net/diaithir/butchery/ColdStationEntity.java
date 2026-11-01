package net.diaithir.butchery;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** The cold room and the stasis case: containers in their own right. Spoilage treats a stack held here as kept at {@link #containerShelf()} percent of normal shelf life. */
public final class ColdStationEntity extends StationEntity {
    private final int shelf;

    ColdStationEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState state, String station, int shelf) {
        super(type, pos, state, station);
        this.shelf = shelf;
    }

    @Override
    public int containerShelf() {
        return shelf;
    }
}
