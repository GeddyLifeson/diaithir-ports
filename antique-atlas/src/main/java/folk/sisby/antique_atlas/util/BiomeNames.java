package folk.sisby.antique_atlas.util;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Predicate;

/** Display names for biome, structure and dimension ids, with a readable fallback when no translation exists. */
public final class BiomeNames {
	private BiomeNames() {
	}

	/** "minecraft:dark_forest" to "Dark Forest". */
	public static String prettify(String path) {
		String last = path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
		return Arrays.stream(last.split("_")).filter(w -> !w.isEmpty()).map(w -> w.substring(0, 1).toUpperCase(Locale.ROOT) + w.substring(1)).reduce((a, b) -> a + " " + b).orElse(path);
	}

	/** Language key "kind.namespace.path" with slashes turned into dots, as vanilla does. */
	public static String key(String kind, String namespace, String path) {
		return kind + "." + namespace + "." + path.replace('/', '.');
	}

	/** The translated name if the key exists, else the prettified path. */
	public static String name(String kind, String namespace, String path, Predicate<String> hasKey, java.util.function.Function<String, String> translate) {
		String k = key(kind, namespace, path);
		return hasKey.test(k) ? translate.apply(k) : prettify(path);
	}
}
