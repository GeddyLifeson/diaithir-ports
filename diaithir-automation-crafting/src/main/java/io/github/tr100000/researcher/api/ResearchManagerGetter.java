package io.github.tr100000.researcher.api;

import io.github.tr100000.researcher.ResearchManager;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface ResearchManagerGetter {
    default ResearchManager researcher$getServerManager() {
        throw new AssertionError();
    }
}
