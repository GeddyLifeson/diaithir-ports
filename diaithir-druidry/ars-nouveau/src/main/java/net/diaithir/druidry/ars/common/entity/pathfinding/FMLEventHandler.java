package net.diaithir.druidry.ars.common.entity.pathfinding;

import net.diaithir.druidry.ars.api.event.EventQueue;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

/**
 * Event handler used to catch various forge events.
 */
public class FMLEventHandler {

    public static void onServerStopped(final ServerStoppingEvent event) {
        Pathfinding.shutdown();
        EventQueue.getServerInstance().clear();
        EventQueue.getClientQueue().clear();
    }
}
