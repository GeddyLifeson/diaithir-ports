package net.diaithir.mega;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Random;
import net.diaithir.mega.LaserDrill.*;
import org.junit.jupiter.api.Test;

class LaserDrillTest {
    private static final List<String> NONE = List.of();

    private static Row row(String out, String cat, int lo, int hi, int w, List<String> dimBlack) {
        return new Row(out, cat, List.of(new Rarity(NONE, NONE, NONE, dimBlack, lo, hi, w)));
    }

    @Test
    void depthBandAndDimensionFilter() {
        Row diamond = row("diamond", "blue", 5, 16, 4, List.of("end"));
        assertNotNull(LaserDrill.validRarity(diamond, "overworld", NONE, 10));
        assertNull(LaserDrill.validRarity(diamond, "overworld", NONE, 40));
        assertNull(LaserDrill.validRarity(diamond, "end", NONE, 10));
    }

    @Test
    void catalystRaisesWeightByModifier() {
        List<Row> rows = List.of(row("iron", "grey", 0, 70, 20, NONE), row("diamond", "blue", 0, 70, 4, NONE));
        assertEquals(4, LaserDrill.weights(rows, "overworld", NONE, 10, List.of()).get(1)[1]);
        assertEquals(4 + 2 * LaserDrill.CATALYST_MODIFIER, LaserDrill.weights(rows, "overworld", NONE, 10, List.of("blue", "blue")).get(1)[1]);
    }

    @Test
    void drawFollowsWeights() {
        List<Row> rows = List.of(row("iron", "grey", 0, 70, 30, NONE), row("diamond", "blue", 0, 70, 10, NONE));
        Random rng = new Random(7);
        int diamond = 0, n = 20000;
        for (int i = 0; i < n; i++) if ("diamond".equals(LaserDrill.pick(rows, "overworld", NONE, 10, List.of(), rng))) diamond++;
        assertEquals(0.25, diamond / (double) n, 0.02);
        assertNull(LaserDrill.pick(rows, "overworld", NONE, 200, List.of(), rng), "below depth band yields nothing");
    }
}
