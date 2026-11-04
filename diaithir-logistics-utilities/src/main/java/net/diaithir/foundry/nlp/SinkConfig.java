// SPDX-License-Identifier: LGPL-3.0-only
// Design derived from Not Logistics Pipes (c) Contributors to Not Logistics Pipes <https://nlp.rs485.network>, reimplemented in Java for NeoForge 26.1.2 (Diaithir).
package net.diaithir.foundry.nlp;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** What a sink pipe accepts: everything, or the listed items. Higher priority is served first, nearer breaks ties. */
public final class SinkConfig {
    public static final int DEFAULT_PRIORITY = 50;

    public int priority = DEFAULT_PRIORITY;
    public boolean all;
    public final List<Identifier> items = new ArrayList<>();

    public boolean wants(ItemStack stack) {
        return all || items.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    /** Adds the item, or removes it if listed. Returns true if now listed. */
    public boolean toggle(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (items.remove(id)) return false;
        items.add(id);
        return true;
    }

    public String describe() {
        return "priority " + priority + ", " + (all ? "everything" : items.isEmpty() ? "nothing" : items.size() + " item(s)");
    }
}
