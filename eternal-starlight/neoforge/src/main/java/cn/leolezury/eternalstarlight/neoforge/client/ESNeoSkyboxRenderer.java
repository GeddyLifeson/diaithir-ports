package cn.leolezury.eternalstarlight.neoforge.client;

import cn.leolezury.eternalstarlight.common.client.renderer.world.ESSkyRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;
import org.joml.Matrix4fc;

public class ESNeoSkyboxRenderer implements CustomSkyboxRenderer {
	@Override
	public boolean renderSky(LevelRenderState levelRenderState, SkyRenderState skyRenderState, Matrix4fc modelViewMatrix, Runnable setupFog) {
		return ESSkyRenderer.renderSky(levelRenderState, skyRenderState, modelViewMatrix, setupFog);
	}
}
