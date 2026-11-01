package net.diaithir.sharedspace;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CellsTest {
    @Test
    void cellAtSplitsTheSpaceIntoQuarters() {
        assertEquals(0, Cells.at(0.2, 0.2));
        assertEquals(1, Cells.at(0.8, 0.2));
        assertEquals(2, Cells.at(0.2, 0.8));
        assertEquals(3, Cells.at(0.8, 0.8));
    }

    @Test
    void oppositeIsDiagonal() {
        for (int i = 0; i < Cells.COUNT; i++) {
            assertEquals(i, Cells.opposite(Cells.opposite(i)));
            assertEquals(-Cells.offsetX(i), Cells.offsetX(Cells.opposite(i)));
            assertEquals(-Cells.offsetZ(i), Cells.offsetZ(Cells.opposite(i)));
        }
    }

    @Test
    void nearestFreeSkipsTakenCells() {
        assertEquals(1, Cells.nearestFree(new boolean[] {true, false, false, false}, 0.6, 0.4));
        assertEquals(3, Cells.nearestFree(new boolean[] {true, true, true, false}, 0.1, 0.1));
        assertEquals(-1, Cells.nearestFree(new boolean[] {true, true, true, true}, 0.5, 0.5));
    }
}
