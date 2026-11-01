package com.lowdragmc.lowdraglib.client.bakedpipeline;

import com.google.common.base.Preconditions;
import net.minecraft.util.Util;
import net.minecraft.util.LightCoordsUtil;


import java.util.Arrays;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote LDLibQuadTransformers
 */
public class LDLibQuadTransformers {

    // do nothing
    private static final IQuadTransformer EMPTY = (quad) -> {
    };

    // modify lightmap for emissive
    private static final IQuadTransformer[] EMISSIVE_TRANSFORMERS = Util.make(new IQuadTransformer[16], (array) ->
            Arrays.setAll(array, (i) -> applyingLightmap(LightCoordsUtil.pack(i, i)))
    );

    public static IQuadTransformer empty() { return null; /* ported stub */ }

    public static IQuadTransformer applyingLightmap(int lightmap) { return null; /* ported stub */ }

    public static IQuadTransformer settingEmissivity(int emissivity) { return null; /* ported stub */ }

    public static IQuadTransformer settingMaxEmissivity() { return null; /* ported stub */ }

    private LDLibQuadTransformers() {
    }
}
