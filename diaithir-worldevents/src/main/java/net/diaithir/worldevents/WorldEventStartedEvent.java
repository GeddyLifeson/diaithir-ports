package net.diaithir.worldevents;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

/** Posted on the NeoForge bus after an event starts, so quests and scripts can react. */
public class WorldEventStartedEvent extends Event {
    private final WorldEvent event;
    private final ServerLevel level;

    public WorldEventStartedEvent(WorldEvent event, ServerLevel level) {
        this.event = event;
        this.level = level;
    }

    public WorldEvent event() {
        return event;
    }

    public ServerLevel level() {
        return level;
    }
}
