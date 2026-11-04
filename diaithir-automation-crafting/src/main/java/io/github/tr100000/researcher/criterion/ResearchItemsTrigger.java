package io.github.tr100000.researcher.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tr100000.researcher.ResearcherCriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Optional;

public class ResearchItemsTrigger extends SimpleCriterionTrigger<ResearchItemsTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public record TriggerInstance(int time, List<Item> items) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        ExtraCodecs.POSITIVE_INT.fieldOf("time").forGetter(TriggerInstance::time),
                        BuiltInRegistries.ITEM.byNameCodec().listOf(1, Integer.MAX_VALUE).fieldOf("items").forGetter(TriggerInstance::items)
                ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> of(int time, Item... items) {
            return ResearcherCriteriaTriggers.RESEARCH_ITEMS.createCriterion(new TriggerInstance(time, List.of(items)));
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return Optional.empty();
        }
    }
}
