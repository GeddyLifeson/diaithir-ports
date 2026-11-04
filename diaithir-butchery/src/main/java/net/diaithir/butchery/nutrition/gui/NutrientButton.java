package net.diaithir.butchery.nutrition.gui;

import net.diaithir.butchery.nutrition.Config;
import net.diaithir.butchery.nutrition.NutritionalBalance;
import net.diaithir.butchery.nutrition.network.GUITrigger;
import net.diaithir.butchery.nutrition.network.ModNetworkHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class NutrientButton extends AbstractWidget {
    private Identifier buttonGUI = Identifier.fromNamespaceAndPath(NutritionalBalance.MODID, "textures/gui/nutrient_button.png");
    private Identifier buttonGUIHover = Identifier.fromNamespaceAndPath(NutritionalBalance.MODID, "textures/gui/nutrient_button_hover.png");
    private InventoryScreen gui;

    public NutrientButton(InventoryScreen gui, Component title) {
        super(gui.getGuiLeft(), gui.getGuiTop(), 0, 0, title);
        this.gui = gui;
        updateLocation();
    }

    private void updateLocation() {
        this.setX(gui.getGuiLeft() + Config.NUTRIENT_BUTTON_X.get());
        this.setY(gui.getGuiTop() + Config.NUTRIENT_BUTTON_Y.get());
        this.width = 20;
        this.height = 18;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        ModNetworkHandler.sendToServer(new GUITrigger());
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (visible) {
            updateLocation();
            Identifier texture = (mouseX > getX() && mouseX < getX() + width && mouseY > getY() && mouseY < getY() + height)
                    ? buttonGUIHover : buttonGUI;
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, getX(), getY(), 0, 0, width, height, 256, 256);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) { }
}
