package net.diaithir.gellings;

import java.util.List;

/** One gelling kind: id, display name, gel colour, the biomes it spawns in (empty = overworld at night), spawn weight. */
public record Species(String id, String name, int color, List<String> biomes, int weight, boolean fireproof) {
    public static final List<Species> ALL = List.of(
            new Species("meadow", "Meadow", 0x5FCB4A, List.of("#minecraft:is_forest", "minecraft:plains", "minecraft:meadow"), 10, false),
            new Species("tide", "Tide", 0x4A8FE0, List.of("#minecraft:is_river", "#minecraft:is_beach", "#minecraft:is_ocean"), 10, false),
            new Species("rouge", "Rouge", 0xD94A4A, List.of("minecraft:savanna", "minecraft:badlands", "minecraft:windswept_savanna"), 8, false),
            new Species("violet", "Violet", 0x9A5AD0, List.of("minecraft:swamp", "minecraft:mangrove_swamp", "minecraft:dark_forest"), 8, false),
            new Species("amber", "Amber", 0xE6C23A, List.of("minecraft:sunflower_plains", "minecraft:cherry_grove", "#minecraft:is_hill"), 6, false),
            new Species("ink", "Ink", 0x2B2B3A, List.of(), 6, false),
            new Species("moss", "Moss", 0x3E9A5A, List.of("#minecraft:is_jungle"), 10, false),
            new Species("frost", "Frost", 0x9ADCF0, List.of("#minecraft:is_taiga", "minecraft:snowy_plains", "minecraft:frozen_river", "minecraft:ice_spikes"), 10, false),
            new Species("dune", "Dune", 0xE0CE8A, List.of("minecraft:desert", "#minecraft:is_beach"), 10, false),
            new Species("ember", "Ember", 0xF08A2A, List.of("#minecraft:is_nether"), 12, true),
            new Species("blush", "Blush", 0xF5A0C8, List.of("#minecraft:is_overworld"), 1, false));

    public static Species byId(String id) {
        for (Species s : ALL) if (s.id.equals(id)) return s;
        return null;
    }
}
