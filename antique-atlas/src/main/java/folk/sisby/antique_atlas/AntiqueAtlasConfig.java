package folk.sisby.antique_atlas;

import folk.sisby.surveyor.config.lib.WrappedConfig;
import folk.sisby.surveyor.config.lib.Comment;
import folk.sisby.surveyor.config.lib.IntegerRange;
import folk.sisby.surveyor.config.lib.ValueMap;
import folk.sisby.surveyor.client.SurveyorClient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("CanBeFinal")
public class AntiqueAtlasConfig extends WrappedConfig {
	public enum GraveStyle {
		CAUSE,
		GRAVE,
		ITEMS,
		DIED,
		EUPHEMISMS
	}

	public enum FallbackHandling {
		TEST,
		MISSING,
		PLAINS,
		CRASH
	}

	public enum EmptyHandling {
		CLOUDS,
		EMPTY
	}

	@Comment("Whether to display the map in full-screen")
	@Comment("The background is slightly less stylish, but more tiles are shown at once")
	public boolean fullscreen = true;

	@Comment("Whether to show the minimap on the HUD (hidden while any screen is open or the GUI is hidden)")
	public boolean minimap = true;

	@Comment("Minimap corner of the screen. 0 = top right, 1 = top left, 2 = bottom right, 3 = bottom left")
	@IntegerRange(min = 0, max = 3)
	public int minimapCorner = 0;

	@Comment("Minimap diameter in GUI pixels")
	@IntegerRange(min = 48, max = 160)
	public int minimapSize = 96;

	@Comment("Minimap zoom: pixels per chunk before GUI scaling. 4 shows the most land, 16 the least")
	@IntegerRange(min = 4, max = 16)
	public int minimapChunkPixels = 8;

	@Comment("Whether to show the biome (or structure) name when hovering the map")
	public boolean biomeHover = true;

	@Comment("Whether the minimap shows a short title when you enter a new biome or dimension")
	@Comment("Off by default: Traveller's Titles already does this in the pack")
	public boolean minimapEntryTitle = false;

	@Comment("Whether to require an item to display the map")
	public boolean requireItem = false;

	@Comment("Whether to keep scale after closing the map")
	public boolean keepZoom = false;

	@Comment("Whether to keep offset after closing the map")
	public boolean keepOffset = false;

	@Comment("How to depict player death locations.")
	public GraveStyle graveStyle = GraveStyle.EUPHEMISMS;

	@Comment("The maximum number of chunks to represent as a tile, as a power of 2")
	@Comment("Effectively the 'minimum zoom'")
	@Comment("0: 1x1 chunk = 1 tile | 6: 64x64 chunks = 1 tile")
	@IntegerRange(min = 0, max = 6)
	public int maxTileChunks = 5;

	@Comment("The maximum size to render a tile at, as a power of 2 multiplier")
	@Comment("Effectively the 'maximum zoom'")
	@Comment("0: 1 tile = 16x16 | 3: 1 tile = 128x128")
	@IntegerRange(min = 0, max = 3)
	public int maxTilePixels = 1;

	@Comment("The effective GUI scale for tiles and markers - independent of the overall GUI scale.")
	@Comment("0 will match your GUI scale - pixels will be the same size as the background & buttons")
	@Comment("-1 will use half your GUI scale, rounding up.")
	@Comment("-2 will use half your GUI scale, rounding down.")
	@IntegerRange(min = -2, max = 10)
	public int mapScale = 0;

	@Comment("The maximum number of chunks to load onto the map per tick after entering a world")
	public int chunkTickLimit = 100;

	@Comment("How to handle biomes that aren't in any minecraft, conventional, or forge biome tags")
	public FallbackHandling fallbackFailHandling = FallbackHandling.MISSING;

	@Comment("How to display areas that aren't explored yet")
	public EmptyHandling emptyHandling = EmptyHandling.EMPTY;

	public Map<String, Boolean> structureMarkers = ValueMap.builder(true)
		.put("minecraft:type/end_city", false)
		.build();

	@Comment("Options to adjust map behaviour for custom or modified dimensions.")
	public Dimensions dimensions = new Dimensions();

	public static class Dimensions implements WrappedConfig.Section {
		@Comment("Cycle order and coordinate scales of each dimension.")
		@Comment("If not 0, the relative position of the player will be shown.")
		public Map<String, Integer> scales = ValueMap.builder(0)
			.put("minecraft:overworld", 8)
			.put("minecraft:the_nether", 1)
			.put("minecraft:the_end", 0)
			.build();

		public List<ResourceKey<Level>> getOrder(ClientPacketListener handler) {
			List<ResourceKey<Level>> dims = new ArrayList<>(SurveyorClient.getSummaries(handler).keySet().stream().sorted(Comparator.comparing(ResourceKey::toString)).toList());
			dims.removeIf(WorldAtlasData::isEmpty);
			scales.keySet().removeIf(v -> handler.levels().stream().noneMatch(d -> d.identifier().toString().equals(v)));
			dims.stream().filter(dim -> !scales.containsKey(dim.identifier().toString())).forEach(dim -> scales.put(dim.identifier().toString(), 0));
			return dims.stream().sorted(Comparator.comparing(dim -> scales.keySet().stream().toList().indexOf(dim.identifier().toString()))).toList();
		}

		public Map<ResourceKey<Level>, Integer> getScales(ClientPacketListener handler) {
			List<ResourceKey<Level>> dims = new ArrayList<>(SurveyorClient.getSummaries(handler).keySet().stream().sorted(Comparator.comparing(ResourceKey::toString)).toList());
			dims.removeIf(WorldAtlasData::isEmpty);
			scales.keySet().removeIf(v -> handler.levels().stream().noneMatch(d -> d.identifier().toString().equals(v)));
			dims.stream().filter(dim -> !scales.containsKey(dim.identifier().toString())).forEach(dim -> scales.put(dim.identifier().toString(), 0));
			return dims.stream().collect(Collectors.toMap(k -> k, k -> scales.get(k.identifier().toString())));
		}
	}
}
