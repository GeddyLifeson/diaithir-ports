package net.diaithir.worldsys.economy;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TechBridgeTest {
    @Test
    void gateAgeIsOpensPlusTierMinusOne() {
        assertEquals("III", TechBridge.gateAge("III", 1));
        assertEquals("V", TechBridge.gateAge("III", 3));
        assertEquals("RODOS", TechBridge.gateAge("VII", 9));
        assertEquals("I", TechBridge.gateAge("nonsense", 2));
    }

    @Test
    void worksWithAndWithoutThePublishedFile() throws Exception {
        Path dir = Files.createTempDirectory("techbridge");
        TechBridge.init(dir);
        assertNull(TechBridge.ageOf("azilian"));
        assertTrue(TechBridge.known("azilian", "writing"));
        Files.createDirectories(dir.resolve("kubejs/data"));
        Files.writeString(dir.resolve("kubejs/data/tech_published.json"), "{\"eras\":{\"azilian\":\"III\"},\"known\":{\"azilian\":[\"fire\"]}}");
        TechBridge.init(dir);
        assertEquals("III", TechBridge.ageOf("azilian"));
        assertTrue(TechBridge.known("azilian", "fire"));
        assertFalse(TechBridge.known("azilian", "writing"));
        assertNull(TechBridge.ageOf("other"));
        assertTrue(TechBridge.known("other", "writing"));
    }
}
