package net.diaithir.worldevents;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/** One kind of world event. Other mods register more through {@link WorldEvents#register}. */
public interface WorldEvent {
    String id();

    String title();

    /** Relative chance when the scheduler rolls. */
    int weight();

    boolean possible(ServerLevel level);

    /** Runs the event for these players and returns a short result line for the log and the command. */
    String trigger(ServerLevel level, List<ServerPlayer> players);
}
