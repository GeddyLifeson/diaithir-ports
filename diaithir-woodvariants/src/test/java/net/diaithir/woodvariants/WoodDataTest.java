package net.diaithir.woodvariants;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WoodDataTest {
    private static final String[] SUFFIXES = {"chair", "stool", "bench", "table", "wall", "vertical_planks", "framed_planks"};

    private static boolean exists(String path) {
        return WoodDataTest.class.getResource(path) != null;
    }

    @Test
    void everyWoodRowHasAllGeneratedFiles() throws Exception {
        List<WoodDef> woods;
        try (InputStream in = WoodDataTest.class.getResourceAsStream("/data/diaithirwoodvariants/woods.json")) {
            woods = WoodDef.parse(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        assertTrue(woods.size() >= 18);
        String lang;
        try (InputStream in = WoodDataTest.class.getResourceAsStream("/assets/diaithirwoodvariants/lang/en_us.json")) {
            lang = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        for (WoodDef w : woods) {
            for (String s : SUFFIXES) {
                String n = w.id() + "_" + s;
                assertTrue(exists("/assets/diaithirwoodvariants/blockstates/" + n + ".json"), "blockstate " + n);
                assertTrue(exists("/assets/diaithirwoodvariants/items/" + n + ".json"), "item " + n);
                assertTrue(exists("/data/diaithirwoodvariants/loot_table/blocks/" + n + ".json"), "loot " + n);
                assertTrue(exists("/data/diaithirwoodvariants/recipe/" + n + ".json"), "recipe " + n);
                assertTrue(lang.contains("\"block.diaithirwoodvariants." + n + "\""), "lang " + n);
            }
            assertTrue(w.planks().startsWith(w.mod() + ":"), w.id());
        }
    }

    @Test
    void rejectsDuplicatesAndMissingFields() {
        String row = "{\"id\":\"a\",\"mod\":\"m\",\"name\":\"A\",\"gen\":\"a\",\"planks\":\"m:p\",\"log\":\"m:l\","
            + "\"planks_texture\":\"m:block/p\",\"log_texture\":\"m:block/l\",\"log_top_texture\":\"m:block/lt\"}";
        assertEquals(1, WoodDef.parse(new StringReader("{\"woods\":[" + row + "]}")).size());
        assertThrows(IllegalArgumentException.class, () -> WoodDef.parse(new StringReader("{\"woods\":[" + row + "," + row + "]}")));
        assertThrows(IllegalArgumentException.class, () -> WoodDef.parse(new StringReader("{\"woods\":[{\"id\":\"a\"}]}")));
    }

    @Test
    void rotationKeepsBoxesInsideTheBlockAndMapsNorthBackToSouth() {
        int[] back = Box.b(2, 9, 12, 14, 16, 14);
        assertArrayEquals(new int[] {2, 9, 12, 14, 16, 14}, Box.rotate(back, 0));
        assertArrayEquals(new int[] {2, 9, 2, 4, 16, 14}, Box.rotate(back, 1));
        assertArrayEquals(new int[] {2, 9, 2, 14, 16, 4}, Box.rotate(back, 2));
        assertArrayEquals(new int[] {12, 9, 2, 14, 16, 14}, Box.rotate(back, 3));
    }
}
