package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.waypoint.JourneyMapWaypoint;
import folk.sisby.antique_atlas.waypoint.WaypointColors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaypointLogicTest {
	@Test
	void parsesDyeNamesAndHex() {
		assertEquals(0xFFB02E26, WaypointColors.parse("red").orElseThrow());
		assertEquals(0xFF3AB3DA, WaypointColors.parse("Light Blue").orElseThrow());
		assertEquals(0xFF112233, WaypointColors.parse("#112233").orElseThrow());
		assertEquals(0xFFABCDEF, WaypointColors.parse("abcdef").orElseThrow());
		assertTrue(WaypointColors.parse("notacolour").isEmpty());
		assertTrue(WaypointColors.parse("#12345").isEmpty());
	}

	@Test
	void idSegmentUsesDyeNameWhenExact() {
		assertEquals("custom/point/red/10/-20", WaypointColors.idPath("point", 0xFFB02E26, 10, -20));
		assertEquals("custom/skull/hex_112233/0/0", WaypointColors.idPath("skull", 0xFF112233, 0, 0));
	}

	@Test
	void nearestDye() {
		assertEquals(WaypointColors.DYE_NAMES.indexOf("red"), WaypointColors.nearestDye(0xFF0000));
		assertEquals(WaypointColors.DYE_NAMES.indexOf("black"), WaypointColors.nearestDye(0x000000));
	}

	@Test
	void parsesJourneyMapFile() {
		String json = "{\"id\":\"Home_10,64,-20\",\"name\":\"Home\",\"icon\":\"waypoint-normal.png\",\"x\":10,\"y\":64,\"z\":-20,\"r\":255,\"g\":0,\"b\":0,\"enable\":true,\"type\":\"Normal\",\"origin\":\"JourneyMap\",\"dimensions\":[\"minecraft:overworld\",\"minecraft:the_nether\"]}";
		JourneyMapWaypoint w = JourneyMapWaypoint.parse(json).orElseThrow();
		assertEquals("Home", w.name());
		assertEquals(10, w.x());
		assertEquals(-20, w.z());
		assertEquals(0xFF0000, w.rgb());
		assertTrue(w.enabled());
		assertFalse(w.death());
		assertEquals(2, w.dimensions().size());
	}

	@Test
	void journeyMapDeathAndBrokenFiles() {
		assertTrue(JourneyMapWaypoint.parse("{\"name\":\"Died\",\"x\":1,\"z\":2,\"type\":\"Death\"}").orElseThrow().death());
		assertEquals("minecraft:overworld", JourneyMapWaypoint.parse("{\"x\":1,\"z\":2}").orElseThrow().dimensions().get(0));
		assertTrue(JourneyMapWaypoint.parse("not json").isEmpty());
		assertTrue(JourneyMapWaypoint.parse("[1,2]").isEmpty());
		assertTrue(JourneyMapWaypoint.parse("{\"name\":\"x\"}").isEmpty());
	}
}
