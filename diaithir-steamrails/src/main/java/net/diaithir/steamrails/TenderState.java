package net.diaithir.steamrails;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.ToIntFunction;

/** Water and fuel carried by a tender. Shared by the block entity and the moving carriage the same way as {@link BoilerState}. */
public final class TenderState {
    public static final int SLOTS = 9;
    public static final int WATER_CAPACITY = 16000;

    public static final Codec<TenderState> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.optionalFieldOf("water", 0F).forGetter(s -> s.water),
            ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("fuel", List.of()).forGetter(s -> List.of(s.fuel)))
            .apply(i, (w, f) -> {
                TenderState s = new TenderState();
                s.water = w;
                for (int k = 0; k < SLOTS && k < f.size(); k++) s.fuel[k] = f.get(k);
                return s;
            }));

    public float water;
    public final ItemStack[] fuel = new ItemStack[SLOTS];

    public TenderState() {
        java.util.Arrays.fill(fuel, ItemStack.EMPTY);
    }

    public int fuelCount() {
        int n = 0;
        for (ItemStack s : fuel) n += s.getCount();
        return n;
    }

    public ItemStack addFuel(ItemStack in, ToIntFunction<ItemStack> burnTime) {
        if (in.isEmpty() || burnTime.applyAsInt(in) <= 0) return in;
        ItemStack rest = in.copy();
        for (int k = 0; k < SLOTS && !rest.isEmpty(); k++) {
            ItemStack slot = fuel[k];
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, rest)) {
                int n = Math.min(slot.getMaxStackSize() - slot.getCount(), rest.getCount());
                if (n > 0) {
                    slot.grow(n);
                    rest.shrink(n);
                }
            }
        }
        for (int k = 0; k < SLOTS && !rest.isEmpty(); k++) {
            if (fuel[k].isEmpty()) {
                int n = Math.min(rest.getCount(), rest.getMaxStackSize());
                fuel[k] = rest.copyWithCount(n);
                rest.shrink(n);
            }
        }
        return rest;
    }

    public int addWater(int mb) {
        int n = Math.max(0, Math.min(mb, WATER_CAPACITY - (int) water));
        water += n;
        return n;
    }

    /** Hands a boiler what it lacks: fuel to a few items and water to near full. Returns true when anything moved. */
    public boolean feed(BoilerState b, Layout l, ToIntFunction<ItemStack> burnTime) {
        boolean moved = false;
        int cap = l.waterCapacity();
        float need = cap - b.water;
        if (need >= 100 && water >= 1) {
            float n = Math.min(Math.min(need, water), 600);
            b.water += n;
            water -= n;
            moved = true;
        }
        if (b.fuelCount() < 8) {
            for (int k = 0; k < SLOTS; k++) {
                ItemStack s = fuel[k];
                if (s.isEmpty() || burnTime.applyAsInt(s) <= 0) continue;
                ItemStack one = s.copyWithCount(Math.min(s.getCount(), 8));
                ItemStack rest = b.addFuel(one, burnTime);
                int took = one.getCount() - rest.getCount();
                if (took > 0) {
                    s.shrink(took);
                    if (s.isEmpty()) fuel[k] = ItemStack.EMPTY;
                    moved = true;
                    break;
                }
            }
        }
        return moved;
    }
}
