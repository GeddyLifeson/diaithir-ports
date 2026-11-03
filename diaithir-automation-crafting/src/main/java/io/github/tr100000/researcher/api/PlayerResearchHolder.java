package io.github.tr100000.researcher.api;

import io.github.tr100000.researcher.Research;
import io.github.tr100000.researcher.ResearchProgress;
import io.github.tr100000.researcher.ResearchStatus;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface PlayerResearchHolder {
    @Contract(pure = true)
    @Nullable Identifier getCurrentResearchingId();

    @Contract(pure = true)
    @Nullable Research getCurrentResearching();

    @Contract(pure = true)
    List<Identifier> getPinnedResearches();

    @Contract(value = "null -> false", pure = true)
    boolean canResearch(@Nullable Research research);

    @Contract(value = "null -> true", pure = true)
    boolean canCraftRecipe(@Nullable Identifier recipeId);

    @Contract(value = "null -> false", pure = true)
    boolean hasFinished(@Nullable Research research);

    ResearchProgress getProgress(Research research);

    default ResearchStatus getStatus(Research research) {
        ResearchProgress progress = getProgress(research);
        if (progress.isFinished()) {
            return ResearchStatus.FINISHED;
        }
        else if (canResearch(research)) {
            if (progress.getCount() > 0) {
                return ResearchStatus.IN_PROGRESS;
            }
            else {
                return ResearchStatus.AVAILABLE;
            }
        }
        else {
            return ResearchStatus.LOCKED;
        }
    }
}
