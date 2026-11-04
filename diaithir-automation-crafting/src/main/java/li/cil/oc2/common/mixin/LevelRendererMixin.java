/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * TODO(oc2-restore): The projector depth capture relied on the pre-26.1 render pipeline: LightTexture,
 * RenderSystem.depthMask/disableBlend/applyModelViewMatrix, FogRenderer.setupNoFog, the Sheets.*Sheet()
 * render type getters, Camera.getPosition and shadowed LevelRenderer render targets. All of these were removed
 * or reworked in 26.1, and the old injection points and shadowed fields no longer exist.
 * The original injections (early exit from renderLevel after particles and weather while rendering projector
 * depth, skipping outlines and sky, redirecting the entity, item entity and weather targets to the main target)
 * need to be re-implemented against the new frame graph based LevelRenderer.
 * Until then this mixin is a deliberate no-op, so the game renders normally.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
}
