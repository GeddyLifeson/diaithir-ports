package net.diaithir.compat;

import java.lang.reflect.Method;

/**
 * Reads Tellus' own projection by reflection, so Diaithir Compat has no hard dependency on it.
 * Block X/Z are converted with {@code EarthProjection.blockXToLongitude / blockZToLat} at the world scale of the generator's
 * settings, which is the active preset's scale.
 */
public final class TellusBridge {
    static final String GENERATOR = "com.yucareux.tellus.worldgen.EarthChunkGenerator";
    static final String PROJECTION = "com.yucareux.tellus.worldgen.EarthProjection";

    private final Method settings;
    private final Method worldScale;
    private final Method blockXToLongitude;
    private final Method blockZToLat;

    private TellusBridge(Method settings, Method worldScale, Method blockXToLongitude, Method blockZToLat) {
        this.settings = settings;
        this.worldScale = worldScale;
        this.blockXToLongitude = blockXToLongitude;
        this.blockZToLat = blockZToLat;
    }

    /** True when the chunk generator is Tellus' Earth generator (the Earth dimension). */
    public static boolean isEarthGenerator(Object generator) {
        for (Class<?> c = generator.getClass(); c != null; c = c.getSuperclass()) {
            if (c.getName().equals(GENERATOR)) {
                return true;
            }
        }
        return false;
    }

    /** Null when Tellus is not on the class path or its API does not match. */
    public static TellusBridge forGenerator(Object generator) {
        try {
            Class<?> gen = null;
            for (Class<?> c = generator.getClass(); c != null; c = c.getSuperclass()) {
                if (c.getName().equals(GENERATOR)) {
                    gen = c;
                }
            }
            if (gen == null) {
                return null;
            }
            Method settings = gen.getMethod("settings");
            Method scale = settings.getReturnType().getMethod("worldScale");
            Class<?> proj = Class.forName(PROJECTION, true, gen.getClassLoader());
            return new TellusBridge(settings, scale,
                    proj.getMethod("blockXToLongitude", double.class, double.class),
                    proj.getMethod("blockZToLat", double.class, double.class));
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    /** {latitude, longitude} for a block position, or null if the call fails. */
    public double[] latLon(Object generator, double blockX, double blockZ) {
        try {
            double scale = (double) this.worldScale.invoke(this.settings.invoke(generator));
            return latLon(blockX, blockZ, scale);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    public double[] latLon(double blockX, double blockZ, double scale) throws ReflectiveOperationException {
        double lon = (double) this.blockXToLongitude.invoke(null, blockX, scale);
        double lat = (double) this.blockZToLat.invoke(null, blockZ, scale);
        return new double[] {lat, lon};
    }

    /** For tests: a bridge on the projection class alone. */
    static TellusBridge forProjection(Class<?> proj) throws ReflectiveOperationException {
        return new TellusBridge(null, null,
                proj.getMethod("blockXToLongitude", double.class, double.class),
                proj.getMethod("blockZToLat", double.class, double.class));
    }
}
