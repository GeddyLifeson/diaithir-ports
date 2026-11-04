package io.github.tr100000.researcher;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Contract;

public record ResearchCriterion<T extends CriterionTriggerInstance>(Criterion<T> criterion, int count) {
    public static final Codec<ResearchCriterion<?>> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Criterion.CODEC.fieldOf("criterion").forGetter(ResearchCriterion::criterion),
                    ExtraCodecs.POSITIVE_INT.fieldOf("count").forGetter(ResearchCriterion::count)
            ).apply(instance, ResearchCriterion::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchCriterion<?>> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Criterion.CODEC), ResearchCriterion::criterion,
            ByteBufCodecs.VAR_INT, ResearchCriterion::count,
            ResearchCriterion::new
    );

    public static final ResearchCriterion<ImpossibleTrigger.TriggerInstance> IMPOSSIBLE = new ResearchCriterion<>(CriteriaTriggers.IMPOSSIBLE, new ImpossibleTrigger.TriggerInstance(), Integer.MAX_VALUE);

    public ResearchCriterion(CriterionTrigger<T> trigger, T conditions, int count) {
        this(new Criterion<>(trigger, conditions), count);
    }

    @Contract(pure = true)
    public T conditions() {
        return criterion().triggerInstance();
    }

    @Contract(pure = true)
    public CriterionTrigger<T> trigger() {
        return criterion().trigger();
    }
}
