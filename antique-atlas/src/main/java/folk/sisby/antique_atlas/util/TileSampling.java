package folk.sisby.antique_atlas.util;

/**
 * Subtile sampling maths for tile sheets. A sheet is a 4 by 6 grid of subtiles, so a 32x48 sheet has
 * 8 pixel subtiles and a 64x96 sheet has 16 pixel ones; both draw identically.
 */
public final class TileSampling {
	public static final int COLUMNS = 4;
	public static final int ROWS = 6;
	public static final int DEFAULT_WIDTH = 32;

	private TileSampling() {
	}

	/** Size in pixels of one subtile in a sheet of the given width. */
	public static int unit(int sheetWidth) {
		return Math.max(1, sheetWidth / COLUMNS);
	}

	/** Sheet height implied by its width. */
	public static int height(int sheetWidth) {
		return unit(sheetWidth) * ROWS;
	}

	/** Source pixel offset of a subtile index (the old code's u or v, in subtiles). */
	public static int offset(int subtileIndex, int sheetWidth) {
		return subtileIndex * unit(sheetWidth);
	}

	/** Reads the width and height from a PNG header, or null if it is not a PNG. */
	public static int[] pngSize(byte[] header) {
		if (header.length < 24 || header[1] != 'P' || header[2] != 'N' || header[3] != 'G') return null;
		return new int[]{readInt(header, 16), readInt(header, 20)};
	}

	private static int readInt(byte[] b, int at) {
		return (b[at] & 0xFF) << 24 | (b[at + 1] & 0xFF) << 16 | (b[at + 2] & 0xFF) << 8 | (b[at + 3] & 0xFF);
	}
}
