package folk.sisby.antique_atlas.util;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Pixel sizes of tile sheets, read once from the PNG header so resource packs can use any scale. */
public final class TextureSizes {
	private static final Map<Identifier, int[]> CACHE = new ConcurrentHashMap<>();
	private static final int[] DEFAULT = {TileSampling.DEFAULT_WIDTH, TileSampling.DEFAULT_WIDTH * 3 / 2};

	private TextureSizes() {
	}

	public static void clear() {
		CACHE.clear();
	}

	public static int[] of(Identifier texture) {
		return CACHE.computeIfAbsent(texture, id -> {
			try (InputStream in = Minecraft.getInstance().getResourceManager().open(id)) {
				int[] size = TileSampling.pngSize(in.readNBytes(24));
				if (size != null && size[0] > 0 && size[1] > 0) return size;
			} catch (Exception ignored) {
			}
			return DEFAULT;
		});
	}
}
