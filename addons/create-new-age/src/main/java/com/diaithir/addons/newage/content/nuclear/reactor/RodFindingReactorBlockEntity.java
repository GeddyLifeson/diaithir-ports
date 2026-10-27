package com.diaithir.addons.newage.content.nuclear.reactor;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.diaithir.addons.newage.config.CNAConfig;
import com.diaithir.addons.newage.content.nuclear.reactor.rod.ReactorRodBlockEntity;

import java.util.List;

public abstract class RodFindingReactorBlockEntity extends SmartBlockEntity {
    public RodFindingReactorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {}

    public void findRods(List<ReactorRodBlockEntity> list, Direction dir) {
        if (level == null)
            return;

        BlockEntity entity = level.getBlockEntity(getBlockPos().relative(dir));

        int c = 0;
        while (entity instanceof ReactorRodBlockEntity rrbe) {
            c++;
            if (c > CNAConfig.getServer().maxRodsInDirection.get()) {
                return;
            }
            list.add(rrbe);
            entity = level.getBlockEntity(rrbe.getBlockPos().relative(dir));
        }
    }

}
