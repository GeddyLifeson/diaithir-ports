package io.github.tr100000.trutils.api.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ItemIconRenderer implements IconRenderer<ItemIcon> {
    public static final ItemIconRenderer INSTANCE = new ItemIconRenderer();

    private ItemIconRenderer() {}

    @Override
    public void draw(ItemIcon icon, GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fakeItem(icon.template().create(), x, y);
    }

    @Override
    public void drawWithSize(ItemIcon icon, GuiGraphicsExtractor graphics, int x, int y, int size) {
        GuiHelper.fakeItemScaled(graphics, icon.template().create(), x, y, size);
    }
}
