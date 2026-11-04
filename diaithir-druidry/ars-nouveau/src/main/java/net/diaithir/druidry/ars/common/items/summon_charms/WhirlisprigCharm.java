package net.diaithir.druidry.ars.common.items.summon_charms;


import net.diaithir.druidry.ars.api.item.AbstractSummonCharm;
import net.diaithir.druidry.ars.common.block.tile.SummoningTile;
import net.diaithir.druidry.ars.common.block.tile.WhirlisprigTile;
import net.diaithir.druidry.ars.common.entity.Whirlisprig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class WhirlisprigCharm extends AbstractSummonCharm {

    public WhirlisprigCharm() {
        super();
    }

    private static Block findFlowerBlock() {
        for (Identifier id : BuiltInRegistries.BLOCK.keySet()) {
            if (id.getPath().equals("whirlisprig_flower")) {
                return BuiltInRegistries.BLOCK.getValue(id);
            }
        }
        return null;
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context, Level world, BlockPos pos) {
        if (world.getBlockState(pos).is(BlockTags.FLOWERS)) {
            Block flower = findFlowerBlock();
            if (flower == null) {
                return InteractionResult.PASS;
            }
            world.setBlockAndUpdate(pos, flower.defaultBlockState());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOnSummonTile(UseOnContext context, Level world, SummoningTile tile, BlockPos pos) {
        if (tile instanceof WhirlisprigTile) {
            Whirlisprig whirlisprig = new Whirlisprig(world, true, pos);
            whirlisprig.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
            world.addFreshEntity(whirlisprig);
            whirlisprig.flowerPos = new BlockPos(pos);
            return InteractionResult.SUCCESS;
        } else return InteractionResult.PASS;
    }

}
