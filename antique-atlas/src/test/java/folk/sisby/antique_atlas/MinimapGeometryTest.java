package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.util.MinimapGeometry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinimapGeometryTest {
	@Test
	void chordsAreSymmetricAndWidestInTheMiddle() {
		int d = 96;
		for (int row = 0; row < d / 2; row++) assertEquals(MinimapGeometry.halfWidth(d, row), MinimapGeometry.halfWidth(d, d - 1 - row), 1e-9);
		assertTrue(MinimapGeometry.halfWidth(d, d / 2) > 47.9);
		assertTrue(MinimapGeometry.halfWidth(d, 0) < 15);
	}

	@Test
	void bandsStayInsideTheCircleAndCoverTheHeight() {
		int d = 96;
		int[][] bands = MinimapGeometry.bands(d, 14);
		assertEquals(0, bands[0][1]);
		assertEquals(d, bands[bands.length - 1][3]);
		for (int[] b : bands) {
			assertTrue(b[0] >= 0 && b[2] <= d && b[0] <= b[2]);
			for (int row = b[1]; row < b[3]; row++) {
				double hw = MinimapGeometry.halfWidth(d, row);
				assertTrue(48 - b[0] <= hw + 1e-9, "band left edge outside circle at row " + row);
				assertTrue(b[2] - 48 <= hw + 1e-9, "band right edge outside circle at row " + row);
			}
		}
	}

	@Test
	void rowSpan() {
		int[] mid = MinimapGeometry.rowSpan(96, 48);
		assertTrue(mid[0] <= 1 && mid[1] >= 95);
		assertNull(MinimapGeometry.rowSpan(96, -1));
	}
}
