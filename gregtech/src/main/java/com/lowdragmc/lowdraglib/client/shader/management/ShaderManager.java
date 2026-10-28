package com.lowdragmc.lowdraglib.client.shader.management;


import com.lowdragmc.lowdraglib.client.shader.uniform.IUniformCallback;
import com.lowdragmc.lowdraglib.utils.PositionedRect;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class ShaderManager {

	private static final ShaderManager INSTANCE = new ShaderManager();

	public static ShaderManager getInstance() {
		return INSTANCE;
	}

	public static boolean allowedShader() {
		return true;
	}

	private final Reference2ReferenceMap<Shader, ShaderProgram> programs;

	private ShaderManager() {
		this.programs = new Reference2ReferenceOpenHashMap<>();
	}

	private static TextureTarget TEMP_TARGET;
	public static TextureTarget getTempTarget() { return null; /* ported stub */ }

	public void reload() {
		programs.forEach((shader, shaderProgram) -> {
			shader.deleteShader();
			shaderProgram.delete();
		});
		programs.clear();
	}

	private PositionedRect viewPort;

	public void setViewPort(PositionedRect viewPort) {
		this.viewPort = viewPort;
	}

	public boolean hasViewPort() {
		return this.viewPort != null;
	}

	public void clearViewPort() {
		this.viewPort = null;
	}

	public RenderTarget renderFullImageInFramebuffer(RenderTarget fbo, Shader frag, IUniformCallback consumeCache, Consumer<ShaderProgram> programCreated) { return null; /* ported stub */ }

}
