package folk.sisby.antique_atlas.reloader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Data-driven fallback for biomes that have no tile provider (added for the NeoForge port).
 * Rules come from {@code assets/antique_atlas/atlas/biome_fallbacks.json} in every resource pack, in order.
 * If no rule matches, the biome's climate (temperature, downfall, snow) picks a vanilla look-alike,
 * so biomes from any mod still draw something sensible.
 * Kept free of Minecraft types so it can be unit tested.
 */
public final class BiomeFallbacks {
	public record Rule(List<String> any, List<String> andAny, String biome) {
		public boolean matches(Predicate<String> hasTag) {
			return any.stream().anyMatch(hasTag) && (andAny.isEmpty() || andAny.stream().anyMatch(hasTag));
		}
	}

	private final List<Rule> rules = new ArrayList<>();

	public void clear() {
		rules.clear();
	}

	public List<Rule> rules() {
		return rules;
	}

	/** Reads one biome_fallbacks.json. Packs later in the stack append their rules after earlier ones unless they set "replace". */
	public void read(JsonObject json) {
		if (json.has("replace") && json.get("replace").getAsBoolean()) rules.clear();
		JsonArray array = json.getAsJsonArray("rules");
		if (array == null) return;
		List<Rule> parsed = new ArrayList<>();
		for (JsonElement element : array) {
			JsonObject o = element.getAsJsonObject();
			parsed.add(new Rule(strings(o.get("any")), strings(o.get("and_any")), o.get("biome").getAsString()));
		}
		// Packs on top of the stack come last in the resource stack; give their rules priority.
		rules.addAll(0, parsed);
	}

	private static List<String> strings(JsonElement element) {
		List<String> list = new ArrayList<>();
		if (element instanceof JsonArray a) a.forEach(e -> list.add(e.getAsString()));
		else if (element != null) list.add(element.getAsString());
		return list;
	}

	/** @return the first matching rule's biome id, or null. */
	public String byTags(Predicate<String> hasTag) {
		for (Rule rule : rules) {
			if (rule.matches(hasTag)) return rule.biome();
		}
		return null;
	}

	/**
	 * Last resort for biomes in no known tag: pick a vanilla biome with a similar climate.
	 * Temperatures and downfall follow the vanilla scale (plains 0.8/0.4, desert 2.0/0.0, snowy plains 0.0/0.5, jungle 0.95/0.9).
	 */
	public static String byClimate(float temperature, float downfall, boolean snows) {
		if (snows || temperature < 0.15F) return downfall > 0.6F ? "minecraft:snowy_taiga" : "minecraft:snowy_plains";
		if (temperature >= 1.5F) return downfall < 0.25F ? "minecraft:desert" : "minecraft:savanna";
		if (temperature >= 1.0F) return downfall < 0.25F ? "minecraft:badlands" : downfall > 0.7F ? "minecraft:jungle" : "minecraft:savanna";
		if (downfall >= 0.85F) return temperature >= 0.9F ? "minecraft:jungle" : temperature >= 0.75F ? "minecraft:swamp" : "minecraft:dark_forest";
		if (downfall >= 0.6F) return temperature < 0.4F ? "minecraft:taiga" : "minecraft:forest";
		if (temperature < 0.4F) return "minecraft:taiga";
		return "minecraft:plains";
	}
}
