package net.diaithir.druidry.ars.anim;
public interface GeoAnimatable {
    void registerControllers(AnimatableManager.ControllerRegistrar controllers);
    AnimatableInstanceCache getAnimatableInstanceCache();
}
