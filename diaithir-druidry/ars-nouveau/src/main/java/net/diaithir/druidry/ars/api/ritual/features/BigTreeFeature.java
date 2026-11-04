package net.diaithir.druidry.ars.api.ritual.features;

import net.diaithir.druidry.ars.api.ritual.FeaturePlacementRitual;
import net.diaithir.druidry.ars.common.block.tile.RitualBrazierTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BigTreeFeature implements IPlaceableFeature {

    BlockState tree;
    double distance;
    double chance;

    public BigTreeFeature(BlockState tree, double distance, double chance){
        this.tree = tree;
        this.distance = distance;
        this.chance = chance;
    }

    @Override
    public double distanceFromOthers() {
        return distance;
    }

    @Override
    public boolean onPlace(Level level, BlockPos pos, FeaturePlacementRitual placementRitual, RitualBrazierTile brazierTile) {
        if(level.getRandom().nextFloat() < chance && validPos(level, pos)
        && validPos(level, pos.north())
        && validPos(level, pos.north().east())
        && validPos(level, pos.east())){
            level.setBlock(pos, tree, 2);
            level.setBlock(pos.north(), tree, 2);
            level.setBlock(pos.north().east(), tree, 2);
            level.setBlock(pos.east(), tree, 2);
            if(level.getBlockState(pos).getBlock() instanceof SaplingBlock saplingBlock){
                saplingBlock.advanceTree((ServerLevel) level, pos, level.getBlockState(pos), level.getRandom());
            }
            // Try twice to grow the tree
            if(level.getBlockState(pos).getBlock() instanceof SaplingBlock saplingBlock){
                saplingBlock.advanceTree((ServerLevel) level, pos, level.getBlockState(pos), level.getRandom());
            }
            return true;
        }
        return false;
    }

    public boolean validPos(Level level, BlockPos pos){
        return level.getBlockState(pos).canBeReplaced() && tree.canSurvive(level, pos);
    }

    @Override
    public String getFeatureName() {
        return "random_tree";
    }
}
