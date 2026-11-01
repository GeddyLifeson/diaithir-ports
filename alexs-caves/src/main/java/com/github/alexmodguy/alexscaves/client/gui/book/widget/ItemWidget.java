package com.github.alexmodguy.alexscaves.client.gui.book.widget;

import com.google.gson.annotations.Expose;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class ItemWidget extends BookWidget {

    @Expose
    private String item;
    @Expose
    private String nbt;
    @Expose
    private boolean sepia;

    private transient ItemStack actualItem = null;

    public ItemWidget(int displayPage, String item, String nbt, boolean sepia, int x, int y, float scale) {
        super(displayPage, Type.ITEM, x, y, scale);
        this.item = item;
        this.nbt = nbt;
        this.sepia = sepia;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, PageContext ctx, float partialTicks) {
        if (actualItem == null) {
            actualItem = item == null ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(item)));
        }
        renderItem(graphics, actualItem, getX(), getY(), getScale());
    }

    /** Draws the stack centred on (x, y) at 16 * scale page units. */
    public static void renderItem(GuiGraphicsExtractor graphics, ItemStack stack, float x, float y, float scale) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(scale, scale);
        graphics.item(stack, -8, -8);
        pose.popMatrix();
    }
}
