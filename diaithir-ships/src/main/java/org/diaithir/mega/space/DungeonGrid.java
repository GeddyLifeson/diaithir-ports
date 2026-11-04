package org.diaithir.mega.space;

import java.util.Random;

/**
 * Deterministic one-per-cell placement of space dungeons and the compass bearing to the nearest one.
 *
 * Ported from Galacticraft Legacy (TeamGalacticraft/Galacticraft-Legacy, core/world/gen/dungeon/MapGenDungeon.java
 * getDungeonPosForCoords / directionToNearestDungeon, and GCCoreUtil.getAngleForRelativePosition).
 * Original: MIT License, Copyright (c) 2022-2023 Team Galacticraft. Notice in LICENSES.md (Galacticraft Legacy row).
 * Changes: World and provider replaced by explicit seed, dimension id and spacing arguments; same arithmetic and seed mix so
 * placements match upstream for the same inputs.
 */
public final class DungeonGrid {
    private DungeonGrid() {}

    /** Chunk (x high 32 bits, z low 32 bits) of the dungeon inside the spacing cell holding (chunkX, chunkZ). */
    public static long dungeonPosForCoords(long worldSeed, int dimension, int chunkX, int chunkZ, int spacing) {
        final int numChunks = spacing / 16;
        if (chunkX < 0) chunkX -= numChunks - 1;
        if (chunkZ < 0) chunkZ -= numChunks - 1;
        int k = chunkX / numChunks;
        int l = chunkZ / numChunks;
        long seed = (long) k * 341873128712L + (long) l * 132897987541L + worldSeed + (long) (10387340 + dimension);
        Random random = new Random();
        random.setSeed(seed);
        k = k * numChunks + random.nextInt(numChunks);
        l = l * numChunks + random.nextInt(numChunks);
        return (((long) k) << 32) + l;
    }

    public static boolean dungeonAt(long worldSeed, int dimension, int chunkX, int chunkZ, int spacing) {
        long p = dungeonPosForCoords(worldSeed, dimension, chunkX, chunkZ, spacing);
        return (int) (p >> 32) == chunkX && (int) p == chunkZ;
    }

    /** Bearing 0..360 degrees, 0 due north, to the nearest dungeon; 0 when the world has no dungeons (spacing 0). */
    public static float directionToNearest(long worldSeed, int dimension, double xpos, double zpos, int spacing) {
        if (spacing == 0) return 0F;
        int x = (int) Math.floor(xpos);
        int z = (int) Math.floor(zpos);
        int quadrantX = x % spacing, quadrantZ = z % spacing;
        int searchOffsetX = quadrantX / (spacing / 2), searchOffsetZ = quadrantZ / (spacing / 2);
        double nearestX = 0, nearestZ = 0, nearest = Double.MAX_VALUE;
        for (int cx = searchOffsetX - 1; cx < searchOffsetX + 1; cx++) {
            for (int cz = searchOffsetZ - 1; cz < searchOffsetZ + 1; cz++) {
                long p = dungeonPosForCoords(worldSeed, dimension, (x + cx * spacing) / 16, (z + cz * spacing) / 16, spacing);
                int i = 2 + (((int) (p >> 32)) << 4);
                int j = 2 + (((int) p) << 4);
                double oX = i - xpos, oZ = j - zpos;
                double d = oX * oX + oZ * oZ;
                if (d < nearest) { nearest = d; nearestX = oX; nearestZ = oZ; }
            }
        }
        return ((float) Math.atan2(nearestX, -nearestZ) * (180F / (float) Math.PI) + 360F) % 360F;
    }
}
