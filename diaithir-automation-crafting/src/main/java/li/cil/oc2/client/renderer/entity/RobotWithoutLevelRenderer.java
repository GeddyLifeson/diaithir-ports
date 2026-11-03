/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import li.cil.oc2.client.renderer.entity.model.RobotModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

// TODO(oc2-restore): BlockEntityWithoutLevelRenderer no longer exists in 26.1.2. The item
//  rendering of the robot needs to be re-hooked through the new special item model renderer
//  system. Until then this class is a plain helper that keeps the previous rendering code and
//  method signature but is no longer invoked by the vanilla item renderer.
public final class RobotWithoutLevelRenderer {
    private final RobotModel model;

    ///////////////////////////////////////////////////////////////////

    public RobotWithoutLevelRenderer(final BlockEntityRenderDispatcher dispatcher, final EntityModelSet modelSet) {
        model = new RobotModel(modelSet.bakeLayer(RobotModel.ROBOT_MODEL_LAYER));
    }

    ///////////////////////////////////////////////////////////////////

    public void renderByItem(final ItemStack itemStack, final ItemDisplayContext transformType, final PoseStack poseStack, final MultiBufferSource bufferSource, final int combinedLight, final int combinedOverlay) {
        poseStack.pushPose();

        poseStack.translate(0.5, 0, 0.5);

        final VertexConsumer consumer = bufferSource.getBuffer(model.renderType(RobotModel.ROBOT_ENTITY_TEXTURE));
        model.renderToBuffer(poseStack, consumer, combinedLight, OverlayTexture.NO_OVERLAY, -1);

        poseStack.popPose();
    }
}
