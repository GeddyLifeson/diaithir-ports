package io.github.tr100000.researcher.api.trigger;

import io.github.tr100000.researcher.ResearchCriterion;
import net.minecraft.advancements.CriterionTriggerInstance;
import org.jetbrains.annotations.Contract;

public interface TriggerHandler<T extends CriterionTriggerInstance> {
    @Contract(value = "_ -> new", pure = true)
    TriggerDisplayElement prepare(ResearchCriterion<T> criterion);
}
