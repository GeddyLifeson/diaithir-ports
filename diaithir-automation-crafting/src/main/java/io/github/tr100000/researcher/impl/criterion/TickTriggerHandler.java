package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.TriggerDisplay;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.TriggerHandler;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper;
import io.github.tr100000.researcher.api.trigger.util.PredicateHelper;
import io.github.tr100000.researcher.api.util.IndentedTextHolder;
import net.minecraft.advancements.criterion.PlayerTrigger;
import net.minecraft.network.chat.Component;

public class TickTriggerHandler implements TriggerHandler<PlayerTrigger.TriggerInstance> {
    public static final TickTriggerHandler LOCATION = new TickTriggerHandler(
            ModUtils.getScreenTranslationKey("trigger.location.one"),
            ModUtils.getScreenTranslationKey("trigger.location.many"),
            false
    );

    public static final TickTriggerHandler TICK = new TickTriggerHandler(
            ModUtils.getScreenTranslationKey("trigger.tick.one"),
            ModUtils.getScreenTranslationKey("trigger.tick.many"),
            true
    );

    public static final TickTriggerHandler SLEPT_IN_BED = new TickTriggerHandler(
            ModUtils.getScreenTranslationKey("trigger.slept_in_bed.one"),
            ModUtils.getScreenTranslationKey("trigger.slept_in_bed.many"),
            false
    );

    public static final TickTriggerHandler HERO_OF_THE_VILLAGE = new TickTriggerHandler(
            ModUtils.getScreenTranslationKey("trigger.hero_of_the_village.one"),
            ModUtils.getScreenTranslationKey("trigger.hero_of_the_village.many"),
            false
    );

    public static final TickTriggerHandler VOLUNTARY_EXILE = new TickTriggerHandler(
            ModUtils.getScreenTranslationKey("trigger.voluntary_exile.one"),
            ModUtils.getScreenTranslationKey("trigger.voluntary_exile.many"),
            false
    );

    public static final TickTriggerHandler AVOID_VIBRATION = new TickTriggerHandler(
            ModUtils.getScreenTranslationKey("trigger.avoid_vibration.one"),
            ModUtils.getScreenTranslationKey("trigger.avoid_vibration.many"),
            false
    );

    private static final Component PLAYER_CONDITIONS_HEADER = ModUtils.getScreenTranslated("predicate.player");

    private final String oneTextKey;
    private final String oneWithConditionsTextKey;
    private final String manyTextKey;
    private final String manyWithConditionsTextKey;
    private final boolean countIsTicks;

    public TickTriggerHandler(String oneTextKey, String oneWithConditionsTextKey, String manyTextKey, String manyWithConditionsTextKey, boolean countIsTicks) {
        this.oneTextKey = oneTextKey;
        this.oneWithConditionsTextKey = oneWithConditionsTextKey;
        this.manyTextKey = manyTextKey;
        this.manyWithConditionsTextKey = manyWithConditionsTextKey;
        this.countIsTicks = countIsTicks;
    }

    public TickTriggerHandler(String oneTextKey, String manyTextKey, boolean countIsTicks) {
        this(
                oneTextKey,
                oneTextKey + ".with_conditions",
                manyTextKey,
                manyTextKey + ".with_conditions",
                countIsTicks
        );
    }

    @Override
    public TriggerDisplayElement prepare(ResearchCriterion<PlayerTrigger.TriggerInstance> criterion) {
        IndentedTextHolder textHolder = new IndentedTextHolder();
        PredicateHelper.optionalTooltip(criterion.conditions().player(), EntityPredicateHelper::tooltip, PLAYER_CONDITIONS_HEADER)
                .ifPresent(textHolder::accept);

        TriggerDisplayElement element;
        if (criterion.count() == 1) {
            element = new TextElement(Component.translatable(textHolder.isEmpty() ? oneTextKey : oneWithConditionsTextKey));
        }
        else {
            element = new TextElement(Component.translatable(textHolder.isEmpty() ? manyTextKey : manyWithConditionsTextKey, criterion.count() * (countIsTicks ? 20 : 1)));
        }

        if (!textHolder.isEmpty()) element = element.withTextTooltip(textHolder.getText());

        return new TriggerDisplay(
                element
        );
    }
}
