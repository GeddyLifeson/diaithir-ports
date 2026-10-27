package cn.leolezury.eternalstarlight.common.client.posteffect;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import org.joml.Matrix4f;

/**
 * The world space post effects (shockwaves, gravity wells...) were driven by legacy post chains with custom uniforms.
 * 26.1 post chains are fully data driven and cannot receive per-frame uniforms from code, so the effects are tracked
 * by {@link WorldPostEffectManager} but are not drawn.
 */
public class PostEffectRenderer {
	public static void render(ClientLevel level, Camera camera, Matrix4f viewMatrix, Matrix4f projectionMatrix, float partialTicks) {
		if (level == null) {
			WorldPostEffectManager.clear();
		}
	}

	public static void captureDepth() {
	}

	public static void reload() {
	}
}
