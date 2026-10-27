package cn.leolezury.eternalstarlight.common.client;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class ESRenderType {
	public static final RenderPipeline ENTITY_TRANSLUCENT_ADDITIVE_GLOW_PIPELINE = RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
		.withLocation(EternalStarlight.id("pipeline/entity_translucent_additive_glow"))
		.withShaderDefine("ALPHA_CUTOUT", 0.1F)
		.withShaderDefine("PER_FACE_LIGHTING")
		.withSampler("Sampler1")
		.withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
		.withCull(false)
		.withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
		.build();

	public static final RenderPipeline ENTITY_TRANSLUCENT_NO_DEPTH_PIPELINE = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
		.withLocation(EternalStarlight.id("pipeline/entity_translucent_no_depth"))
		.withShaderDefine("ALPHA_CUTOUT", 0.1F)
		.withShaderDefine("PER_FACE_LIGHTING")
		.withSampler("Sampler1")
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withCull(false)
		.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
		.build();

	public static final RenderPipeline ENTITY_CUTOUT_GLOW_PIPELINE = RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
		.withLocation(EternalStarlight.id("pipeline/entity_cutout_glow"))
		.withShaderDefine("ALPHA_CUTOUT", 0.1F)
		.withShaderDefine("PER_FACE_LIGHTING")
		.withSampler("Sampler1")
		.withCull(false)
		.build();

	public static final Function<Identifier, RenderType> TRANSLUCENT_ADDITIVE_GLOW = Util.memoize(location ->
		RenderType.create(EternalStarlight.ID + ":entity_translucent_additive_glow", RenderSetup.builder(ENTITY_TRANSLUCENT_ADDITIVE_GLOW_PIPELINE)
			.withTexture("Sampler0", location)
			.useOverlay()
			.sortOnUpload()
			.createRenderSetup()));

	public static final Function<Identifier, RenderType> TRANSLUCENT_NO_DEPTH = Util.memoize(location ->
		RenderType.create(EternalStarlight.ID + ":entity_translucent_no_depth", RenderSetup.builder(ENTITY_TRANSLUCENT_NO_DEPTH_PIPELINE)
			.withTexture("Sampler0", location)
			.useLightmap()
			.useOverlay()
			.sortOnUpload()
			.createRenderSetup()));

	public static final Function<Identifier, RenderType> CUTOUT_GLOW = Util.memoize(location ->
		RenderType.create(EternalStarlight.ID + ":entity_cutout_glow", RenderSetup.builder(ENTITY_CUTOUT_GLOW_PIPELINE)
			.withTexture("Sampler0", location)
			.useOverlay()
			.createRenderSetup()));

	/** Position/colour quads, additive-ish blending. Used for the portal surface. */
	public static RenderType portal() {
		return RenderTypes.lightning();
	}

	/** Position/colour quads, additive-ish blending. Used for the eclipse disc. */
	public static RenderType eclipse() {
		return RenderTypes.lightning();
	}

	public static RenderType dragonRaysQuads() {
		return RenderTypes.lightning();
	}

	public static RenderType entityTranslucentAdditiveGlow(Identifier location) {
		return TRANSLUCENT_ADDITIVE_GLOW.apply(location);
	}

	public static RenderType entityTranslucentNoDepth(Identifier location) {
		return TRANSLUCENT_NO_DEPTH.apply(location);
	}

	public static RenderType entityCutoutGlow(Identifier location) {
		return CUTOUT_GLOW.apply(location);
	}
}
