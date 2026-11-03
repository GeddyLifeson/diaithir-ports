package net.diaithir.vehicles.fuel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FuelTankTest {
    @Test
    void fillsAndRefusesMixing() {
        FuelTank t = new FuelTank(60);
        assertEquals(40, t.fill("c:diesel", 40, false));
        assertEquals(0, t.fill("c:gasoline", 1, false));
        assertEquals(20, t.fill("c:diesel", 50, false));
        assertEquals(60, t.amountL());
        assertEquals(0, t.spaceL());
    }

    @Test
    void simulateChangesNothingAndEmptyClearsKind() {
        FuelTank t = new FuelTank(10);
        assertEquals(10, t.fill("c:diesel", 20, true));
        assertTrue(t.isEmpty());
        t.fill("c:diesel", 5, false);
        assertEquals(5, t.drain(9, false));
        assertNull(t.fuelId());
        assertEquals(5, t.fill("c:gasoline", 5, false));
    }

    @Test
    void unknownFuelRefused() {
        assertEquals(0, new FuelTank(1).fill("c:water", 500, false));
    }

    @Test
    void burnUsesEnergyDensity() {
        FuelTank t = new FuelTank(100);
        t.fill("c:diesel", 100, false);
        // 38.6 MJ/L at 30 % = 11.58 MJ per litre of shaft work
        double got = t.burn(11.5e6, 0.30);
        assertEquals(11.5e6, got, 1e4);
        assertEquals(99, t.amountL());
        for (int i = 0; i < 20; i++) t.burn(0.5e6, 0.30); // many small ticks: 10 MJ more, still under the next litre's credit
        assertEquals(98, t.amountL());
    }

    @Test
    void burnStopsWhenDry() {
        FuelTank t = new FuelTank(1);
        t.fill("c:gasoline", 1, false);
        double got = t.burn(1e9, 0.25);
        assertEquals(34.2e6 * 0.25, got, 1e4);
        assertTrue(t.isEmpty());
    }

    @Test
    void transferMovesWhatFits() {
        FuelTank a = new FuelTank(50), b = new FuelTank(20);
        a.fill("c:kerosene", 50, false);
        assertEquals(20, a.transferTo(b, 100));
        assertEquals(30, a.amountL());
        assertEquals("c:kerosene", b.fuelId());
        a.drain(30, false);
        a.fill("c:diesel", 5, false);
        assertEquals(0, a.transferTo(b, 1)); // b holds kerosene
        assertEquals(5, a.amountL());
    }

    @Test
    void energyReadout() {
        FuelTank t = new FuelTank(10);
        t.fill("c:ethanol", 10, false);
        assertEquals(21.1e6 * 10, t.energyJoules(), 1);
    }

    @Test
    void snapshotRestores() {
        FuelTank t = new FuelTank(10);
        t.fill("c:diesel", 8, false);
        FuelTank.Snapshot s = t.snapshot();
        t.drain(8, false);
        t.restore(s);
        assertEquals(8, t.amountL());
        assertEquals("c:diesel", t.fuelId());
    }
}
