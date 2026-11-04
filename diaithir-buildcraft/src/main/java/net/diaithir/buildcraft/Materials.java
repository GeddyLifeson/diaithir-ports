package net.diaithir.buildcraft;

import java.util.List;

/** Every material that gets a block family. tools/gen_data.py reads this file's table, so edit only here. */
public final class Materials {
    public record Mat(String id, String texture, String block, String setType) {
    }

    public static final List<Mat> WOODS = List.of(
        new Mat("oak", "oak_planks", "OAK_PLANKS", "OAK"),
        new Mat("spruce", "spruce_planks", "SPRUCE_PLANKS", "SPRUCE"),
        new Mat("birch", "birch_planks", "BIRCH_PLANKS", "BIRCH"),
        new Mat("jungle", "jungle_planks", "JUNGLE_PLANKS", "JUNGLE"),
        new Mat("acacia", "acacia_planks", "ACACIA_PLANKS", "ACACIA"),
        new Mat("dark_oak", "dark_oak_planks", "DARK_OAK_PLANKS", "DARK_OAK"),
        new Mat("mangrove", "mangrove_planks", "MANGROVE_PLANKS", "MANGROVE"),
        new Mat("cherry", "cherry_planks", "CHERRY_PLANKS", "CHERRY"),
        new Mat("bamboo", "bamboo_planks", "BAMBOO_PLANKS", "BAMBOO"),
        new Mat("crimson", "crimson_planks", "CRIMSON_PLANKS", "CRIMSON"),
        new Mat("warped", "warped_planks", "WARPED_PLANKS", "WARPED"));

    public static final List<Mat> STONES = List.of(
        new Mat("stone", "stone", "STONE", ""),
        new Mat("cobblestone", "cobblestone", "COBBLESTONE", ""),
        new Mat("stone_brick", "stone_bricks", "STONE_BRICKS", ""),
        new Mat("brick", "bricks", "BRICKS", ""),
        new Mat("sandstone", "sandstone", "SANDSTONE", ""),
        new Mat("granite", "granite", "GRANITE", ""),
        new Mat("diorite", "diorite", "DIORITE", ""),
        new Mat("andesite", "andesite", "ANDESITE", ""),
        new Mat("deepslate_brick", "deepslate_bricks", "DEEPSLATE_BRICKS", ""),
        new Mat("mud_brick", "mud_bricks", "MUD_BRICKS", ""),
        new Mat("blackstone", "blackstone", "BLACKSTONE", ""));

    private Materials() {
    }
}
