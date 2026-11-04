package net.diaithir.inventoryui.inventoryessentials.client;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

public final class DragMoveHandler {
    private static boolean active;
    private static final IntOpenHashSet VISITED = new IntOpenHashSet();

    private DragMoveHandler() {}

    public static boolean isActive() {
        return active;
    }

    public static boolean isVisited(int slotIndex) {
        return VISITED.contains(slotIndex);
    }

    public static void begin() {
        active = true;
        VISITED.clear();
    }

    public static void end() {
        active = false;
        VISITED.clear();
    }

    public static void visit(AbstractContainerScreen<?> screen, Slot slot) {
        if (slot == null || !VISITED.add(slot.index)) return;
        if (!slot.hasItem()) return;
        if (slot.container instanceof Inventory inv && SlotLocks.isLocked(slot.getContainerSlot())) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null || mc.player == null) return;
        mc.gameMode.handleContainerInput(screen.getMenu().containerId, slot.index, 0, ContainerInput.QUICK_MOVE, mc.player);
    }
}
