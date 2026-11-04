package net.diaithir.claims.compass;

import net.minecraft.server.level.ServerPlayer;

/** Chosen structure index per player, kept until the server restarts. */
final class Selection {
    private static final java.util.Map<java.util.UUID, Integer> CHOSEN = new java.util.concurrent.ConcurrentHashMap<>();

    private Selection() {}

    static int get(ServerPlayer p) {
        return CHOSEN.getOrDefault(p.getUUID(), 0);
    }

    static void set(ServerPlayer p, int i) {
        CHOSEN.put(p.getUUID(), i);
    }

    /** Compass point for an offset; north is negative z. */
    static String dir(int dx, int dz) {
        double deg = Math.toDegrees(Math.atan2(dx, -dz));
        String[] names = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        return names[(int) Math.floorMod(Math.round(deg / 45.0), 8L)];
    }
}
