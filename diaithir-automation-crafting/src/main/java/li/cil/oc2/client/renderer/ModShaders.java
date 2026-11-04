/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.joml.Matrix4f;
import li.cil.oc2.api.API;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public final class ModShaders {
    public static final int MAX_PROJECTORS = 3;

    private static final Identifier PROJECTORS_SHADER_LOCATION = Identifier.fromNamespaceAndPath(API.MOD_ID, "projectors");
    private static final String[] PROJECTOR_COLOR_NAMES = {"ProjectorColor0", "ProjectorColor1", "ProjectorColor2"};
    private static final String[] PROJECTOR_DEPTH_NAMES = {"ProjectorDepth0", "ProjectorDepth1", "ProjectorDepth2"};
    private static final String[] PROJECTOR_CAMERA_NAMES = {"ProjectorCamera0", "ProjectorCamera1", "ProjectorCamera2"};

    ///////////////////////////////////////////////////////////////////

    // TODO(oc2-restore): ShaderInstance and RegisterShadersEvent no longer exist in 26.1; the projector
    // TODO(oc2-restore): shader needs to be reimplemented using the new render pipeline API.

    ///////////////////////////////////////////////////////////////////

    /**
     * Always returns {@code null} until the projector shader is ported to the new render pipeline.
     */
    @Nullable
    public static Object getProjectorsShader() {
        return null;
    }

    // TODO(oc2-restore): No-op until the projector shader is ported.
    public static void configureProjectorsShader(
        final RenderTarget target,
        final Matrix4f inverseCameraMatrix,
        final DynamicTexture[] colors,
        final RenderTarget[] depths,
        final Matrix4f[] projectorCameraMatrices,
        final int count
    ) {
    }
}
