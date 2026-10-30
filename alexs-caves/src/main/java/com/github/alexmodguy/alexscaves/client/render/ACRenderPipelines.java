package com.github.alexmodguy.alexscaves.client.render;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * 26.1 render pipelines for the upstream custom core shaders (irradiated, bubbled, ferrouslime gel,
 * purple witch, red ghost). One entity-format vertex/fragment pair with defines selects the effect.
 */
public class ACRenderPipelines {

    private static final Identifier SHADER = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "core/ac_entity");

    private static RenderPipeline.Builder base(String name, RenderPipeline.Snippet snippet) {
        return RenderPipeline.builder(snippet, RenderPipelines.GLOBALS_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "pipeline/" + name))
                .withVertexShader(SHADER)
                .withFragmentShader(SHADER);
    }

    private static final DepthStencilState NO_DEPTH_WRITE = new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false);

    public static final RenderPipeline IRRADIATED = base("irradiated", RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
            .withShaderDefine("IRRADIATED").withShaderDefine("NO_OVERLAY").withShaderDefine("NO_CARDINAL_LIGHTING")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false).withDepthStencilState(NO_DEPTH_WRITE).build();

    public static final RenderPipeline BLUE_IRRADIATED = base("blue_irradiated", RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
            .withShaderDefine("BLUE_IRRADIATED").withShaderDefine("NO_OVERLAY").withShaderDefine("NO_CARDINAL_LIGHTING")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false).withDepthStencilState(NO_DEPTH_WRITE).build();

    public static final RenderPipeline PURPLE_WITCH = base("purple_witch", RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
            .withShaderDefine("PURPLE_WITCH").withShaderDefine("NO_OVERLAY").withShaderDefine("NO_CARDINAL_LIGHTING")
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .withCull(false).withDepthStencilState(NO_DEPTH_WRITE).build();

    public static final RenderPipeline RED_GHOST = base("red_ghost", RenderPipelines.ENTITY_SNIPPET)
            .withShaderDefine("RED_GHOST").withShaderDefine("ALPHA_CUTOUT", 0.1F).withSampler("Sampler1")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false).build();

    public static final RenderPipeline BUBBLED = base("bubbled", RenderPipelines.ENTITY_SNIPPET)
            .withShaderDefine("WOBBLE_BUBBLE").withShaderDefine("ALPHA_CUTOUT", 0.1F).withShaderDefine("PER_FACE_LIGHTING").withSampler("Sampler1")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .build();

    public static final RenderPipeline BUBBLED_NO_CULL = base("bubbled_no_cull", RenderPipelines.ENTITY_SNIPPET)
            .withShaderDefine("WOBBLE_BUBBLE").withShaderDefine("ALPHA_CUTOUT", 0.1F).withShaderDefine("PER_FACE_LIGHTING").withSampler("Sampler1")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false).build();

    public static final RenderPipeline GEL = base("gel", RenderPipelines.ENTITY_SNIPPET)
            .withShaderDefine("WOBBLE_GEL").withShaderDefine("ALPHA_CUTOUT", 0.1F).withShaderDefine("PER_FACE_LIGHTING").withSampler("Sampler1")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false).build();

    public static final RenderPipeline GEL_TRIANGLES = base("gel_triangles", RenderPipelines.ENTITY_SNIPPET)
            .withShaderDefine("WOBBLE_GEL").withShaderDefine("ALPHA_CUTOUT", 0.1F).withShaderDefine("PER_FACE_LIGHTING").withSampler("Sampler1")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.TRIANGLES)
            .withCull(false).build();

    public static void register(net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent event) {
        event.registerPipeline(IRRADIATED);
        event.registerPipeline(BLUE_IRRADIATED);
        event.registerPipeline(PURPLE_WITCH);
        event.registerPipeline(RED_GHOST);
        event.registerPipeline(BUBBLED);
        event.registerPipeline(BUBBLED_NO_CULL);
        event.registerPipeline(GEL);
        event.registerPipeline(GEL_TRIANGLES);
    }
}
