package net.diaithir.wiretech.circuit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/**
 * Reads the items riding a belt from the saved data of its chute block entity (the "moving" list: stack, lane, progress), so the circuit reader can
 * measure belts without depending on the belt mod's classes. Gives each item by id, si:belt_items, and si:belt_lane0 and si:belt_lane1.
 */
final class BeltReading {
    private BeltReading() {}

    static void read(CompoundTag be, Signals r) {
        ListTag moving = be.getList("moving").orElse(null);
        if (moving == null) return;
        long total = 0;
        long[] lane = new long[2];
        for (Tag t : moving) {
            if (!(t instanceof CompoundTag item)) continue;
            CompoundTag stack = item.getCompound("stack").orElse(null);
            if (stack == null) continue;
            String id = stack.getString("id").orElse("");
            int count = stack.getInt("count").orElse(1);
            if (id.isEmpty() || count <= 0) continue;
            r.add(id, count);
            total += count;
            int l = Math.max(0, Math.min(1, item.getByte("lane").orElse((byte) 0)));
            lane[l] += count;
        }
        r.set("si:belt_items", total);
        r.set("si:belt_lane0", lane[0]);
        r.set("si:belt_lane1", lane[1]);
    }

    /** For the self test: a belt save with the given items on the given lanes. */
    static CompoundTag sample(String[] ids, int[] counts, byte[] lanes) {
        CompoundTag be = new CompoundTag();
        ListTag list = new ListTag();
        for (int i = 0; i < ids.length; i++) {
            CompoundTag item = new CompoundTag();
            CompoundTag stack = new CompoundTag();
            stack.putString("id", ids[i]);
            stack.putInt("count", counts[i]);
            item.put("stack", stack);
            item.putByte("lane", lanes[i]);
            item.putFloat("progress", 0.5F);
            list.add(item);
        }
        be.put("moving", list);
        return be;
    }
}
