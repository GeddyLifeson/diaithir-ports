package com.github.alexthe666.citadel.client.render.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.HumanoidArm;

/** 1.21 ArmedModel: moves the pose stack to the given hand. */
public interface LegacyArmedModel {
    void translateToHand(HumanoidArm arm, PoseStack poseStack);
}
