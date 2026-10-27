package folk.sisby.antique_atlas;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class AntiqueAtlasKeybindings {
	public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(AntiqueAtlas.id("atlas"));
	public static final KeyMapping ATLAS_KEYMAPPING = new KeyMapping("key.antique_atlas.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, CATEGORY);

	public static void register(RegisterKeyMappingsEvent event) {
		event.registerCategory(CATEGORY);
		event.register(ATLAS_KEYMAPPING);
	}

	public static void onClientTick(ClientTickEvent.Post event) {
		while (ATLAS_KEYMAPPING.consumeClick()) AntiqueAtlas.openAtlasScreen();
	}
}
