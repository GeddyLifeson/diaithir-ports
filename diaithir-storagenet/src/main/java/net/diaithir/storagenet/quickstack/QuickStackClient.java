package net.diaithir.storagenet.quickstack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;

/** Plain text buttons beside storage screens (own look, no Terrastorage art). */
public final class QuickStackClient {
    private static final int W = 48, H = 14, GAP = 2;

    private QuickStackClient() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Init.Post.class, QuickStackClient::onInit);
    }

    private static void onInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen) || screen instanceof CreativeModeInventoryScreen) return;
        var mc = screen.getMinecraft();
        if (mc.player == null) return;
        int x = screen.getGuiLeft() + screen.getXSize() + 4;
        int y = screen.getGuiTop();
        int id = screen.getMenu().containerId;
        if (screen instanceof InventoryScreen) {
            x = screen.getGuiLeft() - W - 4;
            add(event, x, y, "Stack Near", id, QuickStackPayload.Action.QUICK_STACK_NEARBY);
            add(event, x, y + H + GAP, "Sort", id, QuickStackPayload.Action.SORT_INVENTORY);
            return;
        }
        if (InventoryUtils.getStorageSlots(screen.getMenu(), mc.player).isEmpty()) return;
        add(event, x, y, "Take All", id, QuickStackPayload.Action.LOOT_ALL);
        add(event, x, y + (H + GAP), "Put All", id, QuickStackPayload.Action.DEPOSIT_ALL);
        add(event, x, y + 2 * (H + GAP), "Quick Stack", id, QuickStackPayload.Action.QUICK_STACK);
        add(event, x, y + 3 * (H + GAP), "Restock", id, QuickStackPayload.Action.RESTOCK);
        add(event, x, y + 4 * (H + GAP), "Sort", id, QuickStackPayload.Action.SORT_STORAGE);
    }

    private static void add(ScreenEvent.Init.Post event, int x, int y, String label, int id, QuickStackPayload.Action action) {
        event.addListener(Button.builder(Component.literal(label), b -> ClientPacketDistributor.sendToServer(new QuickStackPayload(id, action)))
                .bounds(x, y, W, H).build());
    }
}
