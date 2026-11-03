package net.diaithir.druidry.ars.api.util;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ANEventBus {
    /** Posts the event on the NeoForge bus and returns true if it was cancelled. */
    public static boolean post(Event event) {
        NeoForge.EVENT_BUS.post(event);
        return event instanceof ICancellableEvent cancellable && cancellable.isCanceled();
    }
}
