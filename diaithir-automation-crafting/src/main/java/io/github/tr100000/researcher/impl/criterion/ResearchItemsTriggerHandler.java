package io.github.tr100000.researcher.impl.criterion;

import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.TriggerDisplay;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.TriggerHandler;
import io.github.tr100000.researcher.api.trigger.element.ItemElement;
import io.github.tr100000.researcher.api.trigger.element.SpacingElement;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import io.github.tr100000.researcher.criterion.ResearchItemsTrigger;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ResearchItemsTriggerHandler implements TriggerHandler<ResearchItemsTrigger.TriggerInstance> {
    @Override
    public TriggerDisplayElement prepare(ResearchCriterion<ResearchItemsTrigger.TriggerInstance> criterion) {
        List<TriggerDisplayElement> elements = new ObjectArrayList<>();

        ResearchItemsTrigger.TriggerInstance triggerInstance = criterion.conditions();
        for (int i = 0; i < triggerInstance.items().size(); i++) {
            ItemStack stack = triggerInstance.items().get(i).getDefaultInstance();
            elements.add(new ItemElement(stack, true));
            elements.add(new SpacingElement(2));
        }

        Component text = getText(criterion);
        elements.add(new TextElement(text));
        elements.add(new SpacingElement(2));

        return new TriggerDisplay(elements);
    }

    private Component getText(ResearchCriterion<ResearchItemsTrigger.TriggerInstance> criterion) {
        return Component.literal(String.format("%ds × %d", criterion.conditions().time(), criterion.count()));
    }
}
