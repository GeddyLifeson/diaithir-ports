package de.ellpeck.actuallyadditions.mod.blocks.render;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class RenderTypes {
    private RenderTypes() {}

    // additive, untextured colour quads: vanilla's lightning pipeline, drawn to the main target
    public static final RenderType LASER = RenderType.create("actuallyadditions:laser",
        RenderSetup.builder(RenderPipelines.LIGHTNING).sortOnUpload().createRenderSetup());
}
