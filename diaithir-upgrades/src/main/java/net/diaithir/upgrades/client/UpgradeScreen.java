package net.diaithir.upgrades.client;

import net.diaithir.upgrades.Effects;
import net.diaithir.upgrades.UpgradeMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/** Flat-fill panel like the other machine screens: module slots on top, a readout of what they do, then the inventory. */
public final class UpgradeScreen extends AbstractContainerScreen<UpgradeMenu> {
    private static final int PANEL = 0xFF3A4048, EDGE = 0xFF1B1F24, HI = 0xFF5C646D, SLOT = 0xFF14171A, TEXT = 0xFFD8DEE4, DIM = 0xFF8D969F;

    public UpgradeScreen(UpgradeMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 52 + 18 * ((menu.moduleSlots() - 1) / UpgradeMenu.PER_ROW) + 82);
    }

    private static void box(GuiGraphicsExtractor g, int x, int y, int w, int h, int fill, int edge) {
        g.fill(x, y, x + w, y + h, edge);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractBackground(g, mouseX, mouseY, a);
        box(g, leftPos, topPos, imageWidth, imageHeight, PANEL, EDGE);
        g.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + 2, HI);
        for (Slot s : menu.slots) box(g, leftPos + s.x - 1, topPos + s.y - 1, 18, 18, SLOT, EDGE);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        g.text(font, title, 8, 6, TEXT, false);
        Effects e = menu.effects().clamped();
        int y = 22 + 18 * ((menu.moduleSlots() - 1) / UpgradeMenu.PER_ROW) + 20;
        g.text(font, String.format("speed x%.2f   output x%.2f", e.speedMultiplier(), e.outputMultiplier()), 8, y, DIM, false);
        g.text(font, String.format("power x%.2f   quality %.0f%%", e.consumptionMultiplier(), e.quality() * 100), 8, y + 10, DIM, false);
    }
}
