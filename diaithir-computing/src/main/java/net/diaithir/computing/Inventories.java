package net.diaithir.computing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

/** Totals of the items in the inventory at a position, read through the item capability. */
final class Inventories {
    record Entry(String name, int count) {}

    static List<Entry> list(Level level, BlockPos pos, Direction face) {
        var handler = level.getCapability(Capabilities.Item.BLOCK, pos, face);
        List<Entry> out = new ArrayList<>();
        if (handler == null) return out;
        java.util.Map<String, Integer> totals = new java.util.LinkedHashMap<>();
        for (int i = 0; i < handler.size(); i++) {
            ItemResource r = handler.getResource(i);
            if (r.isEmpty()) continue;
            totals.merge(BuiltInRegistries.ITEM.getKey(r.getItem()).toString(), (int) handler.getAmountAsLong(i), Integer::sum);
        }
        totals.forEach((k, v) -> out.add(new Entry(k, v)));
        return out;
    }

    /** Puts what fits of the stack into the inventory at pos (as seen from face). Returns how many went in; -1 when there is no inventory. */
    static int insert(Level level, BlockPos pos, Direction face, ItemStack stack) {
        var handler = level.getCapability(Capabilities.Item.BLOCK, pos, face);
        if (handler == null) return -1;
        try (Transaction tx = Transaction.openRoot()) {
            int n = handler.insert(ItemResource.of(stack), stack.getCount(), tx);
            tx.commit();
            return n;
        }
    }

    /** Takes up to max items of the first stack found. Returns EMPTY when there is nothing, null when there is no inventory. */
    static ItemStack extractFirst(Level level, BlockPos pos, Direction face, int max) {
        var handler = level.getCapability(Capabilities.Item.BLOCK, pos, face);
        if (handler == null) return null;
        for (int i = 0; i < handler.size(); i++) {
            ItemResource r = handler.getResource(i);
            if (r.isEmpty()) continue;
            try (Transaction tx = Transaction.openRoot()) {
                int n = handler.extract(i, r, Math.min(max, r.toStack().getMaxStackSize()), tx);
                if (n > 0) { tx.commit(); return r.toStack(n); }
            }
        }
        return ItemStack.EMPTY;
    }

    private Inventories() {}
}
