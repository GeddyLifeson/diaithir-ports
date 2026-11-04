package net.diaithir.worldvisuals.soundphysics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FactorTest {
    @Test
    void capsAtMaxBlocks() {
        assertEquals(1f, Muffle.factor(0, 0.6, 4));
        assertEquals((float) Math.pow(0.6, 4), Muffle.factor(9, 0.6, 4));
    }
}
