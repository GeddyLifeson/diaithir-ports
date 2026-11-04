package net.diaithir.druidry.ars.common.items.summon_charms;

import net.diaithir.druidry.ars.api.item.AbstractSummonCharm;
import net.diaithir.druidry.ars.common.block.tile.SummoningTile;
import net.diaithir.druidry.ars.common.entity.Starbuncle;
import net.diaithir.druidry.ars.common.items.data.StarbuncleCharmData;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class StarbuncleCharm extends AbstractSummonCharm {
    public StarbuncleCharm() {
        super(defaultProps().component(DataComponentRegistry.STARBUNCLE_DATA, new StarbuncleCharmData()));
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context, Level world, BlockPos pos) {
        Starbuncle carbuncle = new Starbuncle(world, true);
        StarbuncleCharmData data = context.getItemInHand().getOrDefault(DataComponentRegistry.STARBUNCLE_DATA, new StarbuncleCharmData());
        pos = pos.relative(context.getClickedFace());
        carbuncle.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        carbuncle.data = data.mutable();
        world.addFreshEntity(carbuncle);
        carbuncle.restoreFromTag();
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOnSummonTile(UseOnContext context, Level world, SummoningTile tile, BlockPos pos) {
        return useOnBlock(context, world, pos);
    }
}
