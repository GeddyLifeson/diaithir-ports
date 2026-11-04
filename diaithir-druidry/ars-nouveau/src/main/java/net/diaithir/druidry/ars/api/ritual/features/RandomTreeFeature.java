package net.diaithir.druidry.ars.api.ritual.features;

import net.diaithir.druidry.ars.api.ritual.FeaturePlacementRitual;
import net.diaithir.druidry.ars.common.block.tile.RitualBrazierTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class RandomTreeFeature implements IPlaceableFeature {

    List<BlockState> treeStates;
    double distance;
    double chance;
    public RandomTreeFeature(List<BlockState> treeStates, double distance, double chance){
        this.treeStates = treeStates;
        this.distance = distance;
        this.chance = chance;
    }

    @Override
    public double distanceFromOthers() {
        return distance;
    }

    @Override
    public boolean onPlace(Level level, BlockPos pos, FeaturePlacementRitual placementRitual, RitualBrazierTile brazierTile) {
        BlockState treeState = treeStates.get(level.getRandom().nextInt(treeStates.size()));
        if(level.getBlockState(pos).canBeReplaced() && treeState.canSurvive(level, pos)){
            level.setBlock(pos, treeState, 3);
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

    @Override
    public String getFeatureName() {
        return "random_tree";
    }
}
