package net.diaithir.entityanimation.notenoughanimations.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;

final class PoseApply {
    private PoseApply() {}

    static void apply(HumanoidModel<?> model, AvatarRenderState rs, PoseState s, boolean firstPersonSkip) {
        float intensity = AnimationConfig.INTENSITY.get().floatValue();
        float t = rs.ageInTicks;

        if (!firstPersonSkip) {
            float sway = Mth.clamp(s.speed, 0F, 1.5F) * 0.25F * intensity;
            float swing = Mth.cos(rs.walkAnimationPos * 0.6662F);
            model.rightArm.xRot += rs.walkAnimationSpeed > 0.01F ? -swing * sway * 0.5F : 0F;
            model.leftArm.xRot += rs.walkAnimationSpeed > 0.01F ? swing * sway * 0.5F : 0F;

            model.rightArm.xRot += s.rightArmX;
            model.leftArm.xRot += s.leftArmX;
            model.rightArm.yRot += s.rightArmY;
            model.leftArm.yRot += s.leftArmY;
            model.rightArm.zRot += s.rightArmZ;
            model.leftArm.zRot += s.leftArmZ;

            if (s.chew > 0.01F) {
                float bob = Mth.sin(t * 1.4F) * 0.06F * s.chew * intensity;
                if (rs.isUsingItem) {
                    model.rightArm.xRot += bob;
                    model.leftArm.xRot += bob;
                }
            }

            if (AnimationConfig.IDLE_BREATHING.get() && s.speed < 0.05F && s.sneak < 0.5F && !rs.isPassenger) {
                float br = Mth.sin(t * 0.067F) * 0.025F * intensity;
                model.body.xRot += br * 0.5F;
                model.rightArm.zRot += br + 0.01F;
                model.leftArm.zRot -= br + 0.01F;
            }
        }

        if (s.sneak > 0.01F) {
            model.body.xRot += 0.12F * s.sneak * intensity;
            model.rightLeg.xRot -= 0.18F * s.sneak * intensity;
            model.leftLeg.xRot -= 0.18F * s.sneak * intensity;
        }

        if (s.legSplay > 0.01F) {
            float sp = 0.45F * s.legSplay * intensity;
            model.rightLeg.zRot += sp;
            model.leftLeg.zRot -= sp;
            model.rightLeg.yRot += sp * 0.2F;
            model.leftLeg.yRot -= sp * 0.2F;
        }

        if (Math.abs(s.lagYaw) > 0.001F) {
            model.head.yRot += s.lagYaw * intensity;
        }
    }
}
