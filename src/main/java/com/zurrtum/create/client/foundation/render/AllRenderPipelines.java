package com.zurrtum.create.client.foundation.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zurrtum.create.client.catnip.render.PonderRenderPipelines;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllRenderPipelines {
    // Own pipelines reuse the vanilla entity shader, so the pack ships no custom shader sources.
    public static final RenderPipeline.Snippet GLOWING_SNIPPET = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET).buildSnippet();
    public static final RenderPipeline ADDITIVE = register(
        "additive",
        RenderPipeline.builder(RenderPipelines.BLOCK_SNIPPET)
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE)).withCull(false)
    );
    public static final RenderPipeline ADDITIVE2 = register(
        "additive2",
        RenderPipeline.builder(RenderPipelines.BLOCK_SNIPPET)
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE)).withCull(false)
            .withDepthStencilState(PonderRenderPipelines.DEFAULT_TEST_NOT_WRITE)
    );
    public static final RenderPipeline GLOWING = register(
        "glowing", RenderPipeline.builder(GLOWING_SNIPPET).withColorTargetState(new ColorTargetState(new BlendFunction(
            SourceFactor.SRC_ALPHA,
            DestFactor.ONE_MINUS_SRC_ALPHA,
            SourceFactor.SRC_ALPHA,
            DestFactor.ONE_MINUS_SRC_ALPHA
        )))
    );
    public static final RenderPipeline GLOWING_TRANSLUCENT = register(
        "glowing_translucent",
        RenderPipeline.builder(GLOWING_SNIPPET).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
    );
    public static final RenderPipeline CUBE = register(
        "cube",
        RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
            .withColorTargetState(new ColorTargetState(new BlendFunction(
                SourceFactor.SRC_ALPHA,
                DestFactor.ONE_MINUS_SRC_ALPHA,
                SourceFactor.SRC_ALPHA,
                DestFactor.ONE_MINUS_SRC_ALPHA
            ))).withDepthStencilState(PonderRenderPipelines.DEFAULT_TEST_NOT_WRITE)
    );

    private static RenderPipeline register(String id, RenderPipeline.Builder builder) {
        Identifier location = Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/" + id);
        RenderPipeline pipeline = builder.withLocation(location).build();
        RenderPipelines.PIPELINES_BY_LOCATION.put(location, pipeline);
        return pipeline;
    }
}
