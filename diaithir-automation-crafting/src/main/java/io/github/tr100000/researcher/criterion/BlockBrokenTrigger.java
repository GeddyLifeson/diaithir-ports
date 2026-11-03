package io.github.tr100000.researcher.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tr100000.researcher.ResearcherCriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class BlockBrokenTrigger extends SimpleCriterionTrigger<BlockBrokenTrigger.Conditions> {
    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player, BlockState brokenBlock) {
        this.trigger(player, conditions -> conditions.matches(brokenBlock));
    }

    public record Conditions(Optional<ContextAwarePredicate> player, Optional<Holder<Block>> block, Optional<StatePropertiesPredicate> state) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                        BuiltInRegistries.BLOCK.holderByNameCodec().optionalFieldOf("block").forGetter(Conditions::block),
                        StatePropertiesPredicate.CODEC.optionalFieldOf("state").forGetter(Conditions::state)
                ).apply(instance, Conditions::new)
        );

        @SuppressWarnings("deprecation")
        public static Criterion<Conditions> of(Block block) {
            return ResearcherCriteriaTriggers.BLOCK_BROKEN.createCriterion(new Conditions(Optional.empty(), Optional.of(block.builtInRegistryHolder()), Optional.empty()));
        }

        public static Criterion<Conditions> of(StatePropertiesPredicate.Builder state) {
            return ResearcherCriteriaTriggers.BLOCK_BROKEN.createCriterion(new Conditions(Optional.empty(), Optional.empty(), state.build()));
        }

        @SuppressWarnings("deprecation")
        public static Criterion<Conditions> of(Block block, StatePropertiesPredicate.Builder state) {
            return ResearcherCriteriaTriggers.BLOCK_BROKEN.createCriterion(new Conditions(Optional.empty(), Optional.of(block.builtInRegistryHolder()), state.build()));
        }

        public boolean matches(BlockState state) {
            return (this.block.isEmpty() || state.is(this.block.get())) && (this.state.isEmpty() || this.state.get().matches(state));
        }
    }
}
