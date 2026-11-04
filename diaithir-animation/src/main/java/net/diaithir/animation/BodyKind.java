package net.diaithir.animation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;

/** What a held item does with the body: throwable, food, drink, horn, spyglass, map, tool, mount, wings, scroll, rod. Tags data/diaithiranimation/tags/item/anim/body/<kind>, with vanilla use animations as a fallback. */
public final class BodyKind {
    public static final List<String> KINDS = new ArrayList<>();
    private static final Map<Item, String> CACHE = new ConcurrentHashMap<>();
    private static final String NONE = "";

    static {
        try (var r = new InputStreamReader(BodyKind.class.getResourceAsStream("/body_clips.json"))) {
            JsonObject j = new Gson().fromJson(r, JsonObject.class);
            j.getAsJsonObject("item_kinds").keySet().forEach(KINDS::add);
        } catch (Exception e) {
            throw new IllegalStateException("body_clips.json missing", e);
        }
    }

    public static void invalidate() { CACHE.clear(); }

    public static TagKey<Item> tag(String kind) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DiaithirAnimation.ID, "anim/body/" + kind));
    }

    /** Null when the item does nothing special with the body. */
    public static String of(ItemStack s) {
        if (s.isEmpty()) return null;
        String k = CACHE.computeIfAbsent(s.getItem(), it -> resolve(it.getDefaultInstance()));
        return k.isEmpty() ? null : k;
    }

    private static String resolve(ItemStack s) {
        for (String k : KINDS) if (s.is(tag(k))) return k;
        var c = s.get(DataComponents.CONSUMABLE);
        if (c != null) {
            ItemUseAnimation a = c.animation();
            if (a == ItemUseAnimation.EAT) return "food";
            if (a == ItemUseAnimation.DRINK) return "drink";
            if (a == ItemUseAnimation.TOOT_HORN) return "horn";
            if (a == ItemUseAnimation.SPYGLASS) return "spyglass";
        }
        return NONE;
    }

    private BodyKind() {}
}
