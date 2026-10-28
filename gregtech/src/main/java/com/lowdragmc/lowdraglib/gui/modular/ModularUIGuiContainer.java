package com.lowdragmc.lowdraglib.gui.modular;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.core.mixins.accessor.AbstractContainerScreenAccessor;
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
import net.minecraft.client.gui.components.events.GuiEventListener;
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
        super(new ModularUIContainer(modularUI, windowId), modularUI.entityPlayer.getInventory(), Component.nullToEmpty("modularUI"));
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
    public void init() {  /* ported stub */ }

    
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {  /* ported stub */ }

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

    
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {  /* ported stub */ }

    public void setHoveredSlot(Slot hoveredSlot) {  /* ported stub */ }

    private void renderItemStackOnMouse(GuiGraphics graphics, int mouseX, int mouseY) {
        if (minecraft == null || minecraft.player == null) return;
        ItemStack draggedStack = ((AbstractContainerScreenAccessor)this).getDraggingItem();
        ItemStack itemstack = draggedStack.isEmpty() ? getMenu().getCarried() : draggedStack;
        if (!itemstack.isEmpty()) {
            int k2 = draggedStack.isEmpty() ? 8 : 16;
            String s = null;
            if (!draggedStack.isEmpty() && ((AbstractContainerScreenAccessor)this).isSplittingStack()) {
                itemstack = itemstack.copy();
                itemstack.setCount((int) Math.ceil((float)itemstack.getCount() / 2.0F));
            } else if (this.isQuickCrafting && this.quickCraftSlots.size() > 1) {
                itemstack = itemstack.copy();
                itemstack.setCount(((AbstractContainerScreenAccessor)this).getQuickCraftingRemainder());
                if (itemstack.isEmpty()) {
                    s = ChatFormatting.YELLOW + "0";
                }
            }
            this.renderFloatingItem(graphics, itemstack, mouseX - leftPos - 8, mouseY - topPos - k2, s);
        }

    }

    public void renderFloatingItem(GuiGraphics graphics, ItemStack stack, int x, int y, @Nullable String amountText) {
        graphics.pose().pushPose();
        graphics.pose().translate(0.0f, 0.0f, 232.0f);
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(this.font, stack, x, y - (((AbstractContainerScreenAccessor)this).getDraggingItem().isEmpty() ? 0 : 8), amountText);
        graphics.pose().popPose();
    }

    private void renderReturningItemStack(GuiGraphics graphics) {
        if (!((AbstractContainerScreenAccessor)this).getSnapbackItem().isEmpty()) {
            float f = (float)(Util.getMillis() - ((AbstractContainerScreenAccessor)this).getSnapbackTime()) / 100.0F;
            if (f >= 1.0F) {
                f = 1.0F;
                ((AbstractContainerScreenAccessor)this).setSnapbackItem(ItemStack.EMPTY);
            }

            int l2 = ((AbstractContainerScreenAccessor)this).getSnapbackEnd().x - ((AbstractContainerScreenAccessor)this).getSnapbackStartX();
            int i3 = ((AbstractContainerScreenAccessor)this).getSnapbackEnd().y - ((AbstractContainerScreenAccessor)this).getSnapbackStartY();
            int l1 = ((AbstractContainerScreenAccessor)this).getSnapbackStartX() + (int)((float)l2 * f);
            int i2 = ((AbstractContainerScreenAccessor)this).getSnapbackStartY() + (int)((float)i3 * f);
            this.renderFloatingItem(graphics, ((AbstractContainerScreenAccessor)this).getSnapbackItem(), l1, i2, null);
        }
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

    
    public boolean mouseClicked(double mouseX, double mouseY, int pButton) { return false; /* ported stub */ }

    
    public boolean mouseDragged(double mouseX, double mouseY, int pButton, double pDragX, double pDragY) { return false; /* ported stub */ }

    
    public boolean mouseReleased(double mouseX, double mouseY, int pButton) { return false; /* ported stub */ }

    
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; /* ported stub */ }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double pScrollX, double pScrollY) {
        focused = false;
        if (modularUI.mainGroup.mouseWheelMove(mouseX, mouseY, pScrollX, pScrollY)) return true;
        return this.getChildAt(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, pScrollX, pScrollY)).isPresent();
    }

    
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) { return false; /* ported stub */ }

    
    public boolean charTyped(char codePoint, int modifiers) { return false; /* ported stub */ }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        focused = false;
        modularUI.mainGroup.mouseMoved(mouseX, mouseY);
    }

    public boolean isButtonPressed(int button) {
        return pressedButton == button;
    }

    public void superMouseClicked(double mouseX, double mouseY, int mouseButton) {  /* ported stub */ }

    public void superMouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {  /* ported stub */ }

    public void superMouseReleased(double mouseX, double mouseY, int state) {  /* ported stub */ }

    public boolean superKeyPressed(int keyCode, int scanCode, int modifiers) { return false; /* ported stub */ }

    public boolean superMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public boolean superKeyReleased(int keyCode, int scanCode, int modifiers) { return false; /* ported stub */ }

    public boolean superCharTyped(char codePoint, int modifiers) { return false; /* ported stub */ }

    public void superMouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
    }


    public List<Rect2i> getGuiExtraAreas() {
        return modularUI.mainGroup.getGuiExtraAreas(modularUI.mainGroup.toRectangleBox(), new ArrayList<>());
    }
}
