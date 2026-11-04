package net.diaithir.druidry.ars.common.event.timed;

import net.diaithir.druidry.ars.api.event.ITimedEvent;
import net.diaithir.druidry.ars.common.block.tile.SkyBlockTile;

public class SkyweaveVisibilityEvent implements ITimedEvent {
    public int ticks;
    SkyBlockTile skyweave;
    boolean visible;

    public SkyweaveVisibilityEvent(SkyBlockTile skyweave, int ticks, boolean visible){
        this.ticks = ticks;
        this.skyweave = skyweave;
        this.visible = visible;
    }

    @Override
    public void tick(boolean serverSide) {
        ticks--;
        if(ticks <= 0 && skyweave != null && !skyweave.isRemoved()){
            skyweave.setShowFacade(visible);
        }
    }

    @Override
    public boolean isExpired() {
        return ticks <= 0;
    }
}
