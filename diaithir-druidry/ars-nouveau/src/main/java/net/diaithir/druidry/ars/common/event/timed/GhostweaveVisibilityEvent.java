package net.diaithir.druidry.ars.common.event.timed;

import net.diaithir.druidry.ars.api.event.ITimedEvent;
import net.diaithir.druidry.ars.common.block.tile.GhostWeaveTile;

public class GhostweaveVisibilityEvent implements ITimedEvent {

    public int ticks;
    GhostWeaveTile ghostWeaveTile;
    boolean visible;

    public GhostweaveVisibilityEvent(GhostWeaveTile ghostWeaveTile, int ticks, boolean visible){
        this.ticks = ticks;
        this.ghostWeaveTile = ghostWeaveTile;
        this.visible = visible;
    }

    @Override
    public void tick(boolean serverSide) {
        ticks--;
        if(ticks <= 0){
            if(ghostWeaveTile != null && !ghostWeaveTile.isRemoved()){
                ghostWeaveTile.setVisibility(visible);
            }
        }
    }

    @Override
    public boolean isExpired() {
        return ticks <= 0;
    }
}
