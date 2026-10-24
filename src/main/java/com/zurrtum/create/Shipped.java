package com.zurrtum.create;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * The part of Create that ships in this pack. Everything else stays registered (the code graph needs it) but has no
 * recipe, no art and no creative tab entry, so it is unobtainable. The list is generated with the assets.
 */
public final class Shipped {
    private static Set<String> ids;

    private Shipped() {
    }

    private static synchronized Set<String> ids() {
        if (ids == null) {
            Set<String> set = new HashSet<>();
            try (InputStream in = Shipped.class.getResourceAsStream("/data/create/shipped_items.txt")) {
                if (in != null) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                    String l;
                    while ((l = r.readLine()) != null) {
                        l = l.trim();
                        if (!l.isEmpty()) {
                            set.add(l);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            ids = set;
        }
        return ids;
    }

    public static boolean has(Item item) {
        var key = BuiltInRegistries.ITEM.getKey(item);
        if (!"create".equals(key.getNamespace())) {
            return true;
        }
        Set<String> s = ids();
        return s.isEmpty() || s.contains(key.getPath());
    }

    public static CreativeModeTab.Output filter(CreativeModeTab.Output out) {
        return new CreativeModeTab.Output() {
            @Override
            public void accept(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
                if (has(stack.getItem())) {
                    out.accept(stack, visibility);
                }
            }
        };
    }
}
