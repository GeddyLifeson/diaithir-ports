package io.github.tr100000.researcher.api;

import io.github.tr100000.researcher.PlayerResearchTracker;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface ServerResearchTrackerGetter {
    default PlayerResearchTracker researcher$getPlayerTracker(ServerPlayer player) {
        throw new AssertionError();
    }
}
