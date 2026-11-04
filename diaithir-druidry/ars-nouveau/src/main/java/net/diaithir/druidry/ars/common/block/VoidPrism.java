package net.diaithir.druidry.ars.common.block;

import net.diaithir.druidry.ars.api.block.IPrismaticBlock;
import net.diaithir.druidry.ars.api.util.BlockUtil;
import net.diaithir.druidry.ars.common.entity.EntityProjectileSpell;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class VoidPrism extends ModBlock implements IPrismaticBlock {

    public VoidPrism() {
        super(defaultProperties());
    }

    @Override
    public void onHit(ServerLevel world, BlockPos pos, EntityProjectileSpell spell) {
        spell.remove(Entity.RemovalReason.DISCARDED);
        BlockUtil.updateObservers(world, pos);
    }
}
