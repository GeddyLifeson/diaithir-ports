package org.valkyrienskies.mod.mixin.feature.shipyard_entities;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.mixinducks.client.render.EntityRenderStateDuck;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    /**
     * Remember which entity a render state was extracted from, so the ship transform can be applied on submit.
     */
    @ModifyReturnValue(
        method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
        at = @At("RETURN")
    )
    private EntityRenderState vs$attachEntity(final EntityRenderState state, final Entity entity,
        final float partialTicks) {
        ((EntityRenderStateDuck) state).vs$setEntity(entity);
        return state;
    }

    /**
     * Entities are rendered along with their name tags, which is good as the ship transforms are already applied.
     * For readability, however, we want the tags to always be vertically oriented so we negate rotation of the ship.
     * The name tag is billboarded using the camera orientation, so we temporarily cancel the ship rotation out of it.
     */
    @WrapOperation(
        method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/level/CameraRenderState;)V"
        )
    )
    private void revertShipRotation(final SubmitNodeCollector instance, final PoseStack poseStack,
        final Vec3 attachment, final int offset, final Component name, final boolean seeThrough,
        final int lightCoords, final double distanceToCameraSq, final CameraRenderState camera,
        final Operation<Void> original, @Local(argsOnly = true) final EntityRenderState state) {
        final Entity entity = ((EntityRenderStateDuck) state).vs$getEntity();
        final ClientShip ship = entity == null ? null :
            (ClientShip) VSGameUtilsKt.getLoadedShipManagingPos(entity.level(), entity.blockPosition());
        if (ship == null) {
            original.call(instance, poseStack, attachment, offset, name, seeThrough, lightCoords, distanceToCameraSq,
                camera);
            return;
        }
        final Quaternionf savedOrientation = new Quaternionf(camera.orientation);
        camera.orientation.set(new Quaternionf(ship.getRenderTransform().getShipToWorldRotation()).invert()
            .mul(savedOrientation));
        try {
            original.call(instance, poseStack, attachment, offset, name, seeThrough, lightCoords,
                distanceToCameraSq, camera);
        } finally {
            camera.orientation.set(savedOrientation);
        }
    }
}
