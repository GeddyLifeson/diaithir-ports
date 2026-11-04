package net.diaithir.animation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** What an item animates as: a tool/melee class or a gun class, read from the item tags data/diaithiranimation/tags/item/anim. */
public record Kind(boolean gun, String cls) {
    public static final List<String> MELEE = new ArrayList<>();
    public static final List<String> GUNS = new ArrayList<>();
    public static final List<String> OWN_GUNS = new ArrayList<>();
    private static final Map<Item, Kind> CACHE = new ConcurrentHashMap<>();
    private static final Kind NONE = new Kind(false, "");

    static {
        try (var r = new InputStreamReader(Kind.class.getResourceAsStream("/clips.json"))) {
            JsonObject j = new Gson().fromJson(r, JsonObject.class);
            for (var e : j.getAsJsonObject("classes").entrySet()) {
                if (e.getKey().startsWith("gun.")) GUNS.add(e.getKey().substring(4));
                else if (e.getKey().equals("own_gun_reloads")) e.getValue().getAsJsonArray().forEach(x -> OWN_GUNS.add(x.getAsString()));
                else if (!e.getKey().equals("seat")) MELEE.add(e.getKey());
            }
        } catch (Exception e) {
            throw new IllegalStateException("clips.json missing", e);
        }
    }

    public static void invalidate() { CACHE.clear(); }

    public static TagKey<Item> tag(boolean gun, String cls) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DiaithirAnimation.ID, "anim/" + (gun ? "gun/" : "") + cls));
    }

    /** Null when the item has no animation class. */
    public static Kind of(ItemStack s) {
        if (s.isEmpty()) return null;
        Kind k = CACHE.computeIfAbsent(s.getItem(), Kind::resolve);
        return k == NONE ? null : k;
    }

    private static Kind resolve(Item it) {
        ItemStack s = it.getDefaultInstance();
        for (String c : GUNS) if (s.is(tag(true, c))) return new Kind(true, c);
        for (String c : MELEE) if (s.is(tag(false, c))) return new Kind(false, c);
        return NONE;
    }

    /** Clip id for an action of this kind; own guns have their own reload length. */
    public String clip(Item item, String action) {
        if (gun) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (action.equals("reload") && id.getNamespace().equals("diaithirfirearms") && OWN_GUNS.contains(id.getPath())) return "gun." + id.getPath() + ".reload";
            return "gun." + cls + "." + action;
        }
        return cls + "." + action;
    }
}
