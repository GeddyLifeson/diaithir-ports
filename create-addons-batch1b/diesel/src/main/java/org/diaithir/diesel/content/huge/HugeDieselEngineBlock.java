package org.diaithir.diesel.content.huge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.diaithir.diesel.content.DieselEngineBlock;
import org.diaithir.diesel.content.DieselEngineBlockEntity;
import org.diaithir.diesel.registries.CDGBlockEntityTypes;

public class HugeDieselEngineBlock extends DieselEngineBlock {
    public HugeDieselEngineBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public Class<DieselEngineBlockEntity> getBlockEntityClass() {
        return DieselEngineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DieselEngineBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.HUGE_DIESEL_ENGINE.get();
    }
}
