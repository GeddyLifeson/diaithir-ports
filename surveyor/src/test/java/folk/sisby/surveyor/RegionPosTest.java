package folk.sisby.surveyor;

import folk.sisby.surveyor.util.RegionPos;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionPosTest {
	@Test
	void chunkRegionConversionHandlesNegatives() {
		assertEquals(0, RegionPos.chunkToRegion(31));
		assertEquals(1, RegionPos.chunkToRegion(32));
		assertEquals(-1, RegionPos.chunkToRegion(-1));
		assertEquals(-1, RegionPos.chunkToRegion(-32));
		assertEquals(-2, RegionPos.chunkToRegion(-33));
		assertEquals(31, RegionPos.regionRelative(-1));
		assertEquals(0, RegionPos.regionRelative(32));
	}

	@Test
	void bitRoundTrip() {
		for (int x = 0; x < 32; x++) {
			for (int z = 0; z < 32; z++) {
				int bit = RegionPos.chunkToBit(x, z);
				assertEquals(x, RegionPos.bitToX(bit));
				assertEquals(z, RegionPos.bitToZ(bit));
			}
		}
	}

	@Test
	void stringRoundTrip() {
		for (int x : new int[]{0, 1, -1, 12345, -98765}) {
			for (int z : new int[]{0, 7, -7, 54321, -2}) {
				RegionPos pos = new RegionPos(x, z);
				assertEquals(pos, RegionPos.of(pos.toString()));
			}
		}
	}

	@Test
	void blockSizesAgree() {
		assertEquals(512, RegionPos.BLOCK_SIZE);
		assertEquals(32, RegionPos.CHUNK_SIZE);
		assertTrue(RegionPos.regionToBlock(1) == RegionPos.regionToChunk(1) * 16);
	}
}
