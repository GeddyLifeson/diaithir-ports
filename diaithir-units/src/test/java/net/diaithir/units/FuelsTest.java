package net.diaithir.units;

import static org.junit.jupiter.api.Assertions.*;

import net.diaithir.units.fuel.*;
import org.junit.jupiter.api.Test;

class FuelsTest {
    @Test void csvLoads() { assertEquals(23, Fuels.all().size()); assertEquals(Fuel.Kind.LIQUID, Fuels.get("diesel").kind()); }

    @Test void everyEngineFuelExists() {
        for (String e : new String[]{"draft_animal", "steam_piston", "petrol_engine", "diesel_engine", "avgas_piston", "jet_turbine", "electric_motor", "fuel_cell", "arcane_motor"})
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

    @Test void magicFuelIsPricedAtOneHundredJoulesPerMana() {
        assertEquals(40_000 / 10.0, MagicFuel.joules("botania:mana_powder"), 1e-9);
        assertEquals(1000.0 * 10, MagicFuel.joules("ars_nouveau:source_gem"), 1e-9);
        assertEquals(9 * MagicFuel.joules("ars_nouveau:source_gem"), MagicFuel.joules("ars_nouveau:source_gem_block"), 1e-9);
        assertEquals(0, MagicFuel.joules("minecraft:stick"), 0);
        assertFalse(MagicFuel.isFuel("minecraft:coal"));
    }

    @Test void magicFuelIsFarWeakerThanCoal() {
        for (String id : MagicFuel.ids()) assertTrue(MagicFuel.coalEquivalent(id) < 0.01, id);
        // 80 kJ at 1 MW and 50 % is 0.04 s of shaft power
        assertEquals(0.04, MagicFuel.burnSeconds("botania:mana_diamond", 1e6, 0.5), 1e-9);
    }

    @Test void magicCarriersMatchTheUnitTable() {
        String[][] pairs = {{"mana", "botania:mana"}, {"aura", "naturesaura:aura"}, {"ember", "embers:ember"}, {"life_points", "bloodmagic:lp"}, {"vis", "thaumcraft:vis"}, {"source", "arsnouveau:source"}};
        for (String[] p : pairs)
            assertEquals(Units.convert(1, p[1], "j"), Fuels.get(p[0]).energyJ(1), 1e-6, p[0]);
        assertEquals(0, Fuels.get("mana").massKg(5));
    }

    @Test void anyEngineClassCanTakeMagicOnlyIfListed() {
        assertTrue(Fuels.engine("arcane_motor").accepts("mana"));
        assertTrue(Fuels.engine("steam_piston").accepts("ember"));
        assertFalse(Fuels.engine("diesel_engine").accepts("mana"));
    }

    @Test void trainOnManaBurnsQuantaAtTheCarrierRate() {
        // 1 MW arcane locomotive at 60 %: 1e6 / 0.6 / 100 J = about 16 667 quanta per second.
        assertEquals(16_667, Fuels.flowPerSecond(Fuels.get("mana"), 1_000_000, 0.60), 1);
    }
}
