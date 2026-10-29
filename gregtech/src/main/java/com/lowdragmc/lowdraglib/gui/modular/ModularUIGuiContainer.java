package com.lowdragmc.lowdraglib.gui.modular;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.networking.s2c.SPacketUIWidgetUpdate;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class ModularUIGuiContainer extends AbstractContainerScreen<ModularUIContainer> {

    public final ModularUI modularUI;
    public Widget lastFocus;
    public boolean focused;
    public int dragSplittingLimit;
    public int dragSplittingButton;
    // hover tips
    @Nullable
    public List<Component> tooltipTexts;
    @Nullable
    public TooltipComponent tooltipComponent;
    @Nullable
    public Font tooltipFont;
    @Nullable
    public ItemStack tooltipStack = ItemStack.EMPTY;
    // drag element
    protected Tuple<Object, IGuiTexture> draggingElement;
    protected int pressedButton = -1;

    public ModularUIGuiContainer(ModularUI modularUI, int windowId) {
        super(new ModularUIContainer(modularUI, windowId), modularUI.entityPlayer.getInventory(),
                Component.nullToEmpty("modularUI"), modularUI.getWidth(), modularUI.getHeight());
        this.modularUI = modularUI;
        modularUI.setModularUIGui(this);
    }

    public void setHoverTooltip(List<Component> tooltipTexts, ItemStack tooltipStack, @Nullable Font tooltipFont, @Nullable TooltipComponent tooltipComponent) {
        this.tooltipTexts = tooltipTexts;
        this.tooltipStack = tooltipStack;
        this.tooltipFont = tooltipFont;
        this.tooltipComponent = tooltipComponent;
    }

    public boolean setDraggingElement(Object element, IGuiTexture renderer) {
        if (draggingElement != null) return false;
        draggingElement = new Tuple<>(element, renderer);
        return true;
    }

    @Nullable
    public Object getDraggingElement() {
        if (draggingElement == null) return null;
        return draggingElement.getA();
    }

    @Override
    public void init() {
        super.init();
        this.modularUI.updateScreenSize(width, height);
    }

    @Override
    public void removed() {
        super.removed();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (modularUI.holder.isInvalid() && modularUI.entityPlayer instanceof ServerPlayer serverPlayer) {
            serverPlayer.closeContainer();
        }
        modularUI.mainGroup.updateScreen();
        modularUI.addTick();
    }

    public void handleWidgetUpdate(SPacketUIWidgetUpdate packet) {
        if (packet.windowId == getMenu().containerId) {
            int updateId = packet.updateData.readVarInt();
            modularUI.mainGroup.readUpdateInfo(updateId, packet.updateData);
        }
    }

    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTicks) {
        GuiGraphics graphics = GuiGraphics.of(extractor);
        this.hoveredSlot = null;
        tooltipTexts = null;
        tooltipComponent = null;

        modularUI.mainGroup.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        modularUI.mainGroup.drawInForeground(graphics, mouseX, mouseY, partialTicks);

        if (draggingElement != null) {
            draggingElement.getB().draw(graphics, mouseX, mouseY, mouseX - 20, mouseY - 20, 40, 40);
        }

        extractCarriedItem(extractor, mouseX, mouseY);
        extractSnapbackItem(extractor);

        if (draggingElement == null && tooltipTexts != null && !tooltipTexts.isEmpty()) {
            DrawerHelper.drawTooltip(graphics, mouseX, mouseY, tooltipTexts, tooltipStack, tooltipComponent,
                    tooltipFont == null ? Minecraft.getInstance().font : tooltipFont);
        }
    }

    public void setHoveredSlot(Slot hoveredSlot) {
        // to make sure the widget won't trigger xei slot lookup
        if (modularUI.mainGroup.isClientSideWidget() && !modularUI.mainGroup.isAllowXEIIngredientOverMouse()) {
            return;
        }
        this.hoveredSlot = hoveredSlot;
    }

    public boolean switchFocus(@Nonnull Widget widget) {
        if (focused) return false;
        focused = true;
        if (lastFocus == widget) return false;
        Widget l = lastFocus;
        lastFocus = widget;
        if (l != null) l.setFocus(false);
        return true;
    }

    public Set<Slot> getQuickCraftSlots() {
        return this.quickCraftSlots;
    }

    public boolean getQuickCrafting() {
        return this.isQuickCrafting;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x(), mouseY = event.y();
        int pButton = event.button();
        pressedButton = pButton;
        focused = false;
        if (modularUI.mainGroup.mouseClicked(mouseX, mouseY, pButton)) return true;
        for (GuiEventListener guiEventListener : this.children()) {
            if (!guiEventListener.mouseClicked(event, doubleClick)) continue;
            this.setFocused(guiEventListener);
            if (pButton == 0) {
                this.setDragging(true);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double pDragX, double pDragY) {
        focused = false;
        if (modularUI.mainGroup.mouseDragged(event.x(), event.y(), event.button(), pDragX, pDragY)) return true;
        if (this.getFocused() != null && this.isDragging() && event.button() == 0) {
            return this.getFocused().mouseDragged(event, pDragX, pDragY);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        pressedButton = -1;
        focused = false;
        var result = modularUI.mainGroup.mouseReleased(event.x(), event.y(), event.button());
        draggingElement = null;
        if (result) return true;
        this.setDragging(false);
        return this.getChildAt(event.x(), event.y()).filter(l -> l.mouseReleased(event)).isPresent();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        focused = false;
        if (modularUI.mainGroup.keyPressed(event.key(), event.scancode(), event.modifiers())) {
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double pScrollX, double pScrollY) {
        focused = false;
        if (modularUI.mainGroup.mouseWheelMove(mouseX, mouseY, pScrollX, pScrollY)) return true;
        return this.getChildAt(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, pScrollX, pScrollY)).isPresent();
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        focused = false;
        if (modularUI.mainGroup.keyReleased(event.key(), event.scancode(), event.modifiers())) return true;
        return this.getFocused() != null && this.getFocused().keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        focused = false;
        if (event.codepoint() < 0x10000 && modularUI.mainGroup.charTyped((char) event.codepoint(), 0)) return true;
        return this.getFocused() != null && this.getFocused().charTyped(event);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        focused = false;
        modularUI.mainGroup.mouseMoved(mouseX, mouseY);
    }

    public boolean isButtonPressed(int button) {
        return pressedButton == button;
    }

    private static MouseButtonEvent mouseEvent(double x, double y, int button) {
        return new MouseButtonEvent(x, y, new MouseButtonInfo(button, 0));
    }

    public void superMouseClicked(double mouseX, double mouseY, int mouseButton) {
        try {
            super.mouseClicked(mouseEvent(mouseX, mouseY, mouseButton), false);
        } catch (Exception ignored) { }
    }

    public void superMouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        super.mouseDragged(mouseEvent(pMouseX, pMouseY, pButton), pDragX, pDragY);
    }

    public void superMouseReleased(double mouseX, double mouseY, int state) {
        super.mouseReleased(mouseEvent(mouseX, mouseY, state));
    }

    public boolean superKeyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
    }

    public boolean superMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public boolean superKeyReleased(int keyCode, int scanCode, int modifiers) {
        return super.keyReleased(new KeyEvent(keyCode, scanCode, modifiers));
    }

    public boolean superCharTyped(char codePoint, int modifiers) {
        return super.charTyped(new CharacterEvent(codePoint));
    }

    public void superMouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
    }

    public List<Rect2i> getGuiExtraAreas() {
        return modularUI.mainGroup.getGuiExtraAreas(modularUI.mainGroup.toRectangleBox(), new ArrayList<>());
    }
}
