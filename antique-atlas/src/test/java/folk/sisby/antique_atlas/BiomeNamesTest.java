package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.util.BiomeNames;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BiomeNamesTest {
	@Test
	void prettifies() {
		assertEquals("Dark Forest", BiomeNames.prettify("dark_forest"));
		assertEquals("Village", BiomeNames.prettify("village"));
		assertEquals("Plains Village", BiomeNames.prettify("village/plains_village"));
	}

	@Test
	void keysAndFallback() {
		assertEquals("biome.minecraft.dark_forest", BiomeNames.key("biome", "minecraft", "dark_forest"));
		assertEquals("structure.a.b.c", BiomeNames.key("structure", "a", "b/c"));
		assertEquals("Dark Forest", BiomeNames.name("biome", "bop", "dark_forest", k -> false, k -> "x"));
		assertEquals("Forêt", BiomeNames.name("biome", "bop", "dark_forest", k -> true, k -> "Forêt"));
	}
}
