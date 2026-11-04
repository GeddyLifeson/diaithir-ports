package me.desht.pneumaticcraft.client.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * 26.1 port: the original defined custom composite render types. These map onto the closest vanilla render types
 * (custom pipelines for no-depth-test variants have not been recreated yet).
 */
public class ModRenderTypes {
    public static final RenderType UNTEXTURED_QUAD_NO_DEPTH = RenderTypes.debugQuads();
    public static final RenderType UNTEXTURED_QUAD = RenderTypes.debugQuads();
    public static final RenderType BLOCK_FRAME = RenderTypes.debugQuads();
    public static final RenderType BLOCK_TRACKER = RenderTypes.linesTranslucent();
    public static final RenderType TARGET_CIRCLE = RenderTypes.debugTriangleFan();
    public static final RenderType TRIANGLE_FAN = RenderTypes.debugTriangleFan();
    public static final RenderType TRIANGLES = RenderTypes.debugQuads();

    public static RenderType getTextureRender(Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    public static RenderType getTextureRenderColored(Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    public static RenderType getTextureRenderColored(Identifier texture, boolean disableDepthTest) {
        return RenderTypes.entityTranslucent(texture);
    }

    public static RenderType getLineLoops(double lineWidth) {
        return RenderTypes.lines();
    }

    public static RenderType getBlockHilightFace(boolean disableDepthTest, boolean disableWriteMask) {
        return RenderTypes.debugQuads();
    }

    public static RenderType getBlockHilightLine(boolean disableDepthTest, boolean disableWriteMask) {
        return RenderTypes.linesTranslucent();
    }

    public static RenderType getArmorTranslucentNoCull(Identifier rl) {
        return RenderTypes.armorTranslucent(rl);
    }
}
