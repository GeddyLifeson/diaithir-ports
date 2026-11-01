package folk.sisby.antique_atlas;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import folk.sisby.antique_atlas.util.ColorUtil;
import folk.sisby.antique_atlas.util.MathUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MathAndDataTest {
	static final Path RES = Paths.get("src/main/resources");

	@Test
	void roundToBase() {
		assertEquals(0, MathUtil.roundToBase(7, 8));
		assertEquals(8, MathUtil.roundToBase(9, 8));
		assertEquals(0, MathUtil.roundToBase(-3, 8)); // truncates toward zero, as upstream
	}

	@Test
	void hashIsStableAndSpreads() {
		assertEquals(MathUtil.hash3(3, 4, 5), MathUtil.hash3(3, 4, 5));
		assertNotEquals(MathUtil.hash3(3, 4, 5), MathUtil.hash3(4, 3, 5));
	}

	@Test
	void colourComponents() {
		float[] c = ColorUtil.componentsFromRgb(0xFF8000);
		assertEquals(1.0F, c[0], 0.001F);
		assertEquals(0.5F, c[1], 0.01F);
		assertEquals(0.0F, c[2], 0.001F);
	}

	@Test
	void distanceSquared() {
		assertEquals(25.0, MathUtil.distanceToSqr(0, 0, 3, 4), 1e-9);
	}

	@Test
	void allDataJsonParses() throws IOException {
		try (Stream<Path> files = Files.walk(RES.resolve("assets"))) {
			List<Path> json = files.filter(p -> p.toString().endsWith(".json") && p.toString().contains("/atlas/")).collect(Collectors.toList());
			assertTrue(json.size() > 100, "expected the atlas data folder to hold many files, found " + json.size());
			for (Path p : json) JsonParser.parseString(Files.readString(p));
		}
	}

	@Test
	void everyBiomeProviderTextureExists() throws IOException {
		try (Stream<Path> files = Files.walk(RES.resolve("assets"))) {
			for (Path p : files.filter(f -> f.toString().endsWith(".json") && f.toString().contains("/atlas/biome/") && !f.toString().endsWith("biome_fallbacks.json")).collect(Collectors.toList())) {
				JsonObject o = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
				for (String tex : textureNames(o.get("textures"))) {
					String id = tex.contains(":") ? tex.substring(tex.indexOf(':') + 1) : tex;
					String ns = tex.contains(":") ? tex.substring(0, tex.indexOf(':')) : "antique_atlas";
					Path png = RES.resolve("assets/" + ns + "/textures/atlas/tile/" + id + ".png");
					assertTrue(Files.exists(png), p.getFileName() + " references missing tile texture " + tex);
				}
			}
		}
	}

	private static List<String> textureNames(JsonElement e) {
		if (e == null) return List.of();
		if (e.isJsonPrimitive()) return List.of(e.getAsString());
		if (e.isJsonArray()) return e.getAsJsonArray().asList().stream().map(JsonElement::getAsString).collect(Collectors.toList());
		List<String> names = new java.util.ArrayList<>();
		e.getAsJsonObject().entrySet().forEach(entry -> {
			if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isNumber()) names.add(entry.getKey()); // weighted: key is the texture
			else names.addAll(textureNames(entry.getValue())); // elevation keyed: value is the texture(s)
		});
		return names;
	}
}
