package net.diaithir.foundry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Pure name logic for metals: any c:ingots/<metal> tag becomes a metal. Ores, raw, ingots and storage blocks melt by tag. */
public final class MetalRegistry {
    private static final String COMMON = "c";

    private MetalRegistry() {
    }

    public static TagKey<Item> tag(String group, String metal) {
        return TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(COMMON, group + "/" + metal));
    }

    /** Extracts the metal name from a tag path such as "ingots/copper", or null when it is not an ingot tag. */
    public static String metalFromTagPath(String path) {
        if (!path.startsWith("ingots/")) return null;
        String m = path.substring("ingots/".length());
        return m.isEmpty() || m.contains("/") ? null : m;
    }

    /** All metals that currently have an ingot tag with at least one item. */
    public static Set<String> discover() {
        Set<String> out = new TreeSet<>();
        BuiltInRegistries.ITEM.getTags().forEach(named -> {
            String m = metalFromTagPath(named.key().location().getPath());
            if (m != null && named.size() > 0) out.add(m);
        });
        return out;
    }

    /** Units of molten metal one item yields, given which tag group it belongs to. */
    public static int meltYield(String group) {
        return switch (group) {
            case "ores" -> 180;
            case "raw_materials" -> 90;
            case "ingots", "gems" -> 90;
            case "nuggets" -> 10;
            case "storage_blocks" -> 810;
            case "plates" -> 90;
            case "rods" -> 45;
            case "gears" -> 360;
            default -> 0;
        };
    }

    public static List<String> meltGroups() {
        return new ArrayList<>(List.of("ores", "raw_materials", "ingots", "nuggets", "storage_blocks", "plates", "rods", "gears"));
    }
}
