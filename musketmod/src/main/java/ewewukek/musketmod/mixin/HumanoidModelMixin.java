package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import ewewukek.musketmod.ClientUtilities;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;

@Mixin(HumanoidModel.class)
abstract class HumanoidModelMixin {
    private static final String SETUP_ANIM = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V";

    @Inject(method = SETUP_ANIM, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/model/HumanoidModel;setupAttackAnimation(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V"))
    private void poseGunArms(HumanoidRenderState state, CallbackInfo ci) {
        ClientUtilities.poseGunArms((HumanoidModel<?>)(Object)this, state);
    }

    // aiming arms stay still, like the spyglass pose
    @WrapOperation(method = SETUP_ANIM, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/model/AnimationUtils;bobModelPart(Lnet/minecraft/client/model/geom/ModelPart;FF)V"))
    private void bobModelPart(ModelPart part, float ageInTicks, float scale, Operation<Void> original, HumanoidRenderState state) {
        if (ClientUtilities.gunPose(state, scale > 0 ? HumanoidArm.RIGHT : HumanoidArm.LEFT) == ClientUtilities.GunPose.NONE) {
            original.call(part, ageInTicks, scale);
        }
    }
}
