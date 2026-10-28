package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.util.TileSampling;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TileSamplingTest {
	@Test
	void unitScalesWithSheetWidth() {
		assertEquals(8, TileSampling.unit(32));
		assertEquals(16, TileSampling.unit(64));
		assertEquals(48, TileSampling.height(32));
		assertEquals(96, TileSampling.height(64));
	}

	@Test
	void sameSubtileCoversTheSameFractionAtAnyScale() {
		for (int index = 0; index < 4; index++) {
			// fraction of the sheet width where the subtile starts must match
			assertEquals(TileSampling.offset(index, 32) / 32.0, TileSampling.offset(index, 64) / 64.0, 1e-9);
		}
		assertEquals(2, TileSampling.offset(1, 32) / 4);
	}

	@Test
	void readsPngHeaderSize() {
		byte[] h = new byte[24];
		h[1] = 'P'; h[2] = 'N'; h[3] = 'G';
		h[19] = 64; h[23] = 96;
		assertArrayEquals(new int[]{64, 96}, TileSampling.pngSize(h));
		assertNull(TileSampling.pngSize(new byte[24]));
	}

	@Test
	void shippedSheetsAreOnTheGrid() throws Exception {
		java.nio.file.Path dir = java.nio.file.Paths.get("src/main/resources/assets/antique_atlas/textures/atlas/tile");
		try (var files = java.nio.file.Files.walk(dir)) {
			for (java.nio.file.Path p : (Iterable<java.nio.file.Path>) files.filter(f -> f.toString().endsWith(".png") && !f.toString().contains("/structure/"))::iterator) {
				byte[] head = java.nio.file.Files.readAllBytes(p);
				int[] size = TileSampling.pngSize(java.util.Arrays.copyOf(head, 24));
				assertEquals(TileSampling.height(size[0]), size[1], p + " is not on the 4x6 grid");
			}
		}
	}
}
