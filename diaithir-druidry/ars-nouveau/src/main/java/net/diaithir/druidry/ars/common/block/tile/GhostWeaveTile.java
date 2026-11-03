package net.diaithir.druidry.ars.common.block.tile;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.diaithir.druidry.ars.api.event.EventQueue;
import net.diaithir.druidry.ars.common.event.timed.GhostweaveVisibilityEvent;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class GhostWeaveTile extends MirrorWeaveTile{

    private boolean invisible;

    public GhostWeaveTile(BlockPos pos, BlockState state) {
        super(BlockRegistry.GHOST_WEAVE_TILE.get(), pos, state);
    }


    public void setVisibility(boolean invisible){
        if(this.invisible == invisible){
            return;
        }

        int ticks = 1;
        for(Direction d : Direction.values()){
            BlockPos offset = getBlockPos().relative(d);
            if(level.getBlockEntity(offset) instanceof GhostWeaveTile neighbor){
                if(this.isInvisible() == neighbor.isInvisible()) {
                    ticks++;
                    EventQueue.getServerInstance().addEvent(new GhostweaveVisibilityEvent(neighbor, ticks, invisible));
                }
            }
        }
        this.invisible = invisible;
        this.updateBlock();
    }

    public boolean isInvisible(){
        return invisible;
    }

    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putBoolean("invisible", invisible);
    }

    @Override
    protected void loadAdditional(ValueInput compound) {
        super.loadAdditional(compound);
        invisible = compound.getBooleanOr("invisible", false);
    }

    @Override
    public BlockState getDefaultBlockState() {
        return BlockRegistry.GHOST_WEAVE.defaultBlockState();
    }
}
