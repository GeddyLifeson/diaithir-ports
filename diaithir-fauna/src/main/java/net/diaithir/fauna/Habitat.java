package net.diaithir.fauna;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;

/** Which species may live at a position: Earth range, habitat from the biome, extinction against the Era year. */
public final class Habitat {
    private Habitat() {}

    private static int bit(String h) { return 1 << Species.HABS.indexOf(h); }

    private static int bits(String... hs) {
        int m = 0;
        for (String h : hs) m |= bit(h);
        return m;
    }

    /** Habitat bits of the biome and depth at the position; related habitats are folded together (plains with grassland, and so on). */
    public static int mask(ServerLevelAccessor level, BlockPos pos) {
        Holder<Biome> b = level.getBiome(pos);
        String p = b.unwrapKey().map(k -> k.identifier().getPath()).orElse("");
        int m = 0;
        if (b.is(BiomeTags.IS_FOREST)) m |= bits("forest");
        if (b.is(BiomeTags.IS_JUNGLE)) m |= bits("jungle");
        if (b.is(BiomeTags.IS_TAIGA)) m |= bits("taiga", "forest");
        if (b.is(BiomeTags.IS_SAVANNA)) m |= bits("savanna", "grassland");
        if (b.is(BiomeTags.IS_BADLANDS)) m |= bits("badlands", "desert");
        if (b.is(BiomeTags.IS_MOUNTAIN) || b.is(BiomeTags.IS_HILL)) m |= bits("mountain");
        if (b.is(BiomeTags.IS_RIVER)) m |= bits("river", "lake", "wetland");
        if (b.is(BiomeTags.IS_BEACH)) m |= bits("coast", "island");
        if (b.is(BiomeTags.IS_OCEAN)) {
            boolean warm = p.contains("warm");
            boolean cold = p.contains("cold") || p.contains("frozen");
            if (warm || !cold) m |= bits("ocean_warm", "reef");
            if (cold || !warm) m |= bits("ocean_cold");
            if (b.is(BiomeTags.IS_DEEP_OCEAN)) m |= bits("ocean_deep");
            m |= bits("coast");
        }
        if (p.contains("plains") || p.contains("meadow") || p.contains("steppe")) m |= bits("plains", "grassland");
        if (p.contains("swamp") || p.contains("mangrove") || p.contains("marsh")) m |= bits("swamp", "wetland", "lake");
        if (p.contains("desert")) m |= bits("desert");
        if (p.contains("snow") || p.contains("frozen") || p.contains("ice") || p.contains("grove")) m |= bits("icy", "tundra");
        if (p.contains("lake")) m |= bits("lake", "river");
        if (p.contains("shore") || p.contains("beach")) m |= bits("coast", "island");
        if (p.contains("peak") || p.contains("slope") || p.contains("hill") || p.contains("jagged") || p.contains("stony")) m |= bits("mountain");
        if (m == 0) m = bits("plains", "grassland", "forest");
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
        if (pos.getY() < surface - 8) m = bit("cave");
        return m;
    }

    /** Species of a plan that can live here now. */
    public static List<Species> candidates(String plan, ServerLevelAccessor level, BlockPos pos) {
        int mask = mask(level, pos);
        int year = EarthGeo.adYear();
        double[] ll = level instanceof ServerLevel sl ? EarthGeo.latLon(sl, pos) : null;
        List<Species> out = new ArrayList<>();
        for (Species s : Species.ofPlan(plan)) {
            if (!s.aliveAt(year)) continue;
            if (s.hab != 0 && (s.hab & mask) == 0) continue;
            if (ll != null && !s.inRange(ll[0], ll[1])) continue;
            out.add(s);
        }
        return out;
    }

    /** One species picked at random, big animals rarer than small ones; null when none can live here. */
    public static Species pick(String plan, ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        List<Species> c = candidates(plan, level, pos);
        if (c.isEmpty()) return null;
        double total = 0;
        double[] w = new double[c.size()];
        for (int i = 0; i < w.length; i++) total += w[i] = 1.0 / (1.0 + Math.sqrt(c.get(i).kg));
        double r = random.nextDouble() * total;
        for (int i = 0; i < w.length; i++) if ((r -= w[i]) <= 0) return c.get(i);
        return c.get(c.size() - 1);
    }
}
