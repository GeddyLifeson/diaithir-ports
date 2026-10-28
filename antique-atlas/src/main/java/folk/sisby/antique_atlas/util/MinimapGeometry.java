package folk.sisby.antique_atlas.util;

/**
 * Pure geometry for the circular minimap: the GUI cannot mask a texture to a circle,
 * so the map is drawn in horizontal bands, each scissored to a rectangle that lies inside the circle.
 */
public final class MinimapGeometry {
	private MinimapGeometry() {
	}

	/** Half the chord width of a circle of the given diameter, for the pixel row whose centre is at row + 0.5. */
	public static double halfWidth(int diameter, int row) {
		double r = diameter / 2.0;
		double dy = row + 0.5 - r;
		double v = r * r - dy * dy;
		return v <= 0 ? 0 : Math.sqrt(v);
	}

	/**
	 * Splits the circle into {@code bandCount} horizontal bands. Each entry is {x0, y0, x1, y1} relative to the
	 * square that contains the circle, with x-extent equal to the narrowest chord inside the band, so nothing leaves the circle.
	 */
	public static int[][] bands(int diameter, int bandCount) {
		int[][] out = new int[bandCount][];
		int n = 0;
		for (int i = 0; i < bandCount; i++) {
			int y0 = Math.round(i * diameter / (float) bandCount);
			int y1 = Math.round((i + 1) * diameter / (float) bandCount);
			if (y1 <= y0) continue;
			double narrow = Math.min(halfWidth(diameter, y0), halfWidth(diameter, y1 - 1));
			int half = (int) Math.floor(narrow);
			int cx = diameter / 2;
			out[n++] = new int[]{cx - half, y0, cx + half, y1};
		}
		int[][] trimmed = new int[n][];
		System.arraycopy(out, 0, trimmed, 0, n);
		return trimmed;
	}

	/** Horizontal extent {left, right} (right exclusive) of the circle in a pixel row, or null if the row is empty. */
	public static int[] rowSpan(int diameter, int row) {
		double hw = halfWidth(diameter, row);
		if (hw <= 0) return null;
		int cx = diameter / 2;
		return new int[]{(int) Math.round(cx - hw), (int) Math.round(cx + hw)};
	}
}
