package io.github.tr100000.researcher.api;

import io.github.tr100000.researcher.ClientResearchTracker;
import io.github.tr100000.researcher.Research;
import io.github.tr100000.researcher.api.event.Event;
import io.github.tr100000.researcher.api.event.EventFactory;

public final class ResearcherClientEvents {
    private ResearcherClientEvents() {}

    public static final Event<ResearchFinished> RESEARCH_FINISHED = EventFactory.createArrayBacked(ResearchFinished.class,
            listeners -> (researchTracker, research) -> {
                    for (ResearchFinished listener : listeners) {
                        listener.onResearchFinished(researchTracker, research);
                    }
            });

    @FunctionalInterface
    public interface ResearchFinished {
        void onResearchFinished(ClientResearchTracker researchTracker, Research research);
    }
}
