package com.diaithir.addons.newage.content.electricity.generation.brushes;

import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.diaithir.addons.newage.CNABlockEntityTypes;
import org.jetbrains.annotations.Nullable;

public class CarbonBrushesBlock extends DirectionalKineticBlock implements IBE<CarbonBrushesBlockEntity> {
    public CarbonBrushesBlock(Properties properties) {
        super(properties.strength(2.5F, 1.0F));
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(FACING).getAxis();
    }

    @Override
    public Class<CarbonBrushesBlockEntity> getBlockEntityClass() {
        return CarbonBrushesBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CarbonBrushesBlockEntity> getBlockEntityType() {
        return CNABlockEntityTypes.CARBON_BRUSHES.get();
    }
}
