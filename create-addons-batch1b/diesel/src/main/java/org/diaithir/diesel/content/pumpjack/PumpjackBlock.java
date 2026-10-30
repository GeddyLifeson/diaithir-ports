package org.diaithir.diesel.content.pumpjack;

import com.zurrtum.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.diaithir.diesel.registries.CDGBlockEntityTypes;

/** A pump that draws crude oil from the pool under its chunk. Turn it and pipe the oil away. */
public class PumpjackBlock extends HorizontalAxisKineticBlock implements IBE<PumpjackBlockEntity> {
    public PumpjackBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<PumpjackBlockEntity> getBlockEntityClass() {
        return PumpjackBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PumpjackBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.PUMPJACK.get();
    }
}
