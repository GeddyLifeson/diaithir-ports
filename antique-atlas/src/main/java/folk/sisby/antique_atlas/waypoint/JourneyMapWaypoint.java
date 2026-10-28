package folk.sisby.antique_atlas.waypoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * One waypoint from a JourneyMap data file ({@code journeymap/data/&lt;sp|mp&gt;/&lt;world&gt;/waypoints/*.json}).
 * Only the plain JSON fields are read: name, x, y, z, r, g, b, enable, type and dimensions.
 */
public record JourneyMapWaypoint(String name, int x, int y, int z, int rgb, boolean enabled, boolean death, List<String> dimensions) {
	public static Optional<JourneyMapWaypoint> parse(String json) {
		try {
			JsonElement root = JsonParser.parseString(json);
			if (!root.isJsonObject()) return Optional.empty();
			JsonObject o = root.getAsJsonObject();
			if (!o.has("x") || !o.has("z")) return Optional.empty();
			String name = o.has("name") ? o.get("name").getAsString() : "Waypoint";
			int y = o.has("y") ? o.get("y").getAsInt() : 64;
			int r = o.has("r") ? o.get("r").getAsInt() : 255;
			int g = o.has("g") ? o.get("g").getAsInt() : 255;
			int b = o.has("b") ? o.get("b").getAsInt() : 255;
			boolean enabled = !o.has("enable") || o.get("enable").getAsBoolean();
			boolean death = o.has("type") && o.get("type").getAsString().equalsIgnoreCase("Death");
			List<String> dims = new ArrayList<>();
			if (o.has("dimensions") && o.get("dimensions").isJsonArray()) {
				JsonArray arr = o.getAsJsonArray("dimensions");
				for (JsonElement e : arr) dims.add(e.getAsString());
			}
			if (dims.isEmpty()) dims.add("minecraft:overworld");
			int rgb = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
			return Optional.of(new JourneyMapWaypoint(name, o.get("x").getAsInt(), y, o.get("z").getAsInt(), rgb, enabled, death, dims));
		} catch (RuntimeException e) {
			return Optional.empty();
		}
	}
}
