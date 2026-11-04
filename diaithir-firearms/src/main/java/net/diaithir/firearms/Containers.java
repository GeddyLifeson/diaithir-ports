package net.diaithir.firearms;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.List;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Ammo containers from the munitions contract (Global-Repo plans/MUNITIONS_CONTAINERS.md): items tagged c:ammo_container/&lt;kind&gt; that carry the
 * component diaithircrafting:ammo_container = {class, capacity, count, round}. Fully optional: with no such component type registered nothing here
 * does anything and guns reload from loose ammo. The component is read and written through its own codec as JSON, so this module has no compile-time
 * dependency on diaithir-crafting and survives field additions.
 */
public final class Containers {
    static final Identifier COMPONENT = Identifier.fromNamespaceAndPath("diaithircrafting", "ammo_container");

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static DataComponentType<Object> type() {
        DataComponentType t = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(COMPONENT);
        return t != null && t.codec() != null ? t : null;
    }

    public static boolean present() { return type() != null; }

    private static TagKey<Item> kindTag(String kind) { return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ammo_container/" + kind)); }

    private static JsonObject read(ServerPlayer p, ItemStack s, DataComponentType<Object> t) {
        Object v = s.get(t);
        if (v == null) return null;
        var ops = RegistryOps.create(JsonOps.INSTANCE, p.registryAccess());
        return t.codec().encodeStart(ops, v).result().filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject).orElse(null);
    }

    private static boolean write(ServerPlayer p, ItemStack s, DataComponentType<Object> t, JsonObject j) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, p.registryAccess());
        var v = t.codec().parse(ops, j).result();
        v.ifPresent(x -> s.set(t, x));
        return v.isPresent();
    }

    private static int count(JsonObject j) { return j.has("count") ? j.get("count").getAsInt() : 0; }

    /** A container holds this gun's round when its class matches (when it names one) and its round is unset or the gun's own ammo (id or same path). */
    static boolean fits(JsonObject j, Defs.Gun g) {
        if (j.has("class") && !j.get("class").getAsString().equals(g.cls())) return false;
        if (!j.has("round")) return true;
        String r = j.get("round").getAsString();
        if (r.isEmpty()) return true;
        return r.equals(DiaithirFirearms.ID + ":" + g.ammo()) || r.endsWith(":" + g.ammo());
    }

    /** Moves up to want rounds out of the container json; returns how many moved. */
    static int drain(JsonObject j, int want) {
        int n = Math.max(0, Math.min(count(j), want));
        j.addProperty("count", count(j) - n);
        return n;
    }

    private static boolean tagged(ItemStack s, List<String> kinds) {
        for (String k : kinds) if (s.is(kindTag(k))) return true;
        return false;
    }

    /** Rounds waiting in matching containers in the inventory. */
    public static int rounds(ServerPlayer p, Defs.Gun g) {
        var t = type();
        List<String> kinds = Defs.KINDS.getOrDefault(g.cls(), List.of());
        if (t == null || kinds.isEmpty()) return 0;
        int n = 0;
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty() || !tagged(s, kinds)) continue;
            JsonObject j = read(p, s, t);
            if (j != null && fits(j, g)) n += count(j);
        }
        return n;
    }

    /** Takes up to want rounds, fullest container first (no partly empty clutter); returns how many were taken. */
    public static int take(ServerPlayer p, Defs.Gun g, int want) {
        var t = type();
        List<String> kinds = Defs.KINDS.getOrDefault(g.cls(), List.of());
        if (t == null || kinds.isEmpty() || want <= 0) return 0;
        int got = 0;
        var inv = p.getInventory();
        while (got < want) {
            int best = -1, bestN = 0;
            JsonObject bestJ = null;
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack s = inv.getItem(i);
                if (s.isEmpty() || !tagged(s, kinds)) continue;
                JsonObject j = read(p, s, t);
                if (j == null || !fits(j, g) || count(j) <= bestN) continue;
                best = i; bestN = count(j); bestJ = j;
            }
            if (best < 0) break;
            int n = drain(bestJ, want - got);
            if (!write(p, inv.getItem(best), t, bestJ)) break;
            got += n;
        }
        return got;
    }

    private Containers() {}
}
