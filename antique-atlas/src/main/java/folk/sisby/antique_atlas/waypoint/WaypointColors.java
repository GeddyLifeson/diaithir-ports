package folk.sisby.antique_atlas.waypoint;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Pure helpers for named, coloured waypoints: colour parsing, nearest vanilla dye, and id slugs.
 * Kept free of Minecraft classes so it can be unit tested.
 */
public final class WaypointColors {
	/** Vanilla dye names with their RGB values (matches DyeColor texture diffuse colours). */
	public static final List<String> DYE_NAMES = List.of("white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black");
	public static final int[] DYE_RGB = {0xF9FFFE, 0xF9801D, 0xC74EBD, 0x3AB3DA, 0xFED83D, 0x80C71F, 0xF38BAA, 0x474F52, 0x9D9D97, 0x169C9C, 0x8932B8, 0x3C44AA, 0x835432, 0x5E7C16, 0xB02E26, 0x1D1D21};

	private WaypointColors() {
	}

	/** Parses a dye name ("light_blue"), "#rrggbb" or "rrggbb"; returns an opaque ARGB int. */
	public static Optional<Integer> parse(String text) {
		if (text == null) return Optional.empty();
		String s = text.trim().toLowerCase(Locale.ROOT);
		int dye = DYE_NAMES.indexOf(s.replace(' ', '_'));
		if (dye >= 0) return Optional.of(0xFF000000 | DYE_RGB[dye]);
		if (s.startsWith("#")) s = s.substring(1);
		if (s.length() == 6 && s.chars().allMatch(c -> Character.digit(c, 16) >= 0)) return Optional.of(0xFF000000 | Integer.parseInt(s, 16));
		return Optional.empty();
	}

	public static int nearestDye(int rgb) {
		int best = 0;
		long bestDist = Long.MAX_VALUE;
		for (int i = 0; i < DYE_RGB.length; i++) {
			long dr = ((rgb >> 16) & 0xFF) - ((DYE_RGB[i] >> 16) & 0xFF);
			long dg = ((rgb >> 8) & 0xFF) - ((DYE_RGB[i] >> 8) & 0xFF);
			long db = (rgb & 0xFF) - (DYE_RGB[i] & 0xFF);
			long d = dr * dr + dg * dg + db * db;
			if (d < bestDist) {
				bestDist = d;
				best = i;
			}
		}
		return best;
	}

	/** Colour segment used in the landmark id: the dye name when it is exact, else "hex_rrggbb". */
	public static String colorSegment(int argb) {
		int rgb = argb & 0xFFFFFF;
		for (int d = 0; d < DYE_RGB.length; d++) if (DYE_RGB[d] == rgb) return DYE_NAMES.get(d);
		return "hex_%06x".formatted(rgb);
	}

	/** Landmark id path in the shape the marker editor uses: custom/&lt;texture&gt;/&lt;colour&gt;/x/z. */
	public static String idPath(String texture, int argb, int x, int z) {
		return "custom/%s/%s/%d/%d".formatted(texture, colorSegment(argb), x, z);
	}
}
