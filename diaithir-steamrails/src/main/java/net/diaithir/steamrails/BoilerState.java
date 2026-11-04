package net.diaithir.steamrails;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * The live numbers of one boiler: water, fire, pressure. It knows nothing about blocks, so the block entity (a boiler standing in the world) and
 * the movement behaviour (a boiler riding a train) share it. Pressure is bar. Steam is kept as pressure times volume, so a big boiler heats
 * slower and holds its pressure better under load.
 */
public final class BoilerState {
    public static final int FUEL_SLOTS = 3;
    public static final int MAX_FUEL_ITEMS = 24;

    public static final Codec<BoilerState> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.optionalFieldOf("water", 0F).forGetter(s -> s.water),
            Codec.FLOAT.optionalFieldOf("pressure", 0F).forGetter(s -> s.pressure),
            Codec.INT.optionalFieldOf("burn_left", 0).forGetter(s -> s.burnLeft),
            Codec.INT.optionalFieldOf("burn_total", 0).forGetter(s -> s.burnTotal),
            ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("fuel", List.of()).forGetter(s -> List.of(s.fuel)))
            .apply(i, (w, p, bl, bt, f) -> {
                BoilerState s = new BoilerState();
                s.water = w;
                s.pressure = p;
                s.burnLeft = bl;
                s.burnTotal = bt;
                for (int k = 0; k < FUEL_SLOTS && k < f.size(); k++) s.fuel[k] = f.get(k);
                return s;
            }));

    public float water;
    public float pressure;
    public int burnLeft;
    public int burnTotal;
    public final ItemStack[] fuel = {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    /** Not saved: the safety valve is open this tick. */
    public boolean venting;
    /** Not saved: steam produced and steam taken last tick, for gauges and tests. */
    public float produced;
    public float drawn;

    public int fuelCount() {
        int n = 0;
        for (ItemStack s : fuel) n += s.getCount();
        return n;
    }

    public boolean burning() {
        return burnLeft > 0;
    }

    /** Adds as much of the stack as fits; returns the part that did not fit. */
    public ItemStack addFuel(ItemStack in, ToIntFunction<ItemStack> burnTime) {
        if (in.isEmpty() || burnTime.applyAsInt(in) <= 0) return in;
        ItemStack rest = in.copy();
        for (int k = 0; k < FUEL_SLOTS && !rest.isEmpty(); k++) {
            ItemStack slot = fuel[k];
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, rest)) {
                int room = Math.min(slot.getMaxStackSize(), MAX_FUEL_ITEMS / FUEL_SLOTS) - slot.getCount();
                int n = Math.min(room, rest.getCount());
                if (n > 0) {
                    slot.grow(n);
                    rest.shrink(n);
                }
            }
        }
        for (int k = 0; k < FUEL_SLOTS && !rest.isEmpty(); k++) {
            if (fuel[k].isEmpty()) {
                int n = Math.min(rest.getCount(), Math.min(rest.getMaxStackSize(), MAX_FUEL_ITEMS / FUEL_SLOTS));
                fuel[k] = rest.copyWithCount(n);
                rest.shrink(n);
            }
        }
        return rest;
    }

    /** Adds water in millibuckets; returns how much fitted. */
    public int addWater(int mb, int capacity) {
        int n = Math.max(0, Math.min(mb, capacity - (int) water));
        water += n;
        return n;
    }

    /**
     * One tick. {@code load} is steam taken from the boiler this tick in bar times volume units (cylinders, whistle). The fire is lit
     * from the fuel slots when the boiler wants heat. Returns nothing; read {@link #pressure}, {@link #venting}, {@link #produced}.
     */
    public void tick(Layout l, ToIntFunction<ItemStack> burnTime, float load) {
        venting = false;
        float size = Math.max(1, l.size);
        float maxP = l.rating;
        boolean wantsHeat = pressure < maxP - 0.15F && water > 0;
        if (burnLeft <= 0 && wantsHeat) {
            for (int k = 0; k < FUEL_SLOTS; k++) {
                ItemStack s = fuel[k];
                int t = s.isEmpty() ? 0 : burnTime.applyAsInt(s);
                if (t > 0) {
                    burnLeft = burnTotal = t;
                    s.shrink(1);
                    if (s.isEmpty()) fuel[k] = ItemStack.EMPTY;
                    break;
                }
            }
        }
        float steamNow = pressure * size;
        produced = 0;
        if (burnLeft > 0) {
            burnLeft--;
            if (wantsHeat) {
                float q = SteamRails.FIRE_STEAM * (float) Math.sqrt(size) * (l.hasStack() ? SteamRails.DRAFT_BONUS : 1F);
                float waterNeeded = q * SteamRails.WATER_PER_STEAM;
                if (water < waterNeeded) {
                    q *= water / waterNeeded;
                    waterNeeded = water;
                }
                water -= waterNeeded;
                steamNow += q;
                produced = q;
            }
        }
        // radiation loss, always
        steamNow -= steamNow * SteamRails.LEAK;
        drawn = Math.min(load, steamNow);
        steamNow -= drawn;
        if (water <= 0 && produced <= 0) steamNow -= steamNow * 0.01F;
        pressure = Math.max(0F, steamNow / size);
        if (pressure > maxP) {
            // safety valve: extra steam is lost with a little water
            venting = true;
            pressure = maxP;
            water = Math.max(0F, water - 1.5F);
        }
        if (burnLeft <= 0) burnTotal = 0;
    }

    public int comparator(int rating) {
        return Math.min(15, Math.round(15F * pressure / Math.max(1, rating)));
    }

    public List<ItemStack> fuelStacks() {
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack s : fuel) if (!s.isEmpty()) out.add(s);
        return out;
    }
}
