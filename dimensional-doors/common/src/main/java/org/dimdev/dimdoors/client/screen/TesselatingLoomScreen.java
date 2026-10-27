package org.dimdev.dimdoors.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.dimdev.dimdoors.DimensionalDoors;
import org.dimdev.dimdoors.screen.TessellatingContainer;

public class TesselatingLoomScreen extends AbstractContainerScreen<TessellatingContainer> {
    private static final Identifier TEXTURE = DimensionalDoors.id("textures/screen/container/tesselating_loom.png");

    public TesselatingLoomScreen(TessellatingContainer handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int i = this.leftPos;
        int j = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        if (this.menu.isWeaving()) {
            int k = this.menu.getWeavProgress(22);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 89, j + 34, 176.0F, 0.0F, k + 1, 16, 256, 256);
        }
    }
}
