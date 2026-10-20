package untamedwilds.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.criterion.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import untamedwilds.UntamedWilds;

public class ModAdvancementTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, UntamedWilds.MOD_ID);

    public static final DeferredHolder<CriterionTrigger<?>, UntamedTriggers> NO_PATCHOULI_LOADED = TRIGGERS.register("guidebook_alt", () -> new UntamedTriggers(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "guidebook_alt")));
    public static final DeferredHolder<CriterionTrigger<?>, UntamedTriggers> BAIT_BASIC = TRIGGERS.register("used_bait", () -> new UntamedTriggers(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "used_bait")));
    public static final DeferredHolder<CriterionTrigger<?>, UntamedTriggers> MASTER_BAIT = TRIGGERS.register("master_bait", () -> new UntamedTriggers(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "master_bait")));
    public static final DeferredHolder<CriterionTrigger<?>, UntamedTriggers> DISCOVERED_SPITTER = TRIGGERS.register("discovered_spitter", () -> new UntamedTriggers(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "discovered_spitter")));

    public static class UntamedTriggers extends SimpleCriterionTrigger<UntamedTriggers.Instance> {
        private final Identifier id;

        public UntamedTriggers(Identifier resourceLocation) {
            this.id = resourceLocation;
        }

        public void trigger(ServerPlayer entityIn) {
            this.trigger(entityIn, (input) -> true);
        }

        public Identifier getId() {
            return id;
        }

        @Override
        public Codec<Instance> codec() {
            return Instance.CODEC;
        }

        public static record Instance(java.util.Optional<ContextAwarePredicate> player) implements SimpleInstance {
            public static final Codec<Instance> CODEC = RecordCodecBuilder.create((instance) -> instance.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player)).apply(instance, Instance::new));

            public static SimpleCriterionTrigger.SimpleInstance instance() {
                return new Instance(java.util.Optional.empty());
            }
        }
    }
}