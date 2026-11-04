package io.github.tr100000.researcher.api;

import io.github.tr100000.researcher.PlayerResearchTracker;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface PlayerResearchTrackerGetter {
    default PlayerResearchTracker researcher$getPlayerTracker() {
        throw new AssertionError();
    }
}
