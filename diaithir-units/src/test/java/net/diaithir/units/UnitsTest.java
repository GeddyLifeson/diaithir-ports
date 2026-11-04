package net.diaithir.units;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UnitsTest {
    @Test void feIsJoule() { assertEquals(1000, Units.convert(1000, "neoforge:fe", "j"), 1e-9); }
    @Test void mbIsLitre() { assertEquals(1000, Units.convert(1, "neoforge:bucket", "neoforge:mb"), 1e-9); assertEquals(1, Units.convert(1, "neoforge:mb", "l"), 1e-9); }
    @Test void dropletsEightyOne() { assertEquals(1, Units.convert(81, "fabric:droplet", "l"), 1e-9); }
    @Test void mekanism() { assertEquals(25, Units.convert(10, "mekanism:joule", "neoforge:fe"), 1e-9); }
    @Test void euAndAe() { assertEquals(4, Units.convert(1, "ic2:eu", "neoforge:fe"), 1e-9); assertEquals(2, Units.convert(1, "ae2:ae", "j"), 1e-9); }
    @Test void mana() { assertEquals(100, Units.convert(1, "botania:mana", "j"), 1e-9); }
    @Test void powerPerTick() { assertEquals(20, Units.convert(1, "neoforge:fe_per_tick", "w"), 1e-9); }
    @Test void celsius() { assertEquals(293.15, Units.convert(20, "c", "k"), 1e-9); assertEquals(100, Units.convert(373.15, "k", "c"), 1e-9); }
    @Test void pressure() { assertEquals(1.013, Units.convert(101.3, "kpa", "bar"), 1e-9); }
    @Test void mixedDimensionsRefused() { assertThrows(IllegalArgumentException.class, () -> Units.convert(1, "j", "l")); }
    @Test void roundsDown() { assertEquals(0, Units.fromCanonicalFloor(1, "ae2:ae")); assertEquals(5, Units.fromCanonicalFloor(11, "ae2:ae")); }
    @Test void format() { assertEquals("1.5 MJ", Fmt.energy(1_500_000)); assertEquals("2 m³", Fmt.volume(2000)); assertEquals("20 °C", Fmt.temperature(293.15)); }
}
