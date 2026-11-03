package io.github.tr100000.researcher.api.trigger.util;

import com.mojang.serialization.JsonOps;
import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.Researcher;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import io.github.tr100000.trutils.api.item.ItemUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.AnyValue;
import net.minecraft.core.component.predicates.AttributeModifiersPredicate;
import net.minecraft.core.component.predicates.BundlePredicate;
import net.minecraft.core.component.predicates.ContainerPredicate;
import net.minecraft.core.component.predicates.CustomDataPredicate;
import net.minecraft.core.component.predicates.DamagePredicate;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.component.predicates.FireworkExplosionPredicate;
import net.minecraft.core.component.predicates.FireworksPredicate;
import net.minecraft.core.component.predicates.JukeboxPlayablePredicate;
import net.minecraft.core.component.predicates.PotionsPredicate;
import net.minecraft.core.component.predicates.TrimPredicate;
import net.minecraft.core.component.predicates.VillagerTypePredicate;
import net.minecraft.core.component.predicates.WritableBookPredicate;
import net.minecraft.core.component.predicates.WrittenBookPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public final class ComponentsPredicateHelper {
    private ComponentsPredicateHelper() {}

    private static final Map<DataComponentPredicate.Type<?>, BiConsumer<DataComponentPredicate, IndentedTextHolder>> PREDICATE_HANDLER_REGISTRY = new Object2ObjectOpenHashMap<>();

    private static final Component COMPONENTS_HEADER = ModUtils.getScreenTranslated("predicate.components");
    private static final String EXACT_VALUE_KEY = ModUtils.getScreenTranslationKey("predicate.components.exact");
    private static final String ANY_VALUE_KEY = ModUtils.getScreenTranslationKey("predicate.components.any");

    private static final Component DAMAGE_HEADER = ModUtils.getScreenTranslated("predicate.components.damage");
    private static final Component DAMAGE_DURABILITY = ModUtils.getScreenTranslated("predicate.components.damage.durability");
    private static final Component DAMAGE_DAMAGE = ModUtils.getScreenTranslated("predicate.components.damage.damage");

    private static final Component ENCHANTMENTS_HEADER = ModUtils.getScreenTranslated("predicate.components.enchantments");
    private static final Component ENCHANTMENTS_LIST = ModUtils.getScreenTranslated("predicate.components.enchantments.list");
    private static final Component ENCHANTMENTS_LEVELS = ModUtils.getScreenTranslated("predicate.components.enchantments.levels");

    private static final Component POTION_CONTENTS_HEADER = ModUtils.getScreenTranslated("predicate.components.potion_contents");

    private static final Component CONTAINER_HEADER = ModUtils.getScreenTranslated("predicate.components.container");
    private static final Component CONTAINER_ITEM = ModUtils.getScreenTranslated("predicate.item");

    private static final Component BUNDLE_CONTENTS_HEADER = ModUtils.getScreenTranslated("predicate.components.bundle_contents");
    private static final Component BUNDLE_CONTENTS_ITEM = ModUtils.getScreenTranslated("predicate.item");

    private static final Component FIREWORK_EXPLOSION_HEADER = ModUtils.getScreenTranslated("predicate.components.firework_explosion");
    private static final String FIREWORK_EXPLOSION_SHAPE_KEY = ModUtils.getScreenTranslationKey("predicate.components.firework_explosion.shape");
    private static final Component FIREWORK_EXPLOSION_TWINKLE = ModUtils.getScreenTranslated("predicate.components.firework_explosion.twinkle");
    private static final Component FIREWORK_EXPLOSION_NO_TWINKLE = ModUtils.getScreenTranslated("predicate.components.firework_explosion.no_twinkle");
    private static final Component FIREWORK_EXPLOSION_TRAIL = ModUtils.getScreenTranslated("predicate.components.firework_explosion.trail");
    private static final Component FIREWORK_EXPLOSION_NO_TRAIL = ModUtils.getScreenTranslated("predicate.components.firework_explosion.no_trail");

    private static final Component FIREWORKS_HEADER = ModUtils.getScreenTranslated("predicate.components.fireworks");
    private static final Component FIREWORKS_FLIGHT_DURATION = ModUtils.getScreenTranslated("predicate.components.fireworks.flight_duration");

    private static final Component WRITABLE_BOOK_CONTENT_HEADER = ModUtils.getScreenTranslated("predicate.components.writable_book_content");

    private static final Component WRITTEN_BOOK_CONTENT_HEADER = ModUtils.getScreenTranslated("predicate.components.written_book_content");
    private static final String WRITTEN_BOOK_CONTENT_AUTHOR = ModUtils.getScreenTranslationKey("predicate.components.written_book_content.author");
    private static final String WRITTEN_BOOK_CONTENT_TITLE = ModUtils.getScreenTranslationKey("predicate.components.written_book_content.title");
    private static final Component WRITTEN_BOOK_CONTENT_GENERATION = ModUtils.getScreenTranslated("predicate.components.written_book_content.generation");
    private static final Component WRITTEN_BOOK_CONTENT_RESOLVED = ModUtils.getScreenTranslated("predicate.components.written_book_content.resolved");
    private static final Component WRITTEN_BOOK_CONTENT_NOT_RESOLVED = ModUtils.getScreenTranslated("predicate.components.written_book_content.not_resolved");

    private static final Component ATTRIBUTE_MODIFIERS_HEADER = ModUtils.getScreenTranslated("predicate.components.attribute_modifiers");
    private static final Component ATTRIBUTE_MODIFIER_LIST = ModUtils.getScreenTranslated("predicate.components.attribute_modifier.list");
    private static final String ATTRIBUTE_MODIFIER_ID_KEY = ModUtils.getScreenTranslationKey("predicate.components.attribute_modifier.id");
    private static final Component ATTRIBUTE_MODIFIER_AMOUNT = ModUtils.getScreenTranslated("predicate.components.attribute_modifier.amount");
    private static final String ATTRIBUTE_MODIFIER_OPERATION_KEY = ModUtils.getScreenTranslationKey("predicate.components.attribute_modifier.operation");
    private static final String ATTRIBUTE_MODIFIER_SLOT_KEY = ModUtils.getScreenTranslationKey("predicate.components.attribute_modifier.slot");

    private static final Component TRIM_MATERIAL_LIST = ModUtils.getScreenTranslated("predicate.components.trim.material");
    private static final Component TRIM_PATTERN_LIST = ModUtils.getScreenTranslated("predicate.components.trim.pattern");

    private static final Component JUKEBOX_PLAYABLE_LIST = ModUtils.getScreenTranslated("predicate.components.jukebox_playable.list");

    private static final Component VILLAGER_TYPE_LIST = ModUtils.getScreenTranslated("predicate.components.villager_type.list");

    @SuppressWarnings("unchecked")
    @Contract(mutates = "param2")
    public static void tooltip(DataComponentMatchers predicate, IndentedTextHolder textHolder) {
        textHolder.accept(COMPONENTS_HEADER);
        textHolder.push();

        predicate.exact().expectedComponents.forEach(component -> {
            Identifier typeId = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component.type());
            assert typeId != null;
            if (component.type().codec() == null) {
                textHolder.accept(Component.literal(String.format("%s is not a persistent component and should not be used in a predicate!", typeId)));
                return;
            }

            String value = Researcher.GSON.toJson(((DataComponentType<@NonNull Object>)component.type()).codec().encodeStart(JsonOps.INSTANCE, component.value()));
            textHolder.accept(Component.translatable(EXACT_VALUE_KEY, typeId, value));
        });

        predicate.partial().forEach((type, partialPredicate) -> {
            if (PREDICATE_HANDLER_REGISTRY.containsKey(type)) {
                PREDICATE_HANDLER_REGISTRY.get(type).accept(partialPredicate, textHolder);
            }
            else if (partialPredicate instanceof AnyValue(DataComponentType<?> componentType)) {
                textHolder.accept(Component.translatable(ANY_VALUE_KEY, Objects.requireNonNull(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(componentType))));
            }
            else {
                textHolder.accept(Component.literal(String.format("No handler for %s", BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE.getKey(type))));
            }
        });

        textHolder.pop();
    }

    private static void damageTooltip(DamagePredicate predicate, IndentedTextHolder textHolder) {
        MinMaxBoundsUtils.tooltip(predicate.durability(), DAMAGE_DURABILITY, textHolder);
        MinMaxBoundsUtils.tooltip(predicate.damage(), DAMAGE_DAMAGE, textHolder);
    }

    private static void enchantmentsTooltip(EnchantmentsPredicate predicate, IndentedTextHolder textHolder) {
        predicate.enchantments.forEach(enchantmentPredicate -> {
            PredicateHelper.tooltip(enchantmentPredicate, (p, t) -> {
                if (p.enchantments().isPresent() && p.enchantments().get().stream().findAny().isPresent()) {
                    t.accept(ENCHANTMENTS_LIST);
                    t.push();
                    p.enchantments().get().forEach(entry -> textHolder.accept(entry.value().description()));
                    t.pop();
                }
                MinMaxBoundsUtils.tooltip(p.level(), ENCHANTMENTS_LEVELS, t);
            }, ENCHANTMENTS_HEADER);
        });
    }

    private static void potionContentsTooltip(PotionsPredicate predicate, IndentedTextHolder textHolder) {
        predicate.potions().forEach(entry -> textHolder.accept(Component.translatable(ItemUtils.getPotionTranslationKey(entry))));
    }

    private static void customDataTooltip(CustomDataPredicate predicate, IndentedTextHolder textHolder) {
        PredicateHelper.nbtTooltip(predicate.value(), textHolder);
    }

    private static void containerTooltip(ContainerPredicate predicate, IndentedTextHolder textHolder) {
        if (predicate.items().isPresent()) {
            PredicateHelper.collectionTooltip(predicate.items().get(), ComponentsPredicateHelper::containerItemTooltip, textHolder);
        }
    }

    private static void containerItemTooltip(ItemPredicate predicate, IndentedTextHolder textHolder) {
        PredicateHelper.tooltip(predicate, ItemPredicateHelper::tooltip, CONTAINER_ITEM);
    }

    private static void bundleContentsTooltip(BundlePredicate predicate, IndentedTextHolder textHolder) {
        if (predicate.items().isPresent()) {
            PredicateHelper.collectionTooltip(predicate.items().get(), ComponentsPredicateHelper::bundleItemTooltip, textHolder);
        }
    }

    private static void bundleItemTooltip(ItemPredicate predicate, IndentedTextHolder textHolder) {
        PredicateHelper.tooltip(predicate, ItemPredicateHelper::tooltip, BUNDLE_CONTENTS_ITEM);
    }

    private static void fireworkExplosionTooltip(FireworkExplosionPredicate predicate, IndentedTextHolder textHolder) {
        fireworkExplosionPredicateTooltip(predicate.predicate(), textHolder);
    }

    private static void fireworkExplosionPredicateTooltip(FireworkExplosionPredicate.FireworkPredicate predicate, IndentedTextHolder textHolder) {
        if (predicate.shape().isPresent()) {
            textHolder.accept(Component.translatable(FIREWORK_EXPLOSION_SHAPE_KEY, predicate.shape().get().getName().getString()));
        }
        PredicateHelper.optionalBooleanTooltip(predicate.twinkle(), FIREWORK_EXPLOSION_TWINKLE, FIREWORK_EXPLOSION_NO_TWINKLE, textHolder);
        PredicateHelper.optionalBooleanTooltip(predicate.trail(), FIREWORK_EXPLOSION_TRAIL, FIREWORK_EXPLOSION_NO_TRAIL, textHolder);

    }

    private static void fireworksTooltip(FireworksPredicate predicate, IndentedTextHolder textHolder) {
        MinMaxBoundsUtils.tooltip(predicate.flightDuration(), FIREWORKS_FLIGHT_DURATION, textHolder);
        if (predicate.explosions().isPresent()) {
            PredicateHelper.collectionTooltip(predicate.explosions().get(), (p, t) -> {
                PredicateHelper.tooltip(p, ComponentsPredicateHelper::fireworkExplosionPredicateTooltip, FIREWORK_EXPLOSION_HEADER);
            }, textHolder);
        }
    }

    private static void writableBookContentTooltip(WritableBookPredicate predicate, IndentedTextHolder textHolder) {
        if (predicate.pages().isPresent()) {
            PredicateHelper.collectionTooltip(predicate.pages().get(), (p, t) -> t.accept(Component.literal(p.contents())), textHolder);
        }
    }

    private static void writtenBookContentTooltip(WrittenBookPredicate predicate, IndentedTextHolder textHolder) {
        if (predicate.pages().isPresent()) {
            PredicateHelper.collectionTooltip(predicate.pages().get(), (p, t) -> t.accept(p.contents()), textHolder);
        }
        if (predicate.author().isPresent()) {
            textHolder.accept(Component.translatable(WRITTEN_BOOK_CONTENT_AUTHOR, predicate.author()));
        }
        if (predicate.title().isPresent()) {
            textHolder.accept(Component.translatable(WRITTEN_BOOK_CONTENT_TITLE, predicate.title()));
        }
        MinMaxBoundsUtils.tooltip(predicate.generation(), WRITTEN_BOOK_CONTENT_GENERATION, textHolder);
        PredicateHelper.optionalBooleanTooltip(predicate.resolved(), WRITTEN_BOOK_CONTENT_RESOLVED, WRITTEN_BOOK_CONTENT_NOT_RESOLVED, textHolder);
    }

    private static void attributeModifiersTooltip(AttributeModifiersPredicate predicate, IndentedTextHolder textHolder) {
        if (predicate.modifiers().isPresent()) {
            PredicateHelper.collectionTooltip(predicate.modifiers().get(), ComponentsPredicateHelper::attributeModifierTooltip, textHolder);
        }
    }

    private static void attributeModifierTooltip(AttributeModifiersPredicate.EntryPredicate predicate, IndentedTextHolder textHolder) {
        textHolder.accept(ATTRIBUTE_MODIFIERS_HEADER);
        textHolder.push();

        if (predicate.attribute().isPresent() && predicate.attribute().get().stream().findAny().isEmpty()) {
            textHolder.accept(ATTRIBUTE_MODIFIER_LIST);
            textHolder.push();
            predicate.attribute().get().forEach(entry -> textHolder.accept(Component.translatable(entry.value().getDescriptionId())));
            textHolder.pop();
        }
        if (predicate.id().isPresent()) {
            textHolder.accept(Component.translatable(ATTRIBUTE_MODIFIER_ID_KEY, predicate.id().get()));
        }
        MinMaxBoundsUtils.tooltip(predicate.amount(), ATTRIBUTE_MODIFIER_AMOUNT, textHolder);
        if (predicate.operation().isPresent()) {
            textHolder.accept(Component.translatable(ATTRIBUTE_MODIFIER_OPERATION_KEY, predicate.operation().get().getSerializedName()));
        }
        if (predicate.slot().isPresent()) {
            textHolder.accept(Component.translatable(ATTRIBUTE_MODIFIER_SLOT_KEY, predicate.slot().get().getSerializedName()));
        }

        textHolder.pop();
    }

    private static void trimTooltip(TrimPredicate predicate, IndentedTextHolder textHolder) {
        if (predicate.material().isPresent() && predicate.material().get().stream().findAny().isPresent()) {
            textHolder.accept(TRIM_MATERIAL_LIST);
            textHolder.push();
            predicate.material().get().forEach(entry -> textHolder.accept(entry.value().description()));
            textHolder.pop();
        }
        if (predicate.pattern().isPresent() && predicate.pattern().get().stream().findAny().isPresent()) {
            textHolder.accept(TRIM_PATTERN_LIST);
            textHolder.push();
            predicate.pattern().get().forEach(entry -> textHolder.accept(entry.value().description()));
            textHolder.pop();
        }
    }

    private static void jukeboxPlayableTooltip(JukeboxPlayablePredicate predicate, IndentedTextHolder textHolder) {
        if (predicate.song().isPresent() && predicate.song().get().size() > 0) {
            textHolder.accept(JUKEBOX_PLAYABLE_LIST);
            textHolder.push();
            predicate.song().get().forEach(entry -> textHolder.accept(entry.value().description()));
            textHolder.pop();
        }
    }

    private static void villagerTypeTooltip(VillagerTypePredicate predicate, IndentedTextHolder textHolder) {
        if (predicate.villagerTypes().size() > 0) {
            textHolder.accept(VILLAGER_TYPE_LIST);
            textHolder.push();
            predicate.villagerTypes().forEach(entry -> textHolder.accept(Component.literal(entry.unwrapKey().orElseThrow().identifier().toLanguageKey("villager_type"))));
            textHolder.pop();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends DataComponentPredicate> void registerHandler(DataComponentPredicate.Type<T> type, BiConsumer<T, IndentedTextHolder> handler, @Nullable Component headerText) {
        Objects.requireNonNull(type, "type is null");
        Objects.requireNonNull(handler, "handler is null");
        if (headerText != null) {
            PREDICATE_HANDLER_REGISTRY.put(type, (p, _) -> PredicateHelper.tooltip(p, (BiConsumer)handler, headerText));
        }
        else {
            PREDICATE_HANDLER_REGISTRY.put(type, (BiConsumer)handler);
        }
    }

    @ApiStatus.Internal
    public static void printNonRegistered() {
        BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE.forEach(type -> {
            if (!PREDICATE_HANDLER_REGISTRY.containsKey(type)) {
                Researcher.LOGGER.warn("Data component predicate {} does not have a registered handler", BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE.getKey(type));
            }
        });
    }

    static {
        registerHandler(DataComponentPredicates.DAMAGE, ComponentsPredicateHelper::damageTooltip, DAMAGE_HEADER);
        registerHandler(DataComponentPredicates.ENCHANTMENTS, ComponentsPredicateHelper::enchantmentsTooltip, null);
        registerHandler(DataComponentPredicates.STORED_ENCHANTMENTS, ComponentsPredicateHelper::enchantmentsTooltip, null);
        registerHandler(DataComponentPredicates.POTIONS, ComponentsPredicateHelper::potionContentsTooltip, POTION_CONTENTS_HEADER);
        registerHandler(DataComponentPredicates.CUSTOM_DATA, ComponentsPredicateHelper::customDataTooltip, null);
        registerHandler(DataComponentPredicates.CONTAINER, ComponentsPredicateHelper::containerTooltip, CONTAINER_HEADER);
        registerHandler(DataComponentPredicates.BUNDLE_CONTENTS, ComponentsPredicateHelper::bundleContentsTooltip, BUNDLE_CONTENTS_HEADER);
        registerHandler(DataComponentPredicates.FIREWORK_EXPLOSION, ComponentsPredicateHelper::fireworkExplosionTooltip, FIREWORK_EXPLOSION_HEADER);
        registerHandler(DataComponentPredicates.FIREWORKS, ComponentsPredicateHelper::fireworksTooltip, FIREWORKS_HEADER);
        registerHandler(DataComponentPredicates.WRITABLE_BOOK, ComponentsPredicateHelper::writableBookContentTooltip, WRITABLE_BOOK_CONTENT_HEADER);
        registerHandler(DataComponentPredicates.WRITTEN_BOOK, ComponentsPredicateHelper::writtenBookContentTooltip, WRITTEN_BOOK_CONTENT_HEADER);
        registerHandler(DataComponentPredicates.ATTRIBUTE_MODIFIERS, ComponentsPredicateHelper::attributeModifiersTooltip, ATTRIBUTE_MODIFIERS_HEADER);
        registerHandler(DataComponentPredicates.ARMOR_TRIM, ComponentsPredicateHelper::trimTooltip, null);
        registerHandler(DataComponentPredicates.JUKEBOX_PLAYABLE, ComponentsPredicateHelper::jukeboxPlayableTooltip, null);
        registerHandler(DataComponentPredicates.VILLAGER_VARIANT, ComponentsPredicateHelper::villagerTypeTooltip, null);
    }
}
