package folk.sisby.antique_atlas;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server-side switches. This is a NeoForge SERVER config, so a dedicated server's value is synced to every client.
 */
public final class AntiqueAtlasServerConfig {
	public static final ModConfigSpec SPEC;
	private static final ModConfigSpec.BooleanValue MINIMAP_ENABLED;

	static {
		ModConfigSpec.Builder b = new ModConfigSpec.Builder();
		MINIMAP_ENABLED = b.comment("Whether the Compass Map item draws its minimap on the HUD. Set to false to keep the item but turn the minimap off for everyone.")
			.define("minimapEnabled", true);
		SPEC = b.build();
	}

	private AntiqueAtlasServerConfig() {
	}

	public static boolean minimapAllowed() {
		try {
			return SPEC.isLoaded() && MINIMAP_ENABLED.get();
		} catch (IllegalStateException e) {
			return true;
		}
	}
}
