package com.diaithir.addons.newage.content.heat.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class EncasedHeatPipeBlockEntity extends HeatPipeBlockEntity{

    public EncasedHeatPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public boolean canConnect(Direction from) {
        if (getLevel() == null)
            return false;
        return getLevel().getBlockState(getBlockPos()).getValue(EncasedHeatPipeBlock.getDirectionProperty(from.getOpposite()));
    }
}
