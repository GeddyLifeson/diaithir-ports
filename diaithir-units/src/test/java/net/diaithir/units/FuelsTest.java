package net.diaithir.units;

import static org.junit.jupiter.api.Assertions.*;

import net.diaithir.units.fuel.*;
import org.junit.jupiter.api.Test;

class FuelsTest {
    @Test void csvLoads() { assertEquals(15, Fuels.all().size()); assertEquals(Fuel.Kind.LIQUID, Fuels.get("diesel").kind()); }

    @Test void everyEngineFuelExists() {
        for (String e : new String[]{"steam_piston", "petrol_engine", "diesel_engine", "avgas_piston", "jet_turbine", "electric_motor", "fuel_cell"})
            for (String f : Fuels.engine(e).fuels()) assertNotNull(Fuels.get(f), f);
    }

    @Test void carBurnsSensibleFuel() {
        // 75 kW petrol car at 25 %: 0.0094 L/s, so a 50 L tank at 25 m/s (90 km/h) and 30 kW cruise goes about 330 km.
        Fuel p = Fuels.get("petrol");
        assertEquals(0.00937, Fuels.flowPerSecond(p, 75_000, 0.25), 1e-4);
        double km = Fuels.rangeM(p, 50, 30_000, 0.25, 25) / 1000;
        assertTrue(km > 300 && km < 380, "km=" + km);
    }

    @Test void ivConsumptionIsMbPerTick() { assertEquals(0.47, Fuels.ivConsumption(Fuels.get("petrol"), 75_000, 0.25), 0.01); }

    @Test void storeNeverCreatesOrLosesFuel() {
        SimpleStore a = new SimpleStore(100), b = new SimpleStore(30);
        a.insert(Fuels.get("diesel"), 80);
        assertEquals(30, FuelStore.transfer(a, b, 50), 1e-9);
        assertEquals(50, a.quantity(), 1e-9);
        assertEquals(0, FuelStore.transfer(a, b, 5), 1e-9);
    }

    @Test void storeRefusesMixing() {
        SimpleStore s = new SimpleStore(10);
        s.insert(Fuels.get("diesel"), 5);
        assertEquals(0, s.insert(Fuels.get("petrol"), 5));
        s.extract(5);
        assertEquals(5, s.insert(Fuels.get("petrol"), 5));
    }

    @Test void upgradesNeverPassOne() { assertEquals(0.4, Fuels.upgradedEfficiency(0.25, 0.2), 1e-9); assertTrue(Fuels.upgradedEfficiency(0.9, 5) < 1); }
}
