package net.diaithir.inventoryui.inventoryessentials.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.diaithir.inventoryui.inventoryessentials.config.ClientConfig;
import net.diaithir.inventoryui.inventoryessentials.network.SortRequest;
import net.diaithir.inventoryui.inventoryessentials.sort.ContainerSorter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {}

    private static boolean valid(Object screen) {
        return screen instanceof AbstractContainerScreen<?> && !(screen instanceof CreativeModeInventoryScreen);
    }

    private static boolean down(net.minecraft.client.KeyMapping k) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), k.getKey().getValue());
    }

    private static boolean shiftDown() {
        return down(Minecraft.getInstance().options.keyShift);
    }

    private static Slot hovered(AbstractContainerScreen<?> s) {
        return s.getSlotUnderMouse();
    }

    private static void sendSort(AbstractContainerScreen<?> screen) {
        ContainerSorter.categoryOrder = ClientConfig.SORT_CATEGORY_ORDER.get();
        ClientPacketDistributor.sendToServer(new SortRequest(screen.getMenu().containerId, SlotLocks.snapshot()));
    }

    @SubscribeEvent
    public static void onInit(ScreenEvent.Init.Post e) {
        if (!(e.getScreen() instanceof AbstractContainerScreen<?> screen) || !valid(screen)) return;
        if (!ClientConfig.ENABLE_SORT_BUTTON.get()) return;
        int w = 12, h = 12;
        int left = screen.getGuiLeft(), top = screen.getGuiTop(), iw = screen.getXSize(), ih = screen.getYSize();
        int x, y;
        switch (ClientConfig.BUTTON_ANCHOR.get()) {
            case TOP_LEFT -> { x = left - w - 2; y = top + 2; }
            case BOTTOM_LEFT -> { x = left - w - 2; y = top + ih - h - 2; }
            case BOTTOM_RIGHT -> { x = left + iw + 2; y = top + ih - h - 2; }
            default -> { x = left + iw + 2; y = top + 2; }
        }
        Button b = Button.builder(Component.literal("S"), btn -> sendSort(screen)).bounds(x, y, w, h).build();
        if (ClientConfig.SHOW_TOOLTIP_HINTS.get()) {
            b.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("tooltip.inventoryui.sort")));
        }
        e.addListener(b);
    }

    @SubscribeEvent
    public static void onKey(ScreenEvent.KeyPressed.Pre e) {
        if (!(e.getScreen() instanceof AbstractContainerScreen<?> screen) || !valid(screen)) return;
        var key = InputConstants.getKey(e.getKeyEvent());
        if (ClientSetup.SORT.isActiveAndMatches(key)) {
            sendSort(screen);
            e.setCanceled(true);
        } else if (ClientSetup.LOCK.isActiveAndMatches(key)) {
            Slot s = hovered(screen);
            if (s != null && s.container instanceof Inventory && s.getContainerSlot() < 36) {
                SlotLocks.toggle(s.getContainerSlot());
                e.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPress(ScreenEvent.MouseButtonPressed.Pre e) {
        if (!(e.getScreen() instanceof AbstractContainerScreen<?> screen) || !valid(screen)) return;
        Slot s = hovered(screen);
        if (s == null) return;
        boolean locked = s.container instanceof Inventory && SlotLocks.isLocked(s.getContainerSlot());
        if (e.getButton() == 0 && ClientConfig.DRAG_MOVE_ENABLED.get() && down(ClientSetup.DRAG)) {
            DragMoveHandler.begin();
            DragMoveHandler.visit(screen, s);
            e.setCanceled(true);
            return;
        }
        if (e.getButton() == 0 && down(ClientSetup.MOVE_ALL) && s.hasItem() && !locked) {
            moveAllMatching(screen, s);
            e.setCanceled(true);
            return;
        }
        if (locked && screen.getMenu().getCarried().isEmpty()) {
            e.setCanceled(true);
        }
    }

    private static void moveAllMatching(AbstractContainerScreen<?> screen, Slot origin) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null || mc.player == null) return;
        var ref = origin.getItem().copy();
        boolean fromPlayer = origin.container instanceof Inventory;
        for (Slot s : screen.getMenu().slots) {
            if (!s.hasItem() || (s.container instanceof Inventory) != fromPlayer) continue;
            if (s.container instanceof Inventory && SlotLocks.isLocked(s.getContainerSlot())) continue;
            if (net.minecraft.world.item.ItemStack.isSameItemSameComponents(ref, s.getItem())) {
                mc.gameMode.handleContainerInput(screen.getMenu().containerId, s.index, 0, ContainerInput.QUICK_MOVE, mc.player);
            }
        }
    }

    @SubscribeEvent
    public static void onDrag(ScreenEvent.MouseDragged.Pre e) {
        if (!DragMoveHandler.isActive() || !(e.getScreen() instanceof AbstractContainerScreen<?> screen)) return;
        DragMoveHandler.visit(screen, hovered(screen));
        e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRelease(ScreenEvent.MouseButtonReleased.Pre e) {
        if (!DragMoveHandler.isActive()) return;
        DragMoveHandler.end();
        e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onScroll(ScreenEvent.MouseScrolled.Pre e) {
        if (!ClientConfig.SHIFT_SCROLL_MOVE.get()) return;
        if (!(e.getScreen() instanceof AbstractContainerScreen<?> screen) || !valid(screen)) return;
        if (!shiftDown()) return;
        Slot s = hovered(screen);
        Minecraft mc = Minecraft.getInstance();
        if (s == null || !s.hasItem() || mc.gameMode == null || mc.player == null) return;
        if (s.container instanceof Inventory && SlotLocks.isLocked(s.getContainerSlot())) return;
        if (!screen.getMenu().getCarried().isEmpty()) return;
        int id = screen.getMenu().containerId;
        int count = s.getItem().getCount();
        if (count <= 0) return;
        // pick up all, put back all but one, quick-move the remainder is not possible; move one via carried stack
        mc.gameMode.handleContainerInput(id, s.index, 1, ContainerInput.PICKUP, mc.player);
        var carried = screen.getMenu().getCarried();
        if (carried.isEmpty()) return;
        int back = -1;
        for (Slot t : screen.getMenu().slots) {
            if (t == s || (t.container instanceof Inventory) == (s.container instanceof Inventory)) continue;
            if (t.mayPlace(carried) && (!t.hasItem() || net.minecraft.world.item.ItemStack.isSameItemSameComponents(t.getItem(), carried))) {
                back = t.index;
                break;
            }
        }
        if (back >= 0) {
            mc.gameMode.handleContainerInput(id, back, 1, ContainerInput.PICKUP, mc.player);
        }
        if (!screen.getMenu().getCarried().isEmpty()) {
            mc.gameMode.handleContainerInput(id, s.index, 0, ContainerInput.PICKUP, mc.player);
        }
        e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRender(ScreenEvent.Render.Post e) {
        if (!(e.getScreen() instanceof AbstractContainerScreen<?> screen) || !valid(screen)) return;
        var g = e.getGuiGraphics();
        int left = screen.getGuiLeft(), top = screen.getGuiTop();
        for (Slot s : screen.getMenu().slots) {
            int x = left + s.x, y = top + s.y;
            if (ClientConfig.LOCKED_SLOT_OVERLAY.get() && s.container instanceof Inventory && SlotLocks.isLocked(s.getContainerSlot())) {
                g.fill(x, y, x + 16, y + 16, 0x80AA0000);
            } else if (DragMoveHandler.isActive() && DragMoveHandler.isVisited(s.index)) {
                g.fill(x, y, x + 16, y + 16, 0x6000AAFF);
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
        SlotLocks.clear();
        DragMoveHandler.end();
    }
}
