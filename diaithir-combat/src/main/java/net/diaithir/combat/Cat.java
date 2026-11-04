package net.diaithir.combat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLPaths;

/** One weapon category, read from data/diaithircombat/categories/&lt;id&gt;.json (override: config/diaithircombat/categories). */
public final class Cat {
    public record Step(float damage, int cooldown, float stamina, float arcMult, float rangeMult, float kbMult, String pattern) {}

    public final String id;
    public final boolean ranged;
    public final String pattern;
    public final float range, arc, width, knockback;
    public final int maxTargets, comboWindow, parryWindow, drawTicks;
    public final float dropReduction;
    public final String fallback;
    public final List<Step> combo;
    public final TagKey<Item> tag;

    private Cat(JsonObject o) {
        id = o.get("id").getAsString();
        ranged = o.has("kind") && o.get("kind").getAsString().equals("ranged");
        pattern = str(o, "pattern", "slash");
        range = f(o, "range", 3.0f);
        arc = f(o, "arc", 90f);
        width = f(o, "width", 0.4f);
        knockback = f(o, "knockback", 0.4f);
        maxTargets = (int) f(o, "max_targets", 1);
        comboWindow = (int) f(o, "combo_window", 12);
        parryWindow = (int) f(o, "parry_window", 0);
        drawTicks = (int) f(o, "draw_ticks", 20);
        dropReduction = f(o, "drop_reduction", 0f);
        fallback = str(o, "fallback", "minecraft:iron_sword");
        List<Step> st = new ArrayList<>();
        if (o.has("combo")) {
            for (var e : o.getAsJsonArray("combo")) {
                JsonObject s = e.getAsJsonObject();
                st.add(new Step(f(s, "damage", 1f), (int) f(s, "cooldown", 10), f(s, "stamina", 2f), f(s, "arc_mult", 1f),
                        f(s, "range_mult", 1f), f(s, "knockback_mult", 1f), str(s, "pattern", pattern)));
            }
        }
        combo = List.copyOf(st);
        tag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DiaithirCombat.ID, "weapons/" + id));
    }

    private static float f(JsonObject o, String k, float d) { return o.has(k) ? o.get(k).getAsFloat() : d; }
    private static String str(JsonObject o, String k, String d) { return o.has(k) ? o.get(k).getAsString() : d; }

    public boolean matches(ItemStack s) { return s.is(tag); }

    // ---- registry ----
    private static final Map<String, Cat> ALL = new LinkedHashMap<>();
    private static final Map<Item, Cat> CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Cat NONE = fromJson("{\"id\":\"none\"}");

    static Cat fromJson(String s) { return new Cat(new Gson().fromJson(s, JsonObject.class)); }

    public static Map<String, Cat> all() { return ALL; }

    public static void load() {
        ALL.clear(); CACHE.clear();
        Gson g = new Gson();
        String base = "/data/" + DiaithirCombat.ID + "/categories/";
        try (Reader r = new InputStreamReader(Cat.class.getResourceAsStream(base + "index.json"))) {
            JsonArray idx = g.fromJson(r, JsonArray.class);
            Path dir = FMLPaths.CONFIGDIR.get().resolve("diaithircombat/categories");
            for (var e : idx) {
                String id = e.getAsString();
                JsonObject o;
                Path ov = dir.resolve(id + ".json");
                if (Files.exists(ov)) {
                    try (Reader rr = Files.newBufferedReader(ov)) { o = g.fromJson(rr, JsonObject.class); }
                } else {
                    try (Reader rr = new InputStreamReader(Cat.class.getResourceAsStream(base + id + ".json"))) { o = g.fromJson(rr, JsonObject.class); }
                }
                ALL.put(id, new Cat(o));
            }
        } catch (Exception ex) {
            throw new IllegalStateException("diaithircombat categories", ex);
        }
    }

    public static void clearCache() { CACHE.clear(); }

    /** Category of a held stack, or null. Ranged and melee categories share the lookup; callers filter on {@link #ranged}. */
    public static Cat of(ItemStack s) {
        if (s.isEmpty()) return null;
        Cat c = CACHE.get(s.getItem());
        if (c == null) {
            c = NONE;
            for (Cat k : ALL.values()) if (k.matches(s)) { c = k; break; }
            CACHE.put(s.getItem(), c);
        }
        return c == NONE ? null : c;
    }

    /** Test hook: forces the category of every stack. */
    public static volatile Cat override;

    public static Cat melee(ItemStack s) { if (override != null) return override.ranged ? null : override; Cat c = of(s); return c != null && !c.ranged ? c : null; }
    public static Cat bow(ItemStack s) { Cat c = of(s); return c != null && c.ranged ? c : null; }
}
