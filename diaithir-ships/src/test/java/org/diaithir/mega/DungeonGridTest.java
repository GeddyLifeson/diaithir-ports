package org.diaithir.mega;

import static org.junit.jupiter.api.Assertions.*;

import org.diaithir.mega.space.DungeonGrid;
import org.junit.jupiter.api.Test;

class DungeonGridTest {
    @Test
    void oneDungeonPerCellAndStable() {
        long seed = 12345L;
        int spacing = 256;                                    // 16 chunks per cell
        int hits = 0;
        for (int cx = 0; cx < 16; cx++) for (int cz = 0; cz < 16; cz++) if (DungeonGrid.dungeonAt(seed, -28, cx, cz, spacing)) hits++;
        assertEquals(1, hits, "exactly one dungeon in a 16x16 chunk cell");
        assertEquals(DungeonGrid.dungeonPosForCoords(seed, -28, 3, 5, spacing), DungeonGrid.dungeonPosForCoords(seed, -28, 3, 5, spacing));
        assertEquals(DungeonGrid.dungeonPosForCoords(seed, -28, 0, 0, spacing), DungeonGrid.dungeonPosForCoords(seed, -28, 15, 15, spacing));
    }

    @Test
    void negativeCellsAlsoGetOne() {
        int hits = 0;
        for (int cx = -16; cx < 0; cx++) for (int cz = -16; cz < 0; cz++) if (DungeonGrid.dungeonAt(99L, -29, cx, cz, 256)) hits++;
        assertEquals(1, hits);
    }

    @Test
    void bearingIsInRangeAndNoSpacingIsZero() {
        float b = DungeonGrid.directionToNearest(5L, -28, 100.5, -300.25, 256);
        assertTrue(b >= 0 && b < 360, "bearing " + b);
        assertEquals(0F, DungeonGrid.directionToNearest(5L, -28, 10, 10, 0));
    }
}
