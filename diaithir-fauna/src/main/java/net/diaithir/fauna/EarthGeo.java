package net.diaithir.fauna;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.lang.reflect.Method;

/** Block position to latitude/longitude on the Tellus Earth dimension, and the Era year, both by reflection (no hard dependency). */
public final class EarthGeo {
    private static final String GENERATOR = "com.yucareux.tellus.worldgen.EarthChunkGenerator";
    private static final String PROJECTION = "com.yucareux.tellus.worldgen.EarthProjection";
    private static final String ERA = "com.yucareux.tellus.era.EraRuntime";
    /** Era years are counted in LC; AD + 12923 = LC. */
    private static final int LC_OFFSET = 12923;
    private static final int TODAY_AD = 2026;

    private static Object cachedGenerator;
    private static Method[] cachedMethods;
    private static Method eraYear;
    private static boolean eraProbed;

    private EarthGeo() {}

    /** {lat, lon} at the block, or null when the level is not Tellus Earth. */
    public static synchronized double[] latLon(ServerLevel level, BlockPos pos) {
        Object gen = level.getChunkSource().getGenerator();
        if (gen != cachedGenerator) {
            cachedGenerator = gen;
            cachedMethods = probe(gen);
        }
        if (cachedMethods == null) return null;
        try {
            double scale = (double) cachedMethods[1].invoke(cachedMethods[0].invoke(gen));
            return new double[] {(double) cachedMethods[3].invoke(null, (double) pos.getZ(), scale), (double) cachedMethods[2].invoke(null, (double) pos.getX(), scale)};
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static Method[] probe(Object gen) {
        try {
            Class<?> g = null;
            for (Class<?> c = gen.getClass(); c != null; c = c.getSuperclass()) if (c.getName().equals(GENERATOR)) g = c;
            if (g == null) return null;
            Method settings = g.getMethod("settings");
            Class<?> proj = Class.forName(PROJECTION, true, g.getClassLoader());
            return new Method[] {settings, settings.getReturnType().getMethod("worldScale"),
                    proj.getMethod("blockXToLongitude", double.class, double.class), proj.getMethod("blockZToLat", double.class, double.class)};
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    /** Current Era year as an AD year (negative = BC); today when the Era engine is absent. */
    public static synchronized int adYear() {
        if (!eraProbed && cachedGenerator != null) {
            eraProbed = true;
            try {
                ClassLoader cl = cachedGenerator != null ? cachedGenerator.getClass().getClassLoader() : Thread.currentThread().getContextClassLoader();
                eraYear = Class.forName(ERA, true, cl).getMethod("year");
            } catch (ReflectiveOperationException | RuntimeException e) {
                eraYear = null;
            }
        }
        if (eraYear == null) return TODAY_AD;
        try {
            return (int) eraYear.invoke(null) - LC_OFFSET;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return TODAY_AD;
        }
    }
}
