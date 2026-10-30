package org.diaithir.diesel.content.distillation;

import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.diaithir.diesel.registries.CDGBlockEntityTypes;

/** One segment of a distillation tower. Stack them over a burner or blaze burner. */
public class DistillationTowerBlock extends Block implements IBE<DistillationTowerBlockEntity>, IWrenchable {
    public DistillationTowerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<DistillationTowerBlockEntity> getBlockEntityClass() {
        return DistillationTowerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DistillationTowerBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.DISTILLATION_TOWER.get();
    }
}
