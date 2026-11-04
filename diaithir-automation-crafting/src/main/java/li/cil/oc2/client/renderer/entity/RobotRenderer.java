/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import li.cil.oc2.client.renderer.entity.model.RobotModel;
import li.cil.oc2.common.entity.Robot;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

public final class RobotRenderer extends EntityRenderer<Robot, RobotRenderer.RobotRenderState> {
    public static final class RobotRenderState extends EntityRenderState {
        public float rotation;
        public final RobotModel.RobotRenderState modelState = new RobotModel.RobotRenderState();
    }

    ///////////////////////////////////////////////////////////////////

    private final RobotModel model;

    ///////////////////////////////////////////////////////////////////

    public RobotRenderer(final EntityRendererProvider.Context context) {
        super(context);
        model = new RobotModel(context.bakeLayer(RobotModel.ROBOT_MODEL_LAYER));
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    public RobotRenderState createRenderState() {
        return new RobotRenderState();
    }

    @Override
    public void extractRenderState(final Robot entity, final RobotRenderState state, final float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        final Robot.AnimationState animationState = entity.getAnimationState();
        animationState.update(partialTicks, entity.level().getRandom());

        // NB: we don't use entityYaw given to us because that uses a plain lerp which can lead to ugly
        //     jumps in case we get a wrapped rotationYaw synced from the server (leading to ~360
        //     degree delta to the last known previous rotation). Haven't figured out where to
        //     alternatively prevent this wrapping or patch the prev value instead.
        final float partialRotation = Mth.degreesDifferenceAbs(entity.yRotO, entity.getYRot()) * partialTicks;
        state.rotation = Mth.approachDegrees(entity.yRotO, entity.getYRot(), partialRotation);

        state.modelState.ageInTicks = state.ageInTicks;
        state.modelState.lightCoords = state.lightCoords;
        state.modelState.outlineColor = state.outlineColor;
    }

    @Override
    public void submit(final RobotRenderState state, final PoseStack stack, final SubmitNodeCollector collector, final CameraRenderState cameraState) {
        stack.pushPose();
        stack.mulPose(Axis.YN.rotationDegrees(state.rotation));

        collector.submitModel(model, state.modelState, stack, model.renderType(RobotModel.ROBOT_ENTITY_TEXTURE),
            state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);

        stack.popPose();
    }
}
