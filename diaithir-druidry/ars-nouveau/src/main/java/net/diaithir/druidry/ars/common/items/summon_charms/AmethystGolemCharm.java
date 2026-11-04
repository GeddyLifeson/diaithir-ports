package net.diaithir.druidry.ars.common.items.summon_charms;

import net.diaithir.druidry.ars.api.item.AbstractSummonCharm;
import net.diaithir.druidry.ars.common.block.tile.SummoningTile;
import net.diaithir.druidry.ars.common.entity.AmethystGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class AmethystGolemCharm extends AbstractSummonCharm {

    public AmethystGolemCharm() {
        super();
        withTooltip(Component.translatable("tooltip.ars_nouveau.amethyst_charm"));
    }

    @Override
    @SuppressWarnings("unchecked")
    public InteractionResult useOnBlock(UseOnContext context, Level world, BlockPos pos) {
        EntityType<AmethystGolem> type = (EntityType<AmethystGolem>) (EntityType<?>) BuiltInRegistries.ENTITY_TYPE
                .getValue(Identifier.fromNamespaceAndPath("ars_nouveau", "amethyst_golem"));
        AmethystGolem amy = new AmethystGolem(type, world);
        amy.setPos(pos.getX(), pos.above().getY(), pos.getZ());
        world.addFreshEntity(amy);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOnSummonTile(UseOnContext context, Level world, SummoningTile tile, BlockPos pos) {
        return useOnBlock(context, world, pos);
    }
}
