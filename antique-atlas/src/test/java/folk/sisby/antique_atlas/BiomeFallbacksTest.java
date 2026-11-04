package folk.sisby.antique_atlas;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import folk.sisby.antique_atlas.reloader.BiomeFallbacks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class BiomeFallbacksTest {
	static final BiomeFallbacks FALLBACKS = new BiomeFallbacks();

	@BeforeAll
	static void load() throws IOException {
		try (Reader reader = new InputStreamReader(BiomeFallbacksTest.class.getResourceAsStream("/assets/antique_atlas/atlas/biome_fallbacks.json"))) {
			FALLBACKS.read(JsonParser.parseReader(reader).getAsJsonObject());
		}
	}

	private static String pick(String... tags) {
		Set<String> set = Set.of(tags);
		return FALLBACKS.byTags(set::contains);
	}

	@Test
	void conventionalTagsPickVanillaLookAlikes() {
		assertEquals("minecraft:swamp", pick("c:is_swamp"));
		assertEquals("minecraft:jungle", pick("c:is_jungle"));
		assertEquals("minecraft:badlands", pick("c:is_badlands"));
		assertEquals("minecraft:desert", pick("c:is_desert"));
		assertEquals("minecraft:the_void", pick("c:is_void"));
	}

	@Test
	void specificRulesWinOverGeneralOnes() {
		assertEquals("minecraft:snowy_taiga", pick("c:is_taiga", "c:is_snowy"));
		assertEquals("minecraft:taiga", pick("c:is_taiga"));
		assertEquals("minecraft:frozen_river", pick("c:is_river", "c:is_icy"));
		assertEquals("minecraft:river", pick("c:is_river"));
		assertEquals("minecraft:end_highlands", pick("c:is_end", "c:is_dense_vegetation"));
		assertEquals("minecraft:end_barrens", pick("c:is_end"));
	}

	@Test
	void unknownTagsGiveNothingSoClimateTakesOver() {
		assertNull(pick("somemod:is_odd"));
	}

	@Test
	void climateFallbackCoversTheVanillaScale() {
		assertEquals("minecraft:desert", BiomeFallbacks.byClimate(2.0F, 0.0F, false));
		assertEquals("minecraft:snowy_plains", BiomeFallbacks.byClimate(0.0F, 0.5F, true));
		assertEquals("minecraft:snowy_taiga", BiomeFallbacks.byClimate(-0.5F, 0.9F, false));
		assertEquals("minecraft:jungle", BiomeFallbacks.byClimate(0.95F, 0.9F, false));
		assertEquals("minecraft:plains", BiomeFallbacks.byClimate(0.8F, 0.4F, false));
		assertEquals("minecraft:swamp", BiomeFallbacks.byClimate(0.8F, 0.9F, false));
		assertEquals("minecraft:savanna", BiomeFallbacks.byClimate(1.2F, 0.4F, false));
		assertEquals("minecraft:badlands", BiomeFallbacks.byClimate(1.2F, 0.1F, false));
	}

	@Test
	void laterPacksTakePriorityAndReplaceWorks() {
		BiomeFallbacks f = new BiomeFallbacks();
		f.read(JsonParser.parseString("{\"rules\":[{\"any\":[\"a:x\"],\"biome\":\"minecraft:plains\"}]}").getAsJsonObject());
		f.read(JsonParser.parseString("{\"rules\":[{\"any\":[\"a:x\"],\"biome\":\"minecraft:forest\"}]}").getAsJsonObject());
		assertEquals("minecraft:forest", f.byTags("a:x"::equals));
		f.read(JsonParser.parseString("{\"replace\":true,\"rules\":[]}").getAsJsonObject());
		assertNull(f.byTags("a:x"::equals));
	}

	@Test
	void everyFallbackTargetHasATileProviderFile() {
		for (BiomeFallbacks.Rule rule : FALLBACKS.rules()) {
			String[] id = rule.biome().split(":");
			String path = "/assets/" + id[0] + "/atlas/biome/" + id[1] + ".json";
			assertNotNull(BiomeFallbacksTest.class.getResource(path), "no tile provider for fallback target " + rule.biome() + " at " + path);
		}
	}
}
