package net.diaithir.routing;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/** Plain screen for the router menu: drawn panel, framed slots, four filter toggle buttons and a range read-out. */
public final class RouterScreen extends AbstractContainerScreen<RouterMenu> {
    public RouterScreen(RouterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 108 + 58 + 18 + 6);
        this.inventoryLabelY = 98;
    }

    @Override
    protected void init() {
        super.init();
        String[] keys = {"blacklist", "tag", "durability", "data"};
        for (int i = 0; i < keys.length; i++) {
            final int id = i;
            addRenderableWidget(Button.builder(label(id), b -> {
                sendButton(id);
            }).bounds(leftPos + 30 + i * 36, topPos + 47, 34, 14).build());
        }
    }

    private Component label(int id) {
        FilterData f = menu.filter();
        return switch (id) {
            case 0 -> Component.translatable(f.blacklist() ? "gui.diaithirrouting.blacklist" : "gui.diaithirrouting.whitelist");
            case 1 -> Component.translatable("gui.diaithirrouting.match_tag", f.matchTag() ? "+" : "-");
            case 2 -> Component.translatable("gui.diaithirrouting.match_durability", f.matchDurability() ? "+" : "-");
            default -> Component.translatable("gui.diaithirrouting.ignore_data", f.ignoreData() ? "+" : "-");
        };
    }

    private void sendButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        int i = 0;
        for (var w : children()) {
            if (w instanceof Button b && i < 4) b.setMessage(label(i++));
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float partial) {
        super.extractBackground(g, mx, my, partial);
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF3A3A3A);
        g.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + imageHeight - 2, 0xFF8B8B8B);
        for (Slot slot : menu.slots) {
            int x = leftPos + slot.x - 1;
            int y = topPos + slot.y - 1;
            g.fill(x, y, x + 18, y + 18, 0xFF373737);
            g.fill(x + 1, y + 1, x + 17, y + 17, 0xFF5E5E5E);
        }
        g.text(font, Component.translatable("gui.diaithirrouting.range", menu.rangeValue()), leftPos + 30, topPos + 37, 0xFF202020, false);
    }
}
