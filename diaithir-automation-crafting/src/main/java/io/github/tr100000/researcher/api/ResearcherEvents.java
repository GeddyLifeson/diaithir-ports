package io.github.tr100000.researcher.api;

import io.github.tr100000.researcher.PlayerResearchTracker;
import io.github.tr100000.researcher.Research;
import io.github.tr100000.researcher.api.event.Event;
import io.github.tr100000.researcher.api.event.EventFactory;

public final class ResearcherEvents {
    private ResearcherEvents() {}

    public static final Event<ResearchFinished> RESEARCH_FINISHED = EventFactory.createArrayBacked(ResearchFinished.class,
            listeners -> (researchTracker, research) -> {
                    for (ResearchFinished listener : listeners) {
                        listener.onResearchFinished(researchTracker, research);
                    }
            });

    @FunctionalInterface
    public interface ResearchFinished {
        void onResearchFinished(PlayerResearchTracker researchTracker, Research research);
    }
}
