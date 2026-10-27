package org.dimdev.dimdoors.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * Draws the shimmering surface of a dimensional door/trapdoor.
 *
 * <p>TODO 26.1: the old renderer used a custom core shader (dimensional_portal) mixing the end sky and a
 * warp texture. Custom core shaders are gone, so for now this uses the vanilla end portal render type.</p>
 */
public final class DimensionalPortalRenderer {
    private static final float MIN = 0.2F / 16F;
    private static final float Z = -0.1F / 16F;

    private DimensionalPortalRenderer() {
    }

    public static void renderDimensionalPortal(SubmitNodeCollector collector, PoseStack poseStack, Transformer transformer, boolean tall) {
        poseStack.pushPose();
        transformer.transform(poseStack);
        final float height = tall ? 2.0F : 1.0F;
        collector.submitCustomGeometry(poseStack, RenderTypes.endPortal(), (pose, buffer) -> {
            quad(pose, buffer, height, Z);
            quad(pose, buffer, height, Z + 0.0006F);
        });
        poseStack.popPose();
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, float height, float z) {
        float x1 = 1.0F - MIN;
        float y1 = height - MIN;
        buffer.addVertex(pose, MIN, MIN, z);
        buffer.addVertex(pose, x1, MIN, z);
        buffer.addVertex(pose, x1, y1, z);
        buffer.addVertex(pose, MIN, y1, z);
    }
}
