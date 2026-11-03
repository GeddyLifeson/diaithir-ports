/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.renderer;

import li.cil.oc2.common.blockentity.ProjectorBlockEntity;

/**
 * TODO(oc2-restore): The projector depth rendering relied on the legacy GlStateManager / RenderSystem state,
 * ShaderInstance, BufferUploader, GameRenderer light texture, RenderTarget.bindWrite and the old
 * RenderLevelStageEvent API, none of which exist in 26.1.2. Until it is rewritten on top of the new
 * GpuDevice / RenderPipeline / RenderPass API, this class is a no-op that keeps the public entry points
 * used by the rest of the mod (blocks entities and mixins) so they keep compiling. Projectors are simply
 * not rendered.
 */
public final class ProjectorDepthRenderer {
    /**
     * Adds a projector that is being rendered this frame.
     * <p>
     * TODO(oc2-restore): Currently a no-op, projector rendering is disabled.
     */
    public static void addProjector(final ProjectorBlockEntity projector) {
        // TODO(oc2-restore)
    }

    /**
     * Whether we will be rendering projector depth this frame.
     * <p>
     * TODO(oc2-restore): Always false while projector rendering is disabled.
     */
    public static boolean willRenderProjectorDepth() {
        return false;
    }

    /**
     * Whether we're currently rendering projector depth maps.
     * <p>
     * TODO(oc2-restore): Always false while projector rendering is disabled.
     */
    public static boolean isIsRenderingProjectorDepth() {
        return false;
    }

    /**
     * Called from a mixin in the level renderer to grab the current depth buffer.
     * <p>
     * TODO(oc2-restore): Currently a no-op, projector rendering is disabled.
     */
    public static void captureMainCameraDepth() {
        // TODO(oc2-restore)
    }

    private ProjectorDepthRenderer() {
    }
}
