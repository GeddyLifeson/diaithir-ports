package mcjty.lib.client;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * 26.1 builds render types from pipelines. McJtyLib's own types map onto the nearest vanilla ones whose vertex
 * format is covered by what McJtyLib writes (26.1 skips extra vertex elements, but missing ones fail).
 */
// ponytail: the additive glow blends became plain translucent; a custom additive pipeline would restore the brighter look.
public class CustomRenderTypes {

    public static final RenderType TRANSLUCENT_ADD = RenderTypes.translucentMovingBlock();
    public static final RenderType TRANSLUCENT_ADD_NOLIGHTMAPS = TRANSLUCENT_ADD;
    public static final RenderType TRANSLUCENT_LIGHTNING_NOLIGHTMAPS = TRANSLUCENT_ADD;
    public static final RenderType OVERLAY_LINES = RenderTypes.lines();
    public static final RenderType QUADS_NOTEXTURE = RenderTypes.debugQuads();

    public static RenderType translucent() {
        return TRANSLUCENT_ADD;
    }
}
