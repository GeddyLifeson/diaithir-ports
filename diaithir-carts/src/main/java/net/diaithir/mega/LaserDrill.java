package net.diaithir.mega;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Laser-drill ore selection ported from Industrial Foregoing (Buuz135/Industrial-Foregoing, recipe/LaserDrillRarity and
 * block/resourceproduction/tile/OreLaserBaseTile.executeRecipe).
 *
 * Original: MIT License, Copyright 2021 Buuz135. Full notice in LICENSES.md (Industrial Foregoing row).
 * Changes: Minecraft registries, tags, items and the Titanium component framework replaced by plain strings so the rule is pure
 * Java: the first rarity entry whose depth band and biome/dimension filters match gives the row its weight; each lens matching the
 * row's catalyst adds {@link #CATALYST_MODIFIER}; one row is drawn by weight. Dig output in diaithirmega is this draw per work cycle.
 */
public final class LaserDrill {
    private LaserDrill() {}

    /** Upstream OreLaserBaseConfig.catalystModifier default. */
    public static final int CATALYST_MODIFIER = 8;

    /** Empty biome lists mean "any". Dimension: whitelist wins when non-empty, else blacklist excludes. */
    public record Rarity(List<String> biomeWhitelist, List<String> biomeBlacklist, List<String> dimWhitelist, List<String> dimBlacklist,
                         int depthMin, int depthMax, int weight) {
        boolean matches(String dim, List<String> biomeTags, int y) {
            if (y < depthMin || y > depthMax) return false;
            boolean biomeOk = (biomeWhitelist.isEmpty() || biomeWhitelist.stream().anyMatch(biomeTags::contains))
                    && biomeBlacklist.stream().noneMatch(biomeTags::contains);
            boolean dimOk = dimWhitelist.isEmpty() ? !dimBlacklist.contains(dim) : dimWhitelist.contains(dim);
            return biomeOk && dimOk;
        }
    }

    public record Row(String output, String catalyst, List<Rarity> rarities) {}

    /** First matching rarity, in order, as upstream getValidRarity. */
    public static Rarity validRarity(Row row, String dim, List<String> biomeTags, int y) {
        for (Rarity r : row.rarities()) if (r.matches(dim, biomeTags, y)) return r;
        return null;
    }

    /** Effective weight of every eligible row for the given lenses, in row order (0 weights dropped). */
    public static List<int[]> weights(List<Row> rows, String dim, List<String> biomeTags, int y, List<String> lenses) {
        List<int[]> out = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Rarity r = validRarity(rows.get(i), dim, biomeTags, y);
            if (r == null) continue;
            int w = r.weight();
            for (String lens : lenses) if (lens.equals(rows.get(i).catalyst())) w += CATALYST_MODIFIER;
            if (w > 0) out.add(new int[] {i, w});
        }
        return out;
    }

    /** One work cycle: draw an output by weight, or null when nothing is eligible. */
    public static String pick(List<Row> rows, String dim, List<String> biomeTags, int y, List<String> lenses, Random rng) {
        List<int[]> ws = weights(rows, dim, biomeTags, y, lenses);
        int total = 0;
        for (int[] w : ws) total += w[1];
        if (total <= 0) return null;
        int roll = rng.nextInt(total);
        for (int[] w : ws) {
            roll -= w[1];
            if (roll < 0) return rows.get(w[0]).output();
        }
        return null;
    }
}
