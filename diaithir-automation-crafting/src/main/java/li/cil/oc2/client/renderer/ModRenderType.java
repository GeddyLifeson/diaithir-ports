/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.renderer;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

// TODO(oc2-restore): RenderType can no longer be extended and custom render types are built from
//  RenderPipeline/RenderSetup in 26.1. Reduced to the closest vanilla render types for now; restore
//  dedicated pipelines (position_color_lightmap for cables, no-cull additive for unlit/overlay) later.
@OnlyIn(Dist.CLIENT)
public final class ModRenderType {
    public static RenderType getNetworkCable() {
        // TODO(oc2-restore): Original used POSITION_COLOR_LIGHTMAP, no texture, no transparency, no cull.
        return RenderTypes.lightning();
    }

    public static RenderType getProjectorLight() {
        return RenderTypes.lightning();
    }

    public static RenderType getUnlitBlock(final Identifier location) {
        // TODO(oc2-restore): Original used additive transparency with no culling.
        return RenderTypes.eyes(location);
    }

    public static RenderType getOverlay(final Identifier location) {
        // TODO(oc2-restore): Original used additive transparency on the translucent target.
        return RenderTypes.eyes(location);
    }

    public static RenderType getGateWayParticle() {
        return RenderTypes.lightning();
    }

    ///////////////////////////////////////////////////////////////////

    private ModRenderType() {
    }
}
