package net.diaithir.druidry.ars.anim;

import net.minecraft.world.entity.LivingEntity;

public class AnimationState<T> {
    private final T animatable;
    private final AnimationController<T> controller;
    private final boolean moving;
    public AnimationState(T animatable, AnimationController<T> controller, boolean moving) {
        this.animatable = animatable; this.controller = controller; this.moving = moving;
    }
    public T getAnimatable() { return animatable; }
    public AnimationController<T> getController() { return controller; }
    public boolean isMoving() { return moving; }
    public boolean isCurrentAnimation(RawAnimation a) { return controller.getCurrentRawAnimation() == a; }
    public PlayState setAndContinue(RawAnimation a) { controller.setAnimation(a); return PlayState.CONTINUE; }
    public PlayState setAnimation(RawAnimation a) { return setAndContinue(a); }
}
