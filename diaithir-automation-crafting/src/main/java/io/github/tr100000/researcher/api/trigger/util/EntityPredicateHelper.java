package io.github.tr100000.researcher.api.trigger.util;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.Researcher;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.element.GroupedElement;
import io.github.tr100000.researcher.api.trigger.element.IconElement;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import io.github.tr100000.researcher.api.trigger.element.TimedSwitchingElement;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import io.github.tr100000.trutils.api.gui.Icon;
import io.github.tr100000.trutils.api.gui.ItemIcon;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityEquipmentPredicate;
import net.minecraft.advancements.criterion.EntityFlagsPredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.advancements.criterion.FishingHookPredicate;
import net.minecraft.advancements.criterion.FoodPredicate;
import net.minecraft.advancements.criterion.GameTypePredicate;
import net.minecraft.advancements.criterion.InputPredicate;
import net.minecraft.advancements.criterion.LightningBoltPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.MobEffectsPredicate;
import net.minecraft.advancements.criterion.PlayerPredicate;
import net.minecraft.advancements.criterion.RaiderPredicate;
import net.minecraft.advancements.criterion.SheepPredicate;
import net.minecraft.advancements.criterion.SlimePredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class EntityPredicateHelper {
    private EntityPredicateHelper() {}

    private static final Map<EntityType<?>, Icon> ENTITY_TYPE_ICONS = new Object2ObjectOpenHashMap<>();

    private static final Component PREDICATE_MISSING = ModUtils.getScreenTranslated("predicate.entity.properties_missing");
    private static final Component ANY_ENTITY = ModUtils.getScreenTranslated("predicate.entity.any");

    private static final Component ENTITY_TYPE = ModUtils.getScreenTranslated("predicate.entity.type");
    private static final Component ENTITY_LOCATED = ModUtils.getScreenTranslated("predicate.entity.location");
    private static final Component ENTITY_STEPPING_ON = ModUtils.getScreenTranslated("predicate.entity.stepping_on");
    private static final Component ENTITY_MOVEMENT_AFFECTED_BY = ModUtils.getScreenTranslated("predicate.entity.movement_affected_by");
    private static final Component ENTITY_EFFECTS_HEADER = ModUtils.getScreenTranslated("predicate.entity.effects");
    private static final String ENTITY_PERIODIC_KEY = ModUtils.getScreenTranslationKey("predicate.entity.periodic");
    private static final Component ENTITY_VEHICLE = ModUtils.getScreenTranslated("predicate.entity.vehicle");
    private static final Component ENTITY_PASSENGER = ModUtils.getScreenTranslated("predicate.entity.passenger");
    private static final Component ENTITY_TARGETED = ModUtils.getScreenTranslated("predicate.entity.targeted");
    private static final String ENTITY_TEAM_KEY = ModUtils.getScreenTranslationKey("predicate.entity.team");

    private static final String EFFECT_HEADER_KEY = ModUtils.getScreenTranslationKey("predicate.entity.effect.header");
    private static final Component EFFECT_AMPLIFIER = ModUtils.getScreenTranslated("predicate.entity.effect.amplifier");
    private static final Component EFFECT_DURATION = ModUtils.getScreenTranslated("predicate.entity.effect.duration");
    private static final Component EFFECT_AMBIENT = ModUtils.getScreenTranslated("predicate.entity.effect.ambient");
    private static final Component EFFECT_NOT_AMBIENT = ModUtils.getScreenTranslated("predicate.entity.effect.not_ambient");
    private static final Component EFFECT_VISIBLE = ModUtils.getScreenTranslated("predicate.entity.effect.visible");
    private static final Component EFFECT_NOT_VISIBLE = ModUtils.getScreenTranslated("predicate.entity.effect.not_visible");

    private static final Component FLAGS_ON_GROUND = ModUtils.getScreenTranslated("predicate.entity.flags.on_ground");
    private static final Component FLAGS_NOT_ON_GROUND = ModUtils.getScreenTranslated("predicate.entity.flags.not_on_ground");
    private static final Component FLAGS_ON_FIRE = ModUtils.getScreenTranslated("predicate.entity.flags.on_fire");
    private static final Component FLAGS_NOT_ON_FIRE = ModUtils.getScreenTranslated("predicate.entity.flags.not_on_fire");
    private static final Component FLAGS_SNEAKING = ModUtils.getScreenTranslated("predicate.entity.flags.sneaking");
    private static final Component FLAGS_NOT_SNEAKING = ModUtils.getScreenTranslated("predicate.entity.flags.not_sneaking");
    private static final Component FLAGS_SPRINTING = ModUtils.getScreenTranslated("predicate.entity.flags.sprinting");
    private static final Component FLAGS_NOT_SPRINTING = ModUtils.getScreenTranslated("predicate.entity.flags.not_sprinting");
    private static final Component FLAGS_SWIMMING = ModUtils.getScreenTranslated("predicate.entity.flags.swimming");
    private static final Component FLAGS_NOT_SWIMMING = ModUtils.getScreenTranslated("predicate.entity.flags.not_swimming");
    private static final Component FLAGS_FLYING = ModUtils.getScreenTranslated("predicate.entity.flags.flying");
    private static final Component FLAGS_NOT_FLYING = ModUtils.getScreenTranslated("predicate.entity.flags.not_flying");
    private static final Component FLAGS_BABY = ModUtils.getScreenTranslated("predicate.entity.flags.baby");
    private static final Component FLAGS_NOT_BABY = ModUtils.getScreenTranslated("predicate.entity.flags.not_baby");
    private static final Component FLAGS_IN_WATER = ModUtils.getScreenTranslated("predicate.entity.flags.in_water");
    private static final Component FLAGS_NOT_IN_WATER = ModUtils.getScreenTranslated("predicate.entity.flags.not_in_water");
    private static final Component FLAGS_FALL_FLYING = ModUtils.getScreenTranslated("predicate.entity.flags.fall_flying");
    private static final Component FLAGS_NOT_FALL_FLYING = ModUtils.getScreenTranslated("predicate.entity.flags.not_fall_flying");

    private static final Component EQUIPMENT_HEAD = ModUtils.getScreenTranslated("predicate.entity.equipment.head");
    private static final Component EQUIPMENT_CHEST = ModUtils.getScreenTranslated("predicate.entity.equipment.chest");
    private static final Component EQUIPMENT_LEGS = ModUtils.getScreenTranslated("predicate.entity.equipment.legs");
    private static final Component EQUIPMENT_FEET = ModUtils.getScreenTranslated("predicate.entity.equipment.feet");
    private static final Component EQUIPMENT_BODY = ModUtils.getScreenTranslated("predicate.entity.equipment.body");
    private static final Component EQUIPMENT_MAINHAND = ModUtils.getScreenTranslated("predicate.entity.equipment.mainhand");
    private static final Component EQUIPMENT_OFFHAND = ModUtils.getScreenTranslated("predicate.entity.equipment.offhand");

    private static final Component LIGHTNING_BLOCKS_SET_ON_FIRE = ModUtils.getScreenTranslated("predicate.entity.lightning.blocks_set_on_fire");
    private static final Component LIGHTNING_ENTITY_STRUCK = ModUtils.getScreenTranslated("predicate.entity.lightning.entity_struck");
    private static final Component FISHING_OPEN_WATER = ModUtils.getScreenTranslated("predicate.entity.fishing.open_water");
    private static final Component FISHING_NOT_OPEN_WATER = ModUtils.getScreenTranslated("predicate.entity.fishing.not_open_water");
    private static final Component PLAYER_EXPERIENCE_LEVEL = ModUtils.getScreenTranslated("predicate.entity.player.xp_level");
    private static final Component PLAYER_FOOD_LEVEL = ModUtils.getScreenTranslated("predicate.entity.player.food.level");
    private static final Component PLAYER_FOOD_SATURATION = ModUtils.getScreenTranslated("predicate.entity.food.saturation");
    private static final Component PLAYER_GAME_MODE = ModUtils.getScreenTranslated("predicate.entity.player.game_mode");
    private static final Component PLAYER_STATS = ModUtils.getScreenTranslated("predicate.entity.player.stats");
    private static final String PLAYER_HAS_RECIPE = ModUtils.getScreenTranslationKey("predicate.entity.player.has_recipe");
    private static final String PLAYER_DOESNT_HAVE_RECIPE = ModUtils.getScreenTranslationKey("predicate.entity.player.doesnt_have_recipe");
    private static final String PLAYER_ADVANCEMENT = ModUtils.getScreenTranslationKey("predicate.entity.player.advancement");
    private static final String PLAYER_ADVANCEMENT_CRITERIA_DONE = ModUtils.getScreenTranslationKey("predicate.entity.player.advancement.criteria.done");
    private static final String PLAYER_ADVANCEMENT_CRITERIA_NOT_DONE = ModUtils.getScreenTranslationKey("predicate.entity.player.advancement.criteria.not_done");
    private static final Component PLAYER_ADVANCEMENT_DONE = ModUtils.getScreenTranslated("predicate.entity.player.advancement.done");
    private static final Component PLAYER_ADVANCEMENT_NOT_DONE = ModUtils.getScreenTranslated("predicate.entity.player.advancement.not_done");
    private static final Component PLAYER_LOOKING_AT = ModUtils.getScreenTranslated("predicate.entity.player.looking_at");
    private static final Component PLAYER_INPUT_FORWARD = ModUtils.getScreenTranslated("predicate.entity.player.input.forward");
    private static final Component PLAYER_INPUT_NOT_FORWARD = ModUtils.getScreenTranslated("predicate.entity.player.input.not_forward");
    private static final Component PLAYER_INPUT_BACKWARD = ModUtils.getScreenTranslated("predicate.entity.player.input.backward");
    private static final Component PLAYER_INPUT_NOT_BACKWARD = ModUtils.getScreenTranslated("predicate.entity.player.input.not_backward");
    private static final Component PLAYER_INPUT_LEFT = ModUtils.getScreenTranslated("predicate.entity.player.input.left");
    private static final Component PLAYER_INPUT_NOT_LEFT = ModUtils.getScreenTranslated("predicate.entity.player.input.not_left");
    private static final Component PLAYER_INPUT_RIGHT = ModUtils.getScreenTranslated("predicate.entity.player.input.right");
    private static final Component PLAYER_INPUT_NOT_RIGHT = ModUtils.getScreenTranslated("predicate.entity.player.input.not_right");
    private static final Component PLAYER_INPUT_JUMP = ModUtils.getScreenTranslated("predicate.entity.player.input.jump");
    private static final Component PLAYER_INPUT_NOT_JUMP = ModUtils.getScreenTranslated("predicate.entity.player.input.not_jump");
    private static final Component PLAYER_INPUT_SNEAK = ModUtils.getScreenTranslated("predicate.entity.player.input.sneak");
    private static final Component PLAYER_INPUT_NOT_SNEAK = ModUtils.getScreenTranslated("predicate.entity.player.input.not_sneak");
    private static final Component PLAYER_INPUT_SPRINT = ModUtils.getScreenTranslated("predicate.entity.player.input.sprint");
    private static final Component PLAYER_INPUT_NOT_SPRINT = ModUtils.getScreenTranslated("predicate.entity.player.input.not_sprint");
    private static final Component SLIME_SIZE = ModUtils.getScreenTranslated("predicate.entity.slime.size");
    private static final Component RAIDER_HAS_RAID = ModUtils.getScreenTranslated("predicate.entity.raider.has_raid");
    private static final Component RAIDER_DOESNT_HAVE_RAID = ModUtils.getScreenTranslated("predicate.entity.raider.doesnt_have_raid");
    private static final Component RAIDER_IS_CAPTAIN = ModUtils.getScreenTranslated("predicate.entity.raider.is_captain");
    private static final Component RAIDER_NOT_CAPTAIN = ModUtils.getScreenTranslated("predicate.entity.raider.not_captain");
    private static final Component SHEEP_SHEARED = ModUtils.getScreenTranslated("predicate.entity.sheep.sheared");
    private static final Component SHEEP_NOT_SHEARED = ModUtils.getScreenTranslated("predicate.entity.sheep.not_sheared");

    @Contract(mutates = "param2")
    public static void tooltip(EntityPredicate predicate, IndentedTextHolder textHolder) {
        if (predicate.entityType().isPresent() && predicate.entityType().get().types().size() > 1) {
            textHolder.accept(ENTITY_TYPE);
            textHolder.push();
            predicate.entityType().get().types().forEach(entry -> textHolder.accept(entry.value().getDescription()));
            textHolder.pop();
        }
        if (predicate.distanceToPlayer().isPresent()) {
            DistancePredicateHelper.tooltip(predicate.distanceToPlayer().get(), textHolder);
        }
        if (predicate.movement().isPresent()) {
            MovementPredicateHelper.tooltip(predicate.movement().get(), textHolder);
        }
        if (predicate.location().located().isPresent()) {
            textHolder.accept(ENTITY_LOCATED);
            textHolder.push();
            LocationPredicateHelper.tooltip(predicate.location().located().get(), textHolder);
            textHolder.pop();
        }
        if (predicate.location().steppingOn().isPresent()) {
            textHolder.accept(ENTITY_STEPPING_ON);
            textHolder.push();
            LocationPredicateHelper.tooltip(predicate.location().steppingOn().get(), textHolder);
            textHolder.pop();
        }
        if (predicate.location().affectsMovement().isPresent()) {
            textHolder.accept(ENTITY_MOVEMENT_AFFECTED_BY);
            textHolder.push();
            LocationPredicateHelper.tooltip(predicate.location().affectsMovement().get(), textHolder);
            textHolder.pop();
        }
        if (predicate.effects().isPresent()) {
            textHolder.accept(ENTITY_EFFECTS_HEADER);
            textHolder.push();
            effectTooltip(predicate.effects().get(), textHolder);
            textHolder.pop();
        }
        if (predicate.nbt().isPresent()) {
            PredicateHelper.nbtTooltip(predicate.nbt().get(), textHolder);
        }
        if (predicate.flags().isPresent()) {
            flagsTooltip(predicate.flags().get(), textHolder);
        }
        if (predicate.equipment().isPresent()) {
            equipmentTooltip(predicate.equipment().get(), textHolder);
        }
        if (predicate.subPredicate().isPresent()) {
            typeSpecificTooltip(predicate.subPredicate().get(), textHolder);
        }
        if (predicate.periodicTick().isPresent()) {
            textHolder.accept(Component.translatable(ENTITY_PERIODIC_KEY, predicate.periodicTick().get()));
        }
        if (predicate.vehicle().isPresent()) {
            textHolder.accept(ENTITY_VEHICLE);
            textHolder.push();
            tooltip(predicate.vehicle().get(), textHolder);
            textHolder.pop();
        }
        if (predicate.passenger().isPresent()) {
            textHolder.accept(ENTITY_PASSENGER);
            textHolder.push();
            tooltip(predicate.passenger().get(), textHolder);
            textHolder.pop();
        }
        if (predicate.targetedEntity().isPresent()) {
            textHolder.accept(ENTITY_TARGETED);
            textHolder.push();
            tooltip(predicate.targetedEntity().get(), textHolder);
            textHolder.pop();
        }
        if (predicate.team().isPresent()) {
            textHolder.accept(Component.translatable(ENTITY_TEAM_KEY, predicate.team().get()));
        }
        if (predicate.slots().isPresent()) {
            // TODO
            textHolder.accept(Component.literal("TODO slot conditions"));
        }
        if (!predicate.components().isEmpty()) {
            ComponentsPredicateHelper.tooltip(predicate.components(), textHolder);
        }
    }

    @Contract(mutates = "param2")
    public static void tooltip(@Nullable ContextAwarePredicate predicate, IndentedTextHolder textHolder) {
        Optional<EntityPredicate> entityPredicate = entityPredicateFromLootContextPredicate(predicate);
        entityPredicate.ifPresentOrElse(p -> tooltip(p, textHolder), () -> textHolder.accept(PREDICATE_MISSING));
    }

    public static Optional<List<MutableComponent>> tooltip(Optional<ContextAwarePredicate> predicate, @Nullable Component headerText) {
        return PredicateHelper.optionalTooltip(predicate, EntityPredicateHelper::tooltip, headerText);
    }

    @Contract(value = "_ -> new", pure = true)
    public static TriggerDisplayElement element(EntityType<?> entityType) {
        return new GroupedElement(
                new IconElement(ENTITY_TYPE_ICONS.getOrDefault(entityType, Icon.ERROR)),
                new TextElement(entityType.getDescription())
        );
    }

    @Contract(value = "_ -> new", pure = true)
    public static TriggerDisplayElement element(@Nullable EntityPredicate predicate) {
        if (predicate != null && predicate.entityType().isPresent()) {
            List<TriggerDisplayElement> list = predicate.entityType().get().types().stream().map(Holder::value).map(EntityPredicateHelper::element).toList();
            if (list.size() == 1) {
                return list.getFirst();
            }
            else if (list.size() >= 2) {
                return new GroupedElement(new TimedSwitchingElement(list), new TextElement(Component.literal("*")));
            }
        }
        return new TextElement(ANY_ENTITY);
    }

    @Contract(value = "_ -> new", pure = true)
    public static TriggerDisplayElement element(@Nullable ContextAwarePredicate predicate) {
        Optional<EntityPredicate> entityPredicate = entityPredicateFromLootContextPredicate(predicate);
        return entityPredicate.map(EntityPredicateHelper::element).orElseGet(() -> new TextElement(ANY_ENTITY));
    }

    @Contract(value = "_ -> new", pure = true)
    public static TriggerDisplayElement vehicleElement(@Nullable ContextAwarePredicate predicate) {
        Optional<EntityPredicate> entityPredicate = entityPredicateFromLootContextPredicate(predicate);
        Optional<EntityPredicate> vehiclePredicate = entityPredicate.flatMap(EntityPredicate::vehicle);
        return element(vehiclePredicate.orElse(null));
    }

    @Contract(pure = true)
    private static Optional<EntityPredicate> entityPredicateFromLootContextPredicate(@Nullable ContextAwarePredicate predicate) {
        if (predicate == null) return Optional.empty();

        for (LootItemCondition condition : predicate.conditions) {
            if (condition instanceof LootItemEntityPropertyCondition entityPropertiesCondition) {
                return entityPropertiesCondition.predicate();
            }
        }
        return Optional.empty();
    }

    @Contract(mutates = "param2")
    public static void effectTooltip(MobEffectsPredicate predicate, IndentedTextHolder textHolder) {
        predicate.effectMap().forEach((entry, data) -> {
            textHolder.accept(Component.translatable(EFFECT_HEADER_KEY, entry.unwrapKey().orElseThrow().identifier().toLanguageKey("effect")));
            textHolder.push();

            MinMaxBoundsUtils.tooltip(data.amplifier(), EFFECT_AMPLIFIER, textHolder);
            MinMaxBoundsUtils.tooltip(data.duration(), EFFECT_DURATION, textHolder);
            PredicateHelper.optionalBooleanTooltip(data.ambient(), EFFECT_AMBIENT, EFFECT_NOT_AMBIENT, textHolder);
            PredicateHelper.optionalBooleanTooltip(data.visible(), EFFECT_VISIBLE, EFFECT_NOT_VISIBLE, textHolder);

            textHolder.pop();
        });
    }

    @Contract(mutates = "param2")
    public static void flagsTooltip(EntityFlagsPredicate predicate, IndentedTextHolder textHolder) {
        PredicateHelper.optionalBooleanTooltip(predicate.isOnGround(), FLAGS_ON_GROUND, FLAGS_NOT_ON_GROUND, textHolder);
        PredicateHelper.optionalBooleanTooltip(predicate.isOnFire(), FLAGS_ON_FIRE, FLAGS_NOT_ON_FIRE, textHolder);
        PredicateHelper.optionalBooleanTooltip(predicate.isCrouching(), FLAGS_SNEAKING, FLAGS_NOT_SNEAKING, textHolder);
        PredicateHelper.optionalBooleanTooltip(predicate.isSprinting(), FLAGS_SPRINTING, FLAGS_NOT_SPRINTING, textHolder);
        PredicateHelper.optionalBooleanTooltip(predicate.isSwimming(), FLAGS_SWIMMING, FLAGS_NOT_SWIMMING, textHolder);
        PredicateHelper.optionalBooleanTooltip(predicate.isFlying(), FLAGS_FLYING, FLAGS_NOT_FLYING, textHolder);
        PredicateHelper.optionalBooleanTooltip(predicate.isBaby(), FLAGS_BABY, FLAGS_NOT_BABY, textHolder);
        PredicateHelper.optionalBooleanTooltip(predicate.isInWater(), FLAGS_IN_WATER, FLAGS_NOT_IN_WATER, textHolder);
        PredicateHelper.optionalBooleanTooltip(predicate.isFallFlying(), FLAGS_FALL_FLYING, FLAGS_NOT_FALL_FLYING, textHolder);
    }

    @Contract(mutates = "param2")
    public static void equipmentTooltip(EntityEquipmentPredicate predicate, IndentedTextHolder textHolder) {
        if (predicate.head().isPresent()) {
            textHolder.accept(EQUIPMENT_HEAD);
            textHolder.push();
            ItemPredicateHelper.tooltip(predicate.head().get(), textHolder);
            textHolder.pop();
        }
        if (predicate.chest().isPresent()) {
            textHolder.accept(EQUIPMENT_CHEST);
            textHolder.push();
            ItemPredicateHelper.tooltip(predicate.chest().get(), textHolder);
            textHolder.pop();
        }
        if (predicate.legs().isPresent()) {
            textHolder.accept(EQUIPMENT_LEGS);
            textHolder.push();
            ItemPredicateHelper.tooltip(predicate.legs().get(), textHolder);
            textHolder.pop();
        }
        if (predicate.feet().isPresent()) {
            textHolder.accept(EQUIPMENT_FEET);
            textHolder.push();
            ItemPredicateHelper.tooltip(predicate.feet().get(), textHolder);
            textHolder.pop();
        }
        if (predicate.body().isPresent()) {
            textHolder.accept(EQUIPMENT_BODY);
            textHolder.push();
            ItemPredicateHelper.tooltip(predicate.body().get(), textHolder);
            textHolder.pop();
        }
        if (predicate.mainhand().isPresent()) {
            textHolder.accept(EQUIPMENT_MAINHAND);
            textHolder.push();
            ItemPredicateHelper.tooltip(predicate.mainhand().get(), textHolder);
            textHolder.pop();
        }
        if (predicate.offhand().isPresent()) {
            textHolder.accept(EQUIPMENT_OFFHAND);
            textHolder.push();
            ItemPredicateHelper.tooltip(predicate.offhand().get(), textHolder);
            textHolder.pop();
        }
    }

    @Contract(mutates = "param2")
    public static void typeSpecificTooltip(EntitySubPredicate predicate, IndentedTextHolder textHolder) {
        if (predicate instanceof LightningBoltPredicate(
                MinMaxBounds.Ints blocksSetOnFire, Optional<EntityPredicate> entityStruck
        )) {
            MinMaxBoundsUtils.tooltip(blocksSetOnFire, LIGHTNING_BLOCKS_SET_ON_FIRE, textHolder);
            if (entityStruck.isPresent()) {
                textHolder.accept(LIGHTNING_ENTITY_STRUCK);
                textHolder.push();
                tooltip(entityStruck.get(), textHolder);
                textHolder.pop();
            }
        }

        if (predicate instanceof FishingHookPredicate(
                Optional<Boolean> inOpenWater
        )) {
            PredicateHelper.optionalBooleanTooltip(inOpenWater, FISHING_OPEN_WATER, FISHING_NOT_OPEN_WATER, textHolder);
        }

        if (predicate instanceof PlayerPredicate(
                MinMaxBounds.Ints experienceLevel, FoodPredicate food, GameTypePredicate gameType, List<PlayerPredicate.StatMatcher<?>> stats, Object2BooleanMap<ResourceKey<Recipe<?>>> recipes, Map<Identifier, PlayerPredicate.AdvancementPredicate> advancements, Optional<EntityPredicate> lookingAt, Optional<InputPredicate> input
        )) {
            MinMaxBoundsUtils.tooltip(experienceLevel, PLAYER_EXPERIENCE_LEVEL, textHolder);
            if (gameType != GameTypePredicate.ANY) {
                textHolder.accept(PLAYER_GAME_MODE);
                textHolder.push();
                gameType.types().forEach(mode -> textHolder.accept(mode.getLongDisplayName()));
                textHolder.pop();
            }
            MinMaxBoundsUtils.tooltip(food.level(), PLAYER_FOOD_LEVEL, textHolder);
            MinMaxBoundsUtils.tooltip(food.saturation(), PLAYER_FOOD_SATURATION, textHolder);
            if (!stats.isEmpty()) {
                textHolder.accept(PLAYER_STATS);
                textHolder.push();
                stats.forEach(matcher -> MinMaxBoundsUtils.tooltip(matcher.range(), Component.translatable(matcher.value().unwrapKey().orElseThrow().identifier().toLanguageKey("stat")), textHolder));
                textHolder.pop();
            }
            recipes.forEach((key, expected) -> {
                textHolder.accept(Component.translatable(expected ? PLAYER_HAS_RECIPE : PLAYER_DOESNT_HAVE_RECIPE, key.identifier().toString()));
            });
            if (!advancements.isEmpty()) {
                advancements.forEach((id, advPredicate) -> {
                    textHolder.accept(Component.translatable(PLAYER_ADVANCEMENT, id.toString()));
                    textHolder.push();

                    if (advPredicate instanceof PlayerPredicate.AdvancementCriterionsPredicate(Object2BooleanMap<String> criteriaMap)) {
                        criteriaMap.forEach((criteria, done) -> {
                            textHolder.accept(Component.translatable(done ? PLAYER_ADVANCEMENT_CRITERIA_DONE : PLAYER_ADVANCEMENT_CRITERIA_NOT_DONE, criteria));
                        });
                    }
                    else if (advPredicate instanceof PlayerPredicate.AdvancementDonePredicate(boolean done)) {
                        textHolder.accept(done ? PLAYER_ADVANCEMENT_DONE : PLAYER_ADVANCEMENT_NOT_DONE);
                    }

                    textHolder.pop();
                });
            }
            if (lookingAt.isPresent()) {
                textHolder.accept(PLAYER_LOOKING_AT);
                textHolder.push();
                tooltip(lookingAt.get(), textHolder);
                textHolder.pop();
            }
            if (input.isPresent()) {
                PredicateHelper.optionalBooleanTooltip(input.get().forward(), PLAYER_INPUT_FORWARD, PLAYER_INPUT_NOT_FORWARD, textHolder);
                PredicateHelper.optionalBooleanTooltip(input.get().backward(), PLAYER_INPUT_BACKWARD, PLAYER_INPUT_NOT_BACKWARD, textHolder);
                PredicateHelper.optionalBooleanTooltip(input.get().left(), PLAYER_INPUT_LEFT, PLAYER_INPUT_NOT_LEFT, textHolder);
                PredicateHelper.optionalBooleanTooltip(input.get().right(), PLAYER_INPUT_RIGHT, PLAYER_INPUT_NOT_RIGHT, textHolder);
                PredicateHelper.optionalBooleanTooltip(input.get().jump(), PLAYER_INPUT_JUMP, PLAYER_INPUT_NOT_JUMP, textHolder);
                PredicateHelper.optionalBooleanTooltip(input.get().sneak(), PLAYER_INPUT_SNEAK, PLAYER_INPUT_NOT_SNEAK, textHolder);
                PredicateHelper.optionalBooleanTooltip(input.get().sprint(), PLAYER_INPUT_SPRINT, PLAYER_INPUT_NOT_SPRINT, textHolder);
            }
        }

        if (predicate instanceof SlimePredicate(
                MinMaxBounds.Ints size
        )) {
            MinMaxBoundsUtils.tooltip(size, SLIME_SIZE, textHolder);
        }

        if (predicate instanceof RaiderPredicate(
                boolean hasRaid, boolean isCaptain
        )) {
            textHolder.accept(hasRaid ? RAIDER_HAS_RAID : RAIDER_DOESNT_HAVE_RAID);
            textHolder.accept(isCaptain ? RAIDER_IS_CAPTAIN : RAIDER_NOT_CAPTAIN);
        }

        if (predicate instanceof SheepPredicate(
                Optional<Boolean> sheared
        )) {
            PredicateHelper.optionalBooleanTooltip(sheared, SHEEP_SHEARED, SHEEP_NOT_SHEARED, textHolder);
        }
    }

    public static void registerItemForEntityType(EntityType<?> entityType, @Nullable Item item) {
        if (item != null) {
            registerIconForEntityType(entityType, new ItemIcon(item));
        }
    }

    public static void registerIconForEntityType(EntityType<?> entityType, Icon icon) {
        Objects.requireNonNull(entityType, "entityType is null");
        Objects.requireNonNull(icon, "icon in null");
        ENTITY_TYPE_ICONS.put(entityType, icon);
    }

    @ApiStatus.Internal
    public static void printNonRegistered() {
        BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> {
            if (!ENTITY_TYPE_ICONS.containsKey(entityType)) {
                Researcher.LOGGER.warn("{} doesn't have a registered item", BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
            }
        });
    }

    @ApiStatus.Internal
    public static void onItemRegistered(Item item) {
        switch (item) {
            case BoatItem boatItem -> registerItemForEntityType(boatItem.entityType, item);
            case MinecartItem minecartItem -> registerItemForEntityType(minecartItem.type, item);
            case HangingEntityItem hangingEntityItem -> registerItemForEntityType(hangingEntityItem.type, item);
            default -> {}
        }
    }

    @ApiStatus.Internal
    public static void registerDefault() {
        registerItemForEntityType(EntityType.ARMOR_STAND, Items.ARMOR_STAND);
        registerItemForEntityType(EntityType.ARROW, Items.ARROW);
        registerItemForEntityType(EntityType.SPECTRAL_ARROW, Items.SPECTRAL_ARROW);
        registerItemForEntityType(EntityType.BREEZE_WIND_CHARGE, Items.WIND_CHARGE);
        registerItemForEntityType(EntityType.EGG, Items.EGG);
        registerItemForEntityType(EntityType.ENDER_PEARL, Items.ENDER_PEARL);
        registerItemForEntityType(EntityType.END_CRYSTAL, Items.END_CRYSTAL);
        registerItemForEntityType(EntityType.EXPERIENCE_BOTTLE, Items.EXPERIENCE_BOTTLE);
        registerItemForEntityType(EntityType.EXPERIENCE_ORB, Items.EXPERIENCE_BOTTLE);
        registerItemForEntityType(EntityType.EYE_OF_ENDER, Items.ENDER_EYE);
        registerItemForEntityType(EntityType.FIREBALL, Items.FIRE_CHARGE);
        registerItemForEntityType(EntityType.FIREWORK_ROCKET, Items.FIREWORK_ROCKET);
        registerItemForEntityType(EntityType.LEASH_KNOT, Items.LEAD);
        registerItemForEntityType(EntityType.SMALL_FIREBALL, Items.FIRE_CHARGE);
        registerItemForEntityType(EntityType.SPLASH_POTION, Items.SPLASH_POTION);
        registerItemForEntityType(EntityType.LINGERING_POTION, Items.LINGERING_POTION);
        registerItemForEntityType(EntityType.SNOWBALL, Items.SNOWBALL);
        registerItemForEntityType(EntityType.TNT, Items.TNT);
        registerItemForEntityType(EntityType.TRIDENT, Items.TRIDENT);
        registerItemForEntityType(EntityType.WIND_CHARGE, Items.WIND_CHARGE);
        registerItemForEntityType(EntityType.WITHER_SKULL, Items.WITHER_SKELETON_SKULL);
    }
}
