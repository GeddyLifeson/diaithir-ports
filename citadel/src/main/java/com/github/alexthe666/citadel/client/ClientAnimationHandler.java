package com.github.alexthe666.citadel.client;

import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/** Client side of AnimationMessage. Only loaded when the packet arrives on a client. */
public class ClientAnimationHandler {
    public static void handle(int entityId, int index) {
        if (Minecraft.getInstance().level == null) return;
        Entity e = Minecraft.getInstance().level.getEntity(entityId);
        if (e instanceof IAnimatedEntity animated) {
            Animation[] all = animated.getAnimations();
            animated.setAnimation(index == -1 || index >= all.length ? IAnimatedEntity.NO_ANIMATION : all[index]);
            animated.setAnimationTick(0);
        }
    }
}
