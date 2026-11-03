package net.diaithir.computing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemResource;

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

    private Inventories() {}
}
