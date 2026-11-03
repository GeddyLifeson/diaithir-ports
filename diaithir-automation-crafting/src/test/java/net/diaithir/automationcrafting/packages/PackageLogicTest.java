package net.diaithir.automationcrafting.packages;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PackageLogicTest {
    private static final String JSON = "{\"name\":\"Torch crate\",\"result\":{\"item\":\"minecraft:torch\",\"count\":256},"
            + "\"inputs\":[{\"item\":\"minecraft:coal\",\"count\":64},{\"item\":\"minecraft:stick\",\"count\":64}],"
            + "\"research\":{\"items\":[{\"item\":\"minecraft:redstone\",\"count\":16}],\"packs\":[{\"item\":\"a:b\",\"count\":2}],\"prereqs\":[\"a:b\"]}}";

    @Test
    void parsesAFullFile() {
        List<String> warns = new ArrayList<>();
        PackageDef d = PackageDef.parse("t:torch", JsonParser.parseString(JSON), warns::add);
        assertNotNull(d);
        assertEquals("Torch crate", d.name());
        assertEquals(256, d.result().count());
        assertEquals(2, d.inputs().size());
        assertEquals(List.of("a:b"), d.prereqs());
        assertEquals(2, d.researchPacks().get(0).count());
        assertTrue(warns.isEmpty());
    }

    @Test
    void rejectsAFileWithoutInputs() {
        List<String> warns = new ArrayList<>();
        assertNull(PackageDef.parse("t:x", JsonParser.parseString("{\"result\":{\"item\":\"minecraft:dirt\"}}"), warns::add));
        assertFalse(warns.isEmpty());
    }

    @Test
    void tallyTakesOnlyWhatIsMissing() {
        PackageDef d = PackageDef.parse("t:torch", JsonParser.parseString(JSON), m -> {});
        Tally t = new Tally(d.inputs());
        assertEquals(60, t.accept("minecraft:coal", 60));
        assertEquals(4, t.accept("minecraft:coal", 10));
        assertEquals(0, t.accept("minecraft:coal", 10));
        assertEquals(0, t.accept("minecraft:dirt", 5));
        assertFalse(t.done());
        assertEquals(64, t.accept("minecraft:stick", 100));
        assertTrue(t.done());
    }
}
