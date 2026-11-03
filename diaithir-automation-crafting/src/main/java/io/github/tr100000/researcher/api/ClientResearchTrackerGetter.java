package io.github.tr100000.researcher.api;

import io.github.tr100000.researcher.ClientResearchTracker;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface ClientResearchTrackerGetter {
    default ClientResearchTracker researcher$getClientTracker() {
        throw new AssertionError();
    }
}
