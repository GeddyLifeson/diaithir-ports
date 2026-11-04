package me.desht.pneumaticcraft.client.render.entity.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import net.minecraft.client.renderer.MultiBufferSource;

/** Stand-in for the pre-26.1 RenderLayer used by the drone renderer. */
abstract class LegacyLayer {
    protected abstract void render(PoseStack poseStack, MultiBufferSource buffer, int light, AbstractDroneEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch);
}
