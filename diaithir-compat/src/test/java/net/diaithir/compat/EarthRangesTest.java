package net.diaithir.compat;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EarthRangesTest {
    private static EarthRanges shipped() throws Exception {
        File dir = new File(System.getProperty("ranges.dir"));
        File[] files = dir.listFiles((d, n) -> n.endsWith(".json"));
        assertNotNull(files);
        assertTrue(files.length > 0);
        List<EarthRanges.Entry> all = new ArrayList<>();
        for (File f : files) {
            try (Reader r = new FileReader(f)) {
                List<String> errors = new ArrayList<>();
                all.addAll(EarthRanges.parse(JsonParser.parseReader(r), errors::add));
                assertTrue(errors.isEmpty(), f + ": " + errors);
            }
        }
        EarthRanges ranges = new EarthRanges();
        ranges.replace(all);
        return ranges;
    }

    @Test
    void boxContainsAndWraps() {
        EarthRanges.Box b = new EarthRanges.Box(10, 20, -30, 40);
        assertTrue(b.contains(15, 0));
        assertFalse(b.contains(25, 0));
        assertFalse(b.contains(15, 41));
        EarthRanges.Box wrap = new EarthRanges.Box(50, 65, 160, -120);
        assertTrue(wrap.contains(55, 170));
        assertTrue(wrap.contains(55, -150));
        assertFalse(wrap.contains(55, 0));
    }

    @Test
    void speciesNarrowsAndUnlistedEntitiesAreUnaffected() throws Exception {
        EarthRanges r = shipped();
        // American lion: North America yes, Europe no
        assertTrue(r.allows("untamedwilds:big_cat", 9, "american_lion", 41.9, -87.6));
        assertFalse(r.allows("untamedwilds:big_cat", 9, "american_lion", 52.5, 13.4));
        // cave lion: Europe and North America yes, Africa no
        assertTrue(r.allows("untamedwilds:big_cat", 0, "cave_lion", 52.5, 13.4));
        assertFalse(r.allows("untamedwilds:big_cat", 0, "cave_lion", -1.3, 36.8));
        // a species with no range of its own (an unlisted cat variant) and an unlisted entity are unaffected
        assertTrue(r.allows("untamedwilds:big_cat", 99, "some_unlisted_cat", -25, 135));
        assertTrue(r.allows("untamedwilds:tarantula", 0, "black", 0, 0));
        assertTrue(r.allows("minecraft:zombie", -1, null, 0, 0));
        // Steller's sea cow wraps the antimeridian
        assertTrue(r.allows("untamedwilds:manatee", 3, "steller", 57, 170));
        assertTrue(r.allows("untamedwilds:manatee", 3, "steller", 57, -150));
        assertFalse(r.allows("untamedwilds:manatee", 3, "steller", 57, 0));
    }

    @Test
    void entityWideRangeAppliesToVariantsWithoutTheirOwn() {
        EarthRanges r = new EarthRanges();
        r.replace(List.of(
                new EarthRanges.Entry("x:y", null, null, List.of(new EarthRanges.Box(0, 10, 0, 10))),
                new EarthRanges.Entry("x:y", 2, null, List.of(new EarthRanges.Box(50, 60, 50, 60)))));
        assertTrue(r.allows("x:y", 1, "a", 5, 5));
        assertFalse(r.allows("x:y", 1, "a", 55, 55));
        assertTrue(r.allows("x:y", 2, "b", 55, 55));
        assertFalse(r.allows("x:y", 2, "b", 5, 5));
    }

    @Test
    void emptyBoxesMeanNeverOnEarth() throws Exception {
        EarthRanges r = shipped();
        for (double[] ll : new double[][] {{41.9, -87.6}, {52.5, 13.4}, {-1.3, 36.8}, {0, 0}}) {
            assertFalse(r.allows("untamedwilds:bear", 3, "cave", ll[0], ll[1]));
            assertFalse(r.allows("untamedwilds:bear", 1, "blind", ll[0], ll[1]));
            assertFalse(r.allows("untamedwilds:big_cat", 4, "marsupial_lion", ll[0], ll[1]));
            assertFalse(r.allows("untamedwilds:hyena", 4, "shortface", ll[0], ll[1]));
            assertFalse(r.allows("untamedwilds:camel", 0, "bactrian", ll[0], ll[1]));
            assertFalse(r.allows("untamedwilds:camel", 1, "dromedary", ll[0], ll[1]));
        }
        // the wild Bactrian keeps its box, and an empty-box range does not affect other species of the entity
        assertTrue(r.allows("untamedwilds:camel", 2, "wild_bactrian", 42, 95));
        assertFalse(r.allows("untamedwilds:camel", 2, "wild_bactrian", 52.5, 13.4));
        assertTrue(r.allows("untamedwilds:bear", 2, "brown", 52.5, 13.4));
        EarthRanges only = new EarthRanges();
        only.replace(EarthRanges.parse(JsonParser.parseString("{\"entity\":\"a:b\",\"variant\":1,\"boxes\":[]}"), m -> fail(m)));
        assertFalse(only.allows("a:b", 1, null, 0, 0));
        assertTrue(only.allows("a:b", 2, null, 0, 0));
    }

    @Test
    void bodyPlanSpeciesAndVanillaHorseAndWolf() throws Exception {
        EarthRanges r = shipped();
        // woolly mammoth: Europe yes, Africa no; African elephant the other way round
        assertTrue(r.allows("untamedwilds:elephantid", 0, "woolly", 52.5, 13.4));
        assertFalse(r.allows("untamedwilds:elephantid", 0, "woolly", -1.3, 36.8));
        assertTrue(r.allows("untamedwilds:elephantid", 5, "african", -1.3, 36.8));
        assertFalse(r.allows("untamedwilds:elephantid", 5, "african", 52.5, 13.4));
        // vanilla horse and wolf: Europe yes, South America and Africa no
        assertTrue(r.allows("minecraft:horse", -1, null, 52.5, 13.4));
        assertFalse(r.allows("minecraft:horse", -1, null, -34.6, -58.4));
        assertFalse(r.allows("minecraft:horse", -1, null, -1.3, 36.8));
        assertTrue(r.allows("minecraft:wolf", -1, null, 60.0, 100.0));
        assertFalse(r.allows("minecraft:wolf", -1, null, -34.6, -58.4));
        // przewalski only on the Mongolian steppe
        assertTrue(r.allows("untamedwilds:equid", 1, "przewalski", 45.0, 100.0));
        assertFalse(r.allows("untamedwilds:equid", 1, "przewalski", 52.5, 13.4));
    }

    @Test
    void vanillaDomesticsNeverAndPigChickenGoatRestricted() throws Exception {
        EarthRanges r = shipped();
        for (String e : new String[] {"cow", "sheep", "mule", "donkey", "llama", "trader_llama", "camel", "cat"}) {
            assertFalse(r.allows("minecraft:" + e, -1, null, 52.5, 13.4), e);
            assertFalse(r.allows("minecraft:" + e, -1, null, 31.0, 40.0), e);
        }
        assertTrue(r.allows("minecraft:pig", -1, null, 52.5, 13.4));
        assertFalse(r.allows("minecraft:pig", -1, null, 41.9, -87.6));
        assertTrue(r.allows("minecraft:chicken", -1, null, 15.0, 100.0));
        assertFalse(r.allows("minecraft:chicken", -1, null, 52.5, 13.4));
        assertTrue(r.allows("minecraft:goat", -1, null, 46.0, 8.0));
        assertFalse(r.allows("minecraft:goat", -1, null, 41.9, -87.6));
        // unlisted vanilla animals stay unaffected
        assertTrue(r.allows("minecraft:rabbit", -1, null, 41.9, -87.6));
        assertTrue(r.allows("minecraft:fox", -1, null, 41.9, -87.6));
    }

    @Test
    void wildFloraRanges() throws Exception {
        EarthRanges r = shipped();
        String ns = "diaithircompat:";
        // wild cereals and pulses: SW Asia only (Gobekli Tepe region yes; Berlin, Chicago, Cairo-Nile delta west no)
        for (String c : new String[] {"wild_einkorn", "wild_emmer", "wild_barley", "wild_lentil", "wild_pea", "wild_fig"}) {
            assertTrue(r.allows(ns + c, -1, null, 37.2, 38.9), c);
            assertFalse(r.allows(ns + c, -1, null, 52.5, 13.4), c);
            assertFalse(r.allows(ns + c, -1, null, 41.9, -87.6), c);
            assertFalse(r.allows(ns + c, -1, null, -1.3, 36.8), c);
        }
        // the chickpea is southeast Anatolia only: Diyarbakir yes, Jordan Valley no
        assertTrue(r.allows(ns + "wild_chickpea", -1, null, 37.9, 40.2));
        assertFalse(r.allows(ns + "wild_chickpea", -1, null, 32.0, 35.5));
        // indicator plants of the CSV regions
        assertTrue(r.allows(ns + "dryas_octopetala", -1, null, 60.0, 10.0));
        assertFalse(r.allows(ns + "dryas_octopetala", -1, null, -30.0, 140.0));
        assertTrue(r.allows(ns + "betula_nana", -1, null, 65.0, 100.0));
        assertTrue(r.allows(ns + "empetrum_nigrum", -1, null, 50.0, -100.0));
        assertTrue(r.allows(ns + "calluna_vulgaris", -1, null, 55.0, -3.0));
        assertFalse(r.allows(ns + "calluna_vulgaris", -1, null, 41.9, -87.6));
        assertTrue(r.allows(ns + "cembra_pine", -1, null, 47.0, 10.0));
        assertFalse(r.allows(ns + "cembra_pine", -1, null, 41.9, -87.6));
    }

    @Test
    void everyFloraPlacedFeatureHasARangeFile() {
        File dir = new File(System.getProperty("ranges.dir"));
        File placed = new File(dir, "../../diaithircompat/worldgen/placed_feature");
        File[] features = placed.listFiles((d, n) -> n.endsWith(".json"));
        assertNotNull(features);
        assertTrue(features.length >= 17);
        for (File f : features) {
            String id = f.getName().replace(".json", "");
            assertTrue(new File(dir, "flora_" + id + ".json").exists(), id);
        }
    }

    @Test
    void badEntriesAreReportedAndSkipped() {
        List<String> errors = new ArrayList<>();
        List<EarthRanges.Entry> e = EarthRanges.parse(JsonParser.parseString("{\"ranges\":[{\"entity\":\"a:b\"},{\"entity\":\"a:c\",\"boxes\":[{\"lat\":[0,1],\"lon\":[0,1]}]}]}"), errors::add);
        assertEquals(1, e.size());
        assertEquals(1, errors.size());
    }

    /** Runs the real Tellus EarthProjection (if its classes are built) and checks natural-spawn decisions from block positions. */
    @Test
    void blockPositionsGoThroughTellusProjection() throws Exception {
        File classes = new File(System.getProperty("tellus.classes"));
        Assumptions.assumeTrue(new File(classes, "com/yucareux/tellus/worldgen/EarthProjection.class").exists(), "Tellus classes not built");
        try (URLClassLoader cl = new URLClassLoader(new URL[] {classes.toURI().toURL()}, getClass().getClassLoader())) {
            Class<?> proj = Class.forName(TellusBridge.PROJECTION, true, cl);
            TellusBridge bridge = TellusBridge.forProjection(proj);
            Method latToZ = proj.getMethod("latToBlockZ", double.class, double.class);
            Method lonToX = proj.getMethod("longitudeToBlockX", double.class, double.class);
            EarthRanges r = shipped();
            for (double scale : new double[] {1.0, 5.0, 30.0}) {
                double[] chicago = blockRoundTrip(bridge, lonToX, latToZ, 41.88, -87.63, scale);
                assertEquals(41.88, chicago[0], 1e-6);
                assertEquals(-87.63, chicago[1], 1e-6);
                double[] berlin = blockRoundTrip(bridge, lonToX, latToZ, 52.52, 13.40, scale);
                // American lion: allowed over Chicago's block, refused over Berlin's
                assertTrue(r.allows("untamedwilds:big_cat", 9, "american_lion", chicago[0], chicago[1]));
                assertFalse(r.allows("untamedwilds:big_cat", 9, "american_lion", berlin[0], berlin[1]));
                // wisent: Europe yes, Chicago no
                assertTrue(r.allows("untamedwilds:bison", 1, "wisent", berlin[0], berlin[1]));
                assertFalse(r.allows("untamedwilds:bison", 1, "wisent", chicago[0], chicago[1]));
            }
        }
    }

    private static double[] blockRoundTrip(TellusBridge b, Method lonToX, Method latToZ, double lat, double lon, double scale) throws Exception {
        double x = (double) lonToX.invoke(null, lon, scale);
        double z = (double) latToZ.invoke(null, lat, scale);
        return b.latLon(x, z, scale);
    }
}
