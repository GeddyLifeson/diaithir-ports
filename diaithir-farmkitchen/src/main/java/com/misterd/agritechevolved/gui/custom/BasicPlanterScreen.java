package com.misterd.agritechevolved.gui.custom;

import com.misterd.agritechevolved.compat.jei.ATJeiPlugin;
import com.misterd.agritechevolved.compat.jei.PlanterRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class BasicPlanterScreen extends AbstractContainerScreen<BasicPlanterMenu> {

    private static final Identifier GUI_TEXTURE =
            Identifier.fromNamespaceAndPath("agritechevolved", "textures/gui/basic_planter_gui.png");
    private static final int GUI_HEIGHT = 172;

    public BasicPlanterScreen(BasicPlanterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, GUI_HEIGHT);
        this.inventoryLabelY = GUI_HEIGHT - 94;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE,
                this.leftPos, this.topPos, 0.0F, 0.0F,
                this.imageWidth, this.imageHeight, 256, 256);

        float growthProgress = this.menu.getGrowthProgress() / 100.0F;
        if (growthProgress > 0.0F) {
            int barHeight = (int) (54.0F * growthProgress);
            int barY = this.topPos + 17 + 54 - barHeight;
            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE,
                    this.leftPos + 40, barY,
                    176.0F, (float) (54 - barHeight),
                    6, barHeight, 256, 256);
        }
        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (mouseX >= this.leftPos + 40 && mouseX <= this.leftPos + 46
                && mouseY >= this.topPos + 18 && mouseY <= this.topPos + 71) {
            float progress = this.menu.getGrowthProgress() / 100.0F;
            graphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("tooltip.agritechevolved.growth_progress"),
                    Component.literal(String.format("%.1f%%", progress * 100.0F))
                            .withStyle(ChatFormatting.GREEN),
                    Component.translatable("tooltip.agritechevolved.view_recipes")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            ), mouseX, mouseY);
            return;
        }
        if (isHovering(8, 18, 16, 16, mouseX, mouseY) && menu.slots.get(36).getItem().isEmpty()) {
            graphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("tooltip.agritechevolved.slot.plant")
            ), mouseX, mouseY);
            return;
        }
        if (isHovering(8, 54, 16, 16, mouseX, mouseY) && menu.slots.get(37).getItem().isEmpty()) {
            graphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("tooltip.agritechevolved.slot.soil")
            ), mouseX, mouseY);
            return;
        }
        if (isHovering(152, 18, 16, 16, mouseX, mouseY) && menu.slots.get(38).getItem().isEmpty()) {
            graphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("tooltip.agritechevolved.slot.fertilizer")
            ), mouseX, mouseY);
            return;
        }
        super.extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            double mx = event.x(), my = event.y();
            if (mx >= this.leftPos + 40 && mx <= this.leftPos + 46
                    && my >= this.topPos + 18 && my <= this.topPos + 71) {
                IJeiRuntime runtime = ATJeiPlugin.getJeiRuntime();
                if (runtime != null && minecraft != null && minecraft.player != null) {
                    runtime.getRecipesGui().showTypes(List.of(PlanterRecipeCategory.PLANTER_RECIPE_TYPE));
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}