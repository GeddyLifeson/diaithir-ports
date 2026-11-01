package diaithir.myth;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SpawnRegionsTest {
    static final double SCALE = 5.0;

    /** One city (lat, lon) per court. */
    static final Map<String, double[]> CITY = Map.of(
            "hellenic", new double[] {37.98, 23.73},   // Athens
            "minoan", new double[] {35.30, 25.16},     // Knossos
            "italic", new double[] {41.90, 12.50},     // Rome
            "anatolian", new double[] {39.93, 32.86},  // Ankara
            "canaanite", new double[] {33.89, 35.50},  // Beirut
            "nilotic", new double[] {30.04, 31.24});   // Cairo

    static SpawnRegions load() throws Exception {
        SpawnRegions regions = new SpawnRegions();
        List<SpawnRegions.Region> all = new ArrayList<>();
        for (Creature c : Creature.all()) {
            try (var in = SpawnRegionsTest.class.getResourceAsStream("/data/diaithir_myth/spawn_regions/" + c.id + ".json")) {
                assertNotNull(in, "spawn region file for " + c.id);
                JsonElement json = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonElement.class);
                SpawnRegions.Region r = SpawnRegions.parse(c.id, json, msg -> fail(c.id + ": " + msg));
                assertEquals("diaithir_myth:" + c.id, r.creature());
                all.add(new SpawnRegions.Region(c.id, r.minAge(), r.maxAge(), r.boxes()));
            }
        }
        regions.replace(all);
        return regions;
    }

    @Test
    void projectionRoundTripAndLandmarks() {
        // 111319.4917 m per degree / 5 = 22263.9 blocks per degree
        assertEquals(22263.898, EarthGeo.blocksPerDegree(SCALE), 0.001);
        assertEquals(0.0, EarthGeo.latitude(0, SCALE), 1e-9);
        assertEquals(0.0, EarthGeo.longitude(0, 0, SCALE), 1e-9);
        for (double[] ll : new double[][] {{37.98, 23.73}, {-33.9, 151.2}, {64.1, -21.9}, {0.5, -179.0}}) {
            double x = EarthGeo.blockX(ll[1], SCALE), z = EarthGeo.blockZ(ll[0], SCALE);
            assertEquals(ll[0], EarthGeo.latitude(z, SCALE), 1e-6);
            assertEquals(ll[1], EarthGeo.longitude(x, z, SCALE), 1e-6);
        }
        assertTrue(EarthGeo.blockZ(40, SCALE) < 0, "north is negative Z");
        assertTrue(EarthGeo.blockX(10, SCALE) > 0, "east is positive X");
        // east of the antimeridian the longitude wraps
        double edge = EarthGeo.edgeBlocks(SCALE);
        assertEquals(-179.0, EarthGeo.longitude(edge + EarthGeo.blocksPerDegree(SCALE), 0, SCALE), 0.01);
    }

    @Test
    void everyCreatureCoversItsCourtCityAndAge() throws Exception {
        SpawnRegions regions = load();
        for (Creature c : Creature.all()) {
            double[] city = CITY.get(c.court);
            assertNotNull(city, c.court);
            assertTrue(regions.allowsPlace(c.id, city[0], city[1]), c.id + " court city");
            assertEquals(c.age, regions.get(c.id).minAge(), c.id + " min age");
            assertFalse(regions.allowsAge(c.id, c.age - 1), c.id + " too early");
            assertTrue(regions.allowsAge(c.id, c.age), c.id + " on time");
        }
    }

    @Test
    void courtRegionsExcludeFarPlaces() throws Exception {
        SpawnRegions regions = load();
        for (Creature c : Creature.all()) {
            assertFalse(regions.allowsPlace(c.id, 53.3, -6.3), c.id + " must not spawn in Dublin");
            assertFalse(regions.allowsPlace(c.id, -33.9, 151.2), c.id + " must not spawn in Sydney");
            assertFalse(regions.allowsPlace(c.id, 40.7, -74.0), c.id + " must not spawn in New York");
        }
        // block columns converted as the game does it
        double athensX = EarthGeo.blockX(23.73, SCALE), athensZ = EarthGeo.blockZ(37.98, SCALE);
        double cairoX = EarthGeo.blockX(31.24, SCALE), cairoZ = EarthGeo.blockZ(30.04, SCALE);
        double athensLat = EarthGeo.latitude(athensZ, SCALE), athensLon = EarthGeo.longitude(athensX, athensZ, SCALE);
        double cairoLat = EarthGeo.latitude(cairoZ, SCALE), cairoLon = EarthGeo.longitude(cairoX, cairoZ, SCALE);
        assertTrue(regions.allowsPlace("goburdoiri", athensLat, athensLon));
        assertFalse(regions.allowsPlace("goburdoiri", cairoLat, cairoLon));
        assertTrue(regions.allowsPlace("madadfasaigratad", cairoLat, cairoLon));
        assertFalse(regions.allowsPlace("madadfasaigratad", athensLat, athensLon));
        assertTrue(regions.allowsPlace("tarbiumachuoirtach", 35.2, 25.1), "Minotaur in Crete");
        assertFalse(regions.allowsPlace("tarbiumachuoirtach", 30.0, 31.0), "Minotaur not on the Nile");
    }

    @Test
    void ageWindowAndAntimeridianBox() {
        SpawnRegions r = new SpawnRegions();
        r.replace(List.of(new SpawnRegions.Region("a", 3, 5, List.of(new SpawnRegions.Box(-10, 10, 170, -170)))));
        assertTrue(r.allowsPlace("a", 0, 175));
        assertTrue(r.allowsPlace("a", 0, -175));
        assertFalse(r.allowsPlace("a", 0, 0));
        assertFalse(r.allowsAge("a", 2));
        assertTrue(r.allowsAge("a", 3));
        assertTrue(r.allowsAge("a", 5));
        assertFalse(r.allowsAge("a", 6));
        assertTrue(r.allowsPlace("unlisted", 0, 0));
        assertTrue(r.allowsAge("unlisted", 0));
        assertEquals(0, SpawnRegions.ageNumber("TELLING"));
        assertEquals(4, SpawnRegions.ageNumber("IV"));
        assertEquals(-1, SpawnRegions.ageNumber("nope"));
    }
}
