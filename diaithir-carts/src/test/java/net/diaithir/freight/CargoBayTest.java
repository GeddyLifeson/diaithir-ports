package net.diaithir.freight;

import static org.junit.jupiter.api.Assertions.*;

import net.diaithir.freight.CargoBay.Result;
import org.junit.jupiter.api.Test;

class CargoBayTest {
    private static LoadType t(String id) { return LoadType.byId(id); }

    @Test
    void capacityIsSpaceNotSlots() {
        CargoBay box = new CargoBay(2, 2, 5, 28_180);
        int n = 0;
        for (int y = 0; y < 2; y++)
            for (int x = 0; x < 2; x++)
                for (int z = 0; z < 5; z++) if (box.place(t("chest"), false, x, y, z, 100, 8) == Result.OK) n++;
        assertEquals(20, n);
        assertEquals(0, box.freeCells());
        assertEquals(Result.OVERLAP, box.check(t("chest"), false, 0, 0, 0, 0, 8));
        assertEquals(Result.OUT_OF_BAY, box.check(t("chest"), false, 2, 0, 0, 0, 8));
    }

    @Test
    void stackingNeedsSupportAndRespectsLimits() {
        CargoBay b = new CargoBay(4, 4, 4, 100_000);
        assertEquals(Result.UNSUPPORTED, b.check(t("pallet"), false, 0, 1, 0, 0, 8));
        assertEquals(Result.OK, b.place(t("pallet"), false, 0, 0, 0, 1_500, 8));
        assertEquals(Result.OK, b.place(t("pallet"), false, 0, 1, 0, 400, 8)); // 425 on a pallet that bears 2000
        assertEquals(Result.OK, b.place(t("pallet"), false, 0, 2, 0, 1_500, 8)); // bottom pallet now bears 425 + 1525 = 1950
        assertEquals(Result.OVER_STACK, b.check(t("pallet"), false, 0, 3, 0, 100, 8)); // 125 more would pass 2000 on the bottom one
        assertEquals(Result.UNSUPPORTED, b.check(t("barrel"), false, 0, 1, 1, 0, 8));
    }

    @Test
    void cannotTakeFromUnderneath() {
        CargoBay b = new CargoBay(2, 2, 2, 10_000);
        b.place(t("pallet"), false, 0, 0, 0, 100, 8);
        b.place(t("barrel"), false, 0, 1, 0, 100, 8);
        CargoBay.Placed bottom = b.at(0, 0, 0);
        assertFalse(b.canTake(bottom));
        assertTrue(b.take(b.at(0, 1, 0)));
        assertTrue(b.take(bottom));
        assertEquals(0, b.loads().size());
    }

    @Test
    void containersNestAndWeighTheirContents() {
        CargoBay deck = new CargoBay(4, 8, 13, 1_000_000); // a 40 ft flatcar bed, two boxes high
        assertEquals(Result.OK, deck.place(t("container_40"), false, 0, 0, 0, 0, 8));
        CargoBay.Placed c = deck.at(0, 0, 0);
        assertEquals(44, c.inside.cells());
        assertEquals(Result.OK, c.inside.place(t("pallet"), false, 0, 0, 0, 1_000, 8));
        assertEquals(3_800 + 1_025, c.grossKg(), 1e-6);
        // a second box on top of the first
        assertEquals(Result.OK, deck.place(t("container_40"), false, 0, 4, 0, 0, 8));
        // a box cannot sit half on air
        assertEquals(Result.OUT_OF_BAY, deck.check(t("container_20"), false, 2, 0, 0, 0, 8));
    }

    @Test
    void overweightAndAgeAreRefused() {
        CargoBay box = new CargoBay(2, 2, 5, 1_000);
        assertEquals(Result.OVER_PAYLOAD, box.check(t("crate"), false, 0, 0, 0, 1_000, 8)); // 1000 + 55 > 1000
        assertEquals(Result.AGE_LOCKED, new CargoBay(4, 4, 7, 1e6).check(t("container_20"), false, 0, 0, 0, 0, 3));
        assertEquals(Result.UNKNOWN_TYPE, box.check(null, false, 0, 0, 0, 0, 8));
    }

    @Test
    void turnedLoadSwapsFootprint() {
        CargoBay b = new CargoBay(7, 4, 4, 1e6); // a trailer bed laid across
        assertEquals(Result.OUT_OF_BAY, b.check(t("container_20"), false, 0, 0, 0, 0, 8));
        assertEquals(Result.OK, b.place(t("container_20"), true, 0, 0, 0, 0, 8));
    }
}
