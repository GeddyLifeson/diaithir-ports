package net.diaithir.druidry.ars.common.items.summon_charms;

import net.diaithir.druidry.ars.api.item.AbstractSummonCharm;
import net.diaithir.druidry.ars.common.block.tile.SummoningTile;
import net.diaithir.druidry.ars.common.block.tile.WixieCauldronTile;
import net.diaithir.druidry.ars.common.entity.EntityWixie;
import net.diaithir.druidry.ars.common.util.PortUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CauldronBlock;

public class WixieCharm extends AbstractSummonCharm {
    public WixieCharm() {
        super();
    }

    private static Block findWixieCauldron() {
        for (Identifier id : BuiltInRegistries.BLOCK.keySet()) {
            if (id.getPath().equals("wixie_cauldron")) {
                return BuiltInRegistries.BLOCK.getValue(id);
            }
        }
        return null;
    }

    /**
     * Called when this item is used when targetting a Block
     */
    public InteractionResult useOn(UseOnContext context) {
        return super.useOn(context);
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context, Level world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() instanceof CauldronBlock) {
            Block cauldron = findWixieCauldron();
            if (cauldron == null) {
                return InteractionResult.PASS;
            }
            world.setBlockAndUpdate(pos, cauldron.defaultBlockState());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOnSummonTile(UseOnContext context, Level world, SummoningTile tile, BlockPos pos) {
        if (tile instanceof WixieCauldronTile cauldronTile) {
            if (cauldronTile.hasWixie()) {
                PortUtil.sendMessage(context.getPlayer(), Component.translatable("ars_nouveau.wixie.has_wixie"));
            } else {
                EntityWixie wixie = new EntityWixie(world, pos);
                wixie.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
                world.addFreshEntity(wixie);
                cauldronTile.entityID = wixie.getId();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

}
