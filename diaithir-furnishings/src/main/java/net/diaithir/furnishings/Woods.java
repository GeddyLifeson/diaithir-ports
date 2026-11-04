package net.diaithir.furnishings;

import java.util.List;

/** The vanilla woods that get a signpost. Each entry drives the registration; the art and recipe files are written by tools/gen_data.py from the same list. */
public final class Woods {
    public record Wood(String id, String stripped, String planks) {
    }

    public static final List<Wood> ALL = List.of(
        new Wood("oak", "stripped_oak_log", "oak_planks"),
        new Wood("spruce", "stripped_spruce_log", "spruce_planks"),
        new Wood("birch", "stripped_birch_log", "birch_planks"),
        new Wood("jungle", "stripped_jungle_log", "jungle_planks"),
        new Wood("acacia", "stripped_acacia_log", "acacia_planks"),
        new Wood("dark_oak", "stripped_dark_oak_log", "dark_oak_planks"),
        new Wood("mangrove", "stripped_mangrove_log", "mangrove_planks"),
        new Wood("cherry", "stripped_cherry_log", "cherry_planks"),
        new Wood("pale_oak", "stripped_pale_oak_log", "pale_oak_planks"),
        new Wood("bamboo", "stripped_bamboo_block", "bamboo_planks"),
        new Wood("crimson", "stripped_crimson_stem", "crimson_planks"),
        new Wood("warped", "stripped_warped_stem", "warped_planks"));

    private Woods() {
    }
}
