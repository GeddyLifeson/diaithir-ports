package net.id.paradise_lost.client.screen;

import net.id.paradise_lost.entity.passive.moa.MoaEntity;
import net.id.paradise_lost.screen.handler.MoaScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static net.id.paradise_lost.ModConstants.locate;

public class MoaScreen extends AbstractContainerScreen<MoaScreenHandler> {
    private static final Identifier TEXTURE = locate("textures/gui/container/moa.png");
    private final MoaEntity moa;

    public MoaScreen(MoaScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, 184);
        moa = handler.moa();
        inventoryLabelY += 18;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = (width - imageWidth) >> 1;
        int y = (height - imageHeight) >> 1;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        if (menu.hasMoaInventory()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 79, y + 17, 0, 184, 90, 72, 256, 256);
        }

        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, x + 26, y + 18, x + 78, y + 70, 17, 0.25F, mouseX, mouseY, moa);
    }
}
