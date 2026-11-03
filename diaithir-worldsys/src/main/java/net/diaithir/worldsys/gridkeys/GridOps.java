package net.diaithir.worldsys.gridkeys;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Server-side grid manipulation on the slots of the player's open crafting menu. */
final class GridOps {
    private GridOps() {}

    static void run(ServerPlayer player, GridAction action) {
        AbstractContainerMenu menu = player.containerMenu;
        List<Slot> grid = new ArrayList<>();
        for (Slot s : menu.slots) {
            if (s.container instanceof net.minecraft.world.inventory.CraftingContainer) grid.add(s);
        }
        if (grid.isEmpty()) return;
        switch (action) {
            case ROTATE -> rotate(grid);
            case BALANCE -> balance(grid);
            case CLEAR -> clear(player, grid);
        }
        menu.broadcastChanges();
    }

    private static void rotate(List<Slot> grid) {
        int n = (int) Math.round(Math.sqrt(grid.size()));
        if (n * n != grid.size()) return;
        ItemStack[] old = new ItemStack[grid.size()];
        for (int i = 0; i < old.length; i++) old[i] = grid.get(i).getItem().copy();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                grid.get(c * n + (n - 1 - r)).set(old[r * n + c]);
            }
        }
    }

    private static void balance(List<Slot> grid) {
        List<Slot> done = new ArrayList<>();
        for (Slot first : grid) {
            ItemStack proto = first.getItem();
            if (proto.isEmpty() || done.contains(first)) continue;
            List<Slot> group = new ArrayList<>();
            int total = 0;
            for (Slot s : grid) {
                if (ItemStack.isSameItemSameComponents(proto, s.getItem())) {
                    group.add(s);
                    total += s.getItem().getCount();
                }
            }
            done.addAll(group);
            int max = proto.getMaxStackSize();
            int base = total / group.size();
            int extra = total % group.size();
            for (int i = 0; i < group.size(); i++) {
                int count = Math.min(max, base + (i < extra ? 1 : 0));
                ItemStack copy = proto.copyWithCount(count);
                group.get(i).set(copy);
            }
        }
    }

    private static void clear(ServerPlayer player, List<Slot> grid) {
        for (Slot s : grid) {
            ItemStack stack = s.getItem();
            if (stack.isEmpty()) continue;
            s.set(ItemStack.EMPTY);
            if (!player.getInventory().add(stack)) player.drop(stack, false);
        }
    }
}
