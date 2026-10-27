package com.diaithir.addons.newage.client;

import java.util.function.Function;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class CNARenderTypes {
    private static final Function<Identifier, RenderType> WIRE = Util.memoize(texture -> RenderType.create(
            "create_new_age_wire",
            RenderSetup.builder(RenderPipelines.SOLID_BLOCK)
                    .withTexture("Sampler0", texture)
                    .useLightmap()
                    .createRenderSetup()));

    public static RenderType wire(Identifier texture) {
        return WIRE.apply(texture);
    }
}
