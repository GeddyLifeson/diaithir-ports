package io.github.tr100000.researcher.api.trigger.element;

import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.trutils.api.gui.GuiHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ItemElement implements TriggerDisplayElement {
    protected final ItemStack stack;
    protected final boolean showStackTooltip;

    public ItemElement(ItemLike item, boolean showStackTooltip) {
        this(item.asItem().getDefaultInstance(), showStackTooltip);
    }

    public ItemElement(ItemStack stack, boolean showStackTooltip) {
        this.stack = stack;
        this.showStackTooltip = showStackTooltip;
    }

    @Override
    public int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float delta) {
        if (showStackTooltip) {
            GuiHelper.fakeItem(graphics, stack, x, y, mouseX, mouseY);
        }
        else {
            graphics.fakeItem(stack, x, y);
        }

        return 16;
    }

    @Override
    public int width() {
        return 16;
    }
}
