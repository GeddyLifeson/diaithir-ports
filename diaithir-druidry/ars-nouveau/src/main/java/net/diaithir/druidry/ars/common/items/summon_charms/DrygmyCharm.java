package net.diaithir.druidry.ars.common.items.summon_charms;

import net.diaithir.druidry.ars.api.item.AbstractSummonCharm;
import net.diaithir.druidry.ars.common.block.tile.DrygmyTile;
import net.diaithir.druidry.ars.common.block.tile.SummoningTile;
import net.diaithir.druidry.ars.common.entity.EntityDrygmy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class DrygmyCharm extends AbstractSummonCharm {

    public DrygmyCharm() {
        super();
    }

    private static Block findDrygmyBlock() {
        for (Identifier id : BuiltInRegistries.BLOCK.keySet()) {
            String path = id.getPath();
            if (path.equals("drygmy_block") || path.equals("drygmy")) {
                return BuiltInRegistries.BLOCK.getValue(id);
            }
        }
        return Blocks.AIR;
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context, Level world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() == Blocks.MOSSY_COBBLESTONE) {
            Block drygmyBlock = findDrygmyBlock();
            if (drygmyBlock == Blocks.AIR) {
                return InteractionResult.PASS;
            }
            world.setBlockAndUpdate(pos, drygmyBlock.defaultBlockState());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOnSummonTile(UseOnContext context, Level world, SummoningTile tile, BlockPos pos) {
        if (tile instanceof DrygmyTile) {
            EntityDrygmy drygmy = new EntityDrygmy(world, true);
            drygmy.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
            world.addFreshEntity(drygmy);
            drygmy.homePos = new BlockPos(pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

}
