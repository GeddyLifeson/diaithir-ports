package net.diaithir.druidry.ars.common.spell.rewind;

import net.diaithir.druidry.ars.common.event.timed.RewindEvent;

public interface IRewindCallback {
    /**
     * Called from RewindEvent to rewind the target at the given time
     */
    void onRewind(RewindEvent event);
}
