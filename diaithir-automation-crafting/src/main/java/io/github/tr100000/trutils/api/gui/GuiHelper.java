package io.github.tr100000.trutils.api.gui;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.MenuTooltipPositioner;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Contract;
import org.joml.Matrix3x2f;

import java.util.List;

public final class GuiHelper {
    private static final Minecraft client = Minecraft.getInstance();

    private GuiHelper() {}

    @Contract(pure = true)
    public static boolean isMouseTouching(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Contract(pure = true)
    public static boolean isMouseTouching(int x, int y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Contract(pure = true)
    public static boolean isMouseTouching(LayoutElement widget, int mouseX, int mouseY) {
        return isMouseTouching(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), mouseX, mouseY);
    }

    @Contract(pure = true)
    public static boolean isMouseTouching(int x, int y, int width, int height) {
        return isMouseTouching(x, y, width, height, getMouseX(), getMouseY());
    }

    @Contract(pure = true)
    public static int getMouseX() {
        return Mth.floor(client.mouseHandler.xpos() * client.getWindow().getGuiScaledWidth() / client.getWindow().getScreenWidth());
    }

    @Contract(pure = true)
    public static int getMouseY() {
        return Mth.floor(client.mouseHandler.ypos() * client.getWindow().getGuiScaledHeight() / client.getWindow().getScreenHeight());
    }

    public static void slotHighlight(final GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, -2130706433);
    }

    public static void slotHighlight(final GuiGraphicsExtractor graphics, LayoutElement widget) {
        slotHighlight(graphics, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
    }

    public static void itemStack(final GuiGraphicsExtractor graphics, Font textRenderer, ItemStack stack, int x, int y) {
        graphics.fakeItem(stack, x, y);
        graphics.itemDecorations(textRenderer, stack, x, y);
    }

    public static void fakeItem(final GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int mouseX, int mouseY) {
        fakeItem(graphics, stack, x, y, mouseX, mouseY, getItemTooltip(stack));
    }

    public static void fakeItem(final GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int mouseX, int mouseY, List<Component> text) {
        graphics.fakeItem(stack, x, y);
        if (!text.isEmpty() && GuiHelper.isMouseTouching(x, y, 16, 16, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, text, mouseX, mouseY);
        }
    }

    @Contract(pure = true)
    public static List<Component> getItemTooltip(ItemStack stack) {
        return stack.getTooltipLines(Item.TooltipContext.of(client.level), client.player, client.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL);
    }

    // A version of the vanilla implementation that changes the size
    public static void fakeItemScaled(final GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int size) {
        if (!stack.isEmpty()) {
            TrackingItemStackRenderState keyedItemRenderState = new TrackingItemStackRenderState();
            client.getItemModelResolver().updateForTopItem(keyedItemRenderState, stack, ItemDisplayContext.GUI, null, null, 0);

            try {
                graphics.guiRenderState.addItem(
                        new ScaledItemGuiElementRenderState(
                                new Matrix3x2f(graphics.pose()), keyedItemRenderState, x, y, size, graphics.peekScissorStack()
                        )
                );
            } catch (Exception e) {
                CrashReport crashReport = CrashReport.forThrowable(e, "Rendering scaled item (TrUtils)");
                CrashReportCategory crashReportSection = crashReport.addCategory("Item being rendered");
                crashReportSection.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
                crashReportSection.setDetail("Item Components", () -> String.valueOf(stack.getComponents()));
                crashReportSection.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
                throw new ReportedException(crashReport);
            }
        }
    }

    public static void tooltip(final GuiGraphicsExtractor graphics, Font textRenderer, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner tooltipPositioner) {
        graphics.setTooltipForNextFrameInternal(textRenderer, components, x, y, tooltipPositioner, null, false);
    }

    @Contract(pure = true)
    public static boolean isKeyboard() {
        return client.getLastInputType().isKeyboard();
    }

    @Contract(pure = true)
    public static ClientTooltipPositioner widgetPositionerFor(AbstractWidget widget) {
        return !widget.isHovered() && widget.isFocused() && isKeyboard() ? new MenuTooltipPositioner(widget.getRectangle()) : DefaultTooltipPositioner.INSTANCE;
    }
}
