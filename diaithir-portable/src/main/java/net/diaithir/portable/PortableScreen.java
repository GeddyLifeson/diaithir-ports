package net.diaithir.portable;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Plain screen for the shared menu: background panel, a sort button and a lock button. */
public final class PortableScreen extends AbstractContainerScreen<PortableMenu> {
    public PortableScreen(PortableMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 8 * 2 + menu.tier().columns * 18, 18 + menu.tier().rows() * 18 + 6 + 30 + 82);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("gui.diaithirportable.sort"), b -> sendButton(PortableMenu.SORT))
                .bounds(leftPos + imageWidth - 50, topPos + 4, 20, 12).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.diaithirportable.lock"), b -> sendButton(PortableMenu.LOCK))
                .bounds(leftPos + imageWidth - 26, topPos + 4, 20, 12).build());
    }

    private void sendButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float partial) {
        super.extractBackground(g, mx, my, partial);
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF3A3A3A);
        g.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + imageHeight - 2, 0xFF8B8B8B);
        for (net.minecraft.world.inventory.Slot slot : menu.slots) {
            int x = leftPos + slot.x - 1;
            int y = topPos + slot.y - 1;
            g.fill(x, y, x + 18, y + 18, 0xFF373737);
            g.fill(x + 1, y + 1, x + 17, y + 17, 0xFF5E5E5E);
        }
    }
}
