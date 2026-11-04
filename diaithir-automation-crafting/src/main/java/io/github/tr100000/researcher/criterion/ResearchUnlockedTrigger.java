package io.github.tr100000.researcher.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearcherCriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class ResearchUnlockedTrigger extends SimpleCriterionTrigger<ResearchUnlockedTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Identifier id) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(id));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Identifier researchId) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        Identifier.CODEC.fieldOf("research").forGetter(TriggerInstance::researchId)
                ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> of(Identifier researchId) {
            return ResearcherCriteriaTriggers.HAS_RESEARCH.createCriterion(new TriggerInstance(Optional.empty(), researchId));
        }

        public static Criterion<TriggerInstance> of(String researchId) {
            return of(ModUtils.id(researchId));
        }

        public boolean matches(Identifier id) {
            return this.researchId.equals(id);
        }
    }
}
