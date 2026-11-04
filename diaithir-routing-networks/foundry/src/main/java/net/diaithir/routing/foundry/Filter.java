package net.diaithir.routing.foundry;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Eight ghost entries read as four pairs (0+1, 2+3, 4+5, 6+7). An item passes a pair when it is the entry of a half-empty pair,
 * either entry of a pair from different mods, or from the same mod as a pair of two items of one mod (the whole mod).
 * New entries fill the first slot of each pair before any second slot, so the first four items stay exact.
 * Idea ported from the Factorization sieve (MIT, neptunepink); this is a rewrite.
 */
public final class Filter {
    public static final int SLOTS = 8;
    private static final Codec<List<ItemResource>> CODEC = ItemResource.OPTIONAL_CODEC.listOf();

    private final ItemResource[] e = new ItemResource[SLOTS];

    public Filter() {
        Arrays.fill(e, ItemResource.EMPTY);
    }

    public boolean isEmpty() {
        for (ItemResource r : e) if (!r.isEmpty()) return false;
        return true;
    }

    /** True when the item matches any pair. An empty filter matches nothing; callers decide what empty means. */
    public boolean matches(ItemResource s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < SLOTS; i += 2) if (pair(e[i], e[i + 1], s)) return true;
        return false;
    }

    private static boolean exact(ItemResource a, ItemResource s) {
        if (!a.is(s.getItem())) return false;
        return a.isComponentsPatchEmpty() || a.equals(s);
    }

    private static String mod(ItemResource r) {
        return BuiltInRegistries.ITEM.getKey(r.getItem()).getNamespace();
    }

    private static boolean pair(ItemResource a, ItemResource b, ItemResource s) {
        if (a.isEmpty() && b.isEmpty()) return false;
        if (a.isEmpty()) return exact(b, s);
        if (b.isEmpty()) return exact(a, s);
        if (a.is(b.getItem())) return exact(a, s) || exact(b, s);
        if (mod(a).equals(mod(b))) return mod(s).equals(mod(a));
        return exact(a, s) || exact(b, s);
    }

    /** Adds an entry. False when full or already present. */
    public boolean add(ItemResource r) {
        if (r.isEmpty() || Arrays.asList(e).contains(r)) return false;
        for (int i = 0; i < SLOTS; i += 2) if (e[i].isEmpty()) return put(i, r);
        for (int i = 1; i < SLOTS; i += 2) if (e[i].isEmpty()) return put(i, r);
        return false;
    }

    private boolean put(int i, ItemResource r) {
        e[i] = r;
        return true;
    }

    public void clear() {
        Arrays.fill(e, ItemResource.EMPTY);
    }

    public String describe() {
        List<String> names = new ArrayList<>();
        for (ItemResource r : e) if (!r.isEmpty()) names.add(BuiltInRegistries.ITEM.getKey(r.getItem()).getPath());
        return names.isEmpty() ? "empty" : String.join(", ", names);
    }

    public void save(ValueOutput out, String key) {
        out.store(key, CODEC, Arrays.asList(e));
    }

    public void load(ValueInput in, String key) {
        clear();
        in.read(key, CODEC).ifPresent(l -> {
            for (int i = 0; i < SLOTS && i < l.size(); i++) e[i] = l.get(i);
        });
    }
}
