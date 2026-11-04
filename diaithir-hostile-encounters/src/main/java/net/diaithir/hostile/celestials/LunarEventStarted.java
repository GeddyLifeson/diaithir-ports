package net.diaithir.hostile.celestials;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

/** Posted on the NeoForge bus when a lunar event rises ({@code started} true) or sets (false); the quest layer completes world_event quests from it. */
public final class LunarEventStarted extends Event {
    private final String eventId;
    private final ServerLevel level;
    private final boolean started;

    public LunarEventStarted(String eventId, ServerLevel level, boolean started) {
        this.eventId = eventId;
        this.level = level;
        this.started = started;
    }

    public String eventId() { return eventId; }
    public ServerLevel level() { return level; }
    public boolean started() { return started; }
}
