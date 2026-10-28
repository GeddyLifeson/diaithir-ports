package folk.sisby.antique_atlas.gui;

import folk.sisby.antique_atlas.WorldAtlasData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * An {@link AtlasRenderer} that is not a screen: it centres the map on the player inside a fixed square of the HUD,
 * so the tile drawing of the atlas can be reused for the minimap.
 */
public class MinimapRenderer implements AtlasRenderer {
	private final Player player;
	private final WorldAtlasData data;
	private final ResourceKey<Level> dim;
	private final int x;
	private final int y;
	private final int size;
	private final int tilePixels;
	private final double guiScale;
	private final int mapScale;

	public MinimapRenderer(Player player, WorldAtlasData data, ResourceKey<Level> dim, int x, int y, int size, int tilePixels, double guiScale) {
		this.player = player;
		this.data = data;
		this.dim = dim;
		this.x = x;
		this.y = y;
		this.size = size;
		this.tilePixels = tilePixels;
		this.guiScale = guiScale;
		this.mapScale = Math.max(1, (int) guiScale); // 1 gui pixel per source pixel
	}

	@Override public int bookX() { return x - MAP_BORDER_WIDTH; }
	@Override public int bookY() { return y - MAP_BORDER_HEIGHT; }
	@Override public int bookWidth() { return size + 2 * MAP_BORDER_WIDTH; }
	@Override public int bookHeight() { return size + 2 * MAP_BORDER_HEIGHT; }
	@Override public int mapWidth() { return size; }
	@Override public int mapHeight() { return size; }
	@Override public double mapOffsetX() { return -player.getX() * getPixelsPerBlock(); }
	@Override public double mapOffsetY() { return -player.getZ() * getPixelsPerBlock(); }
	@Override public int tilePixels() { return tilePixels; }
	@Override public int tileChunks() { return 1; }
	@Override public int mapScale() { return mapScale; }
	@Override public Player player() { return player; }
	@Override public WorldAtlasData worldAtlasData() { return data; }
	@Override public double getPixelsPerBlock() { return (mapScale / guiScale) * tilePixels / 16.0; }
	@Override public double guiScale() { return guiScale; }
	@Override public ResourceKey<Level> dim() { return dim; }
}
