package io.github.tr100000.researcher.api.trigger;

import io.github.tr100000.researcher.Researcher;
import io.github.tr100000.researcher.impl.criterion.ErrorTriggerHandler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class TriggerHandlerRegistry {
    private TriggerHandlerRegistry() {}

    private static final Supplier<TriggerHandler<?>> FALLBACK_HANDLER = () -> ErrorTriggerHandler.NULL;
    private static final Map<CriterionTrigger<?>, Supplier<TriggerHandler<?>>> REGISTRY = new Object2ObjectOpenHashMap<>();

    @SuppressWarnings("unchecked")
    @Contract(pure = true)
    public static <T extends CriterionTriggerInstance> TriggerHandler<T> get(CriterionTrigger<?> trigger) {
        return (TriggerHandler<T>)REGISTRY.getOrDefault(trigger, FALLBACK_HANDLER).get();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends CriterionTriggerInstance> void register(CriterionTrigger<T> trigger, Supplier<TriggerHandler<T>> handler) {
        Objects.requireNonNull(trigger, "trigger is null");
        Objects.requireNonNull(handler, "handler is null");
        REGISTRY.put(trigger, (Supplier)handler);
    }

    @ApiStatus.Internal
    public static void printNonRegistered() {
        BuiltInRegistries.TRIGGER_TYPES.forEach(criterion -> {
            if (!REGISTRY.containsKey(criterion)) {
                Researcher.LOGGER.warn("Trigger {} does not have a handler", BuiltInRegistries.TRIGGER_TYPES.getKey(criterion));
            }
        });
    }
}
