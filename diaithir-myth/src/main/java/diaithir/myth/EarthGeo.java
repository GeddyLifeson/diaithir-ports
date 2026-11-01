package diaithir.myth;

/**
 * Block to latitude/longitude, the same projection Tellus uses (EarthProjection, Web Mercator mode, which is the
 * default): blocksPerDegree = 111319.49166666667 / worldScale; longitude = x / blocksPerDegree, wrapped at the
 * antimeridian edge of ceil(180 * blocksPerDegree / 16) * 16 blocks; latitude is the inverse Mercator of -z * worldScale
 * on a sphere of radius METERS_PER_DEGREE * 180 / pi, reflected back past the pole seams. The Diaithir pack runs Tellus at
 * world_scale 5 (1:5). Pure maths, no Minecraft classes, so it can be unit tested.
 */
public final class EarthGeo {
    public static final double METERS_PER_DEGREE = 111319.49166666667;
    public static final double MAX_MERCATOR_LATITUDE = 85.05112878;
    public static final double DEFAULT_WORLD_SCALE = 5.0;
    private static final double EARTH_RADIUS_METERS = METERS_PER_DEGREE * 180.0 / Math.PI;

    private EarthGeo() {
    }

    public static double blocksPerDegree(double worldScale) {
        return worldScale <= 0.0 ? 0.0 : METERS_PER_DEGREE / worldScale;
    }

    /** Half the width of the looping world in blocks. */
    public static double edgeBlocks(double worldScale) {
        return Math.ceil(180.0 * blocksPerDegree(worldScale) / 16.0) * 16.0;
    }

    public static double longitude(double blockX, double blockZ, double worldScale) {
        double bpd = blocksPerDegree(worldScale);
        if (bpd <= 0.0) {
            return 0.0;
        }
        double edge = edgeBlocks(worldScale);
        if (Math.abs(blockX) <= edge && Math.abs(blockZ) <= edge) {
            return Math.max(-180.0, Math.min(180.0, blockX / bpd));
        }
        double x = Math.abs(blockZ) > edge ? blockX + edge : blockX;
        double wrapped = x - 2.0 * edge * Math.floor((x + edge) / (2.0 * edge));
        return Math.max(-180.0, Math.min(180.0, wrapped / bpd));
    }

    public static double latitude(double blockZ, double worldScale) {
        if (worldScale <= 0.0) {
            return 0.0;
        }
        double mercatorY = -blockZ * worldScale;
        if (Math.abs(mercatorY) > METERS_PER_DEGREE * 180.0) {
            double edge = edgeBlocks(worldScale) * worldScale;
            if (Math.abs(mercatorY) > edge) {
                mercatorY = Math.signum(mercatorY) * 2.0 * edge - mercatorY;
            }
        }
        double deg = Math.toDegrees(Math.atan(Math.sinh(mercatorY / EARTH_RADIUS_METERS)));
        return Math.max(-MAX_MERCATOR_LATITUDE, Math.min(MAX_MERCATOR_LATITUDE, deg));
    }

    /** Inverse of {@link #latitude}, inside the world: the block Z of a latitude. */
    public static double blockZ(double latitude, double worldScale) {
        double lat = Math.max(-MAX_MERCATOR_LATITUDE, Math.min(MAX_MERCATOR_LATITUDE, latitude));
        double y = EARTH_RADIUS_METERS * Math.log(Math.tan(Math.PI * 0.25 + Math.toRadians(lat) * 0.5));
        return -y / worldScale;
    }

    public static double blockX(double longitude, double worldScale) {
        return longitude * blocksPerDegree(worldScale);
    }
}
