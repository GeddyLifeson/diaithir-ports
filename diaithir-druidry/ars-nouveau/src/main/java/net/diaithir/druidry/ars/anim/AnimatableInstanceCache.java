package net.diaithir.druidry.ars.anim;
public class AnimatableInstanceCache {
    private final Object owner;
    public final AnimatableManager<?> manager = new AnimatableManager<>();
    public AnimatableInstanceCache(Object owner) { this.owner = owner; }
    public Object owner() { return owner; }
    @SuppressWarnings("unchecked")
    public <T> AnimatableManager<T> getManagerForId(long id) { return (AnimatableManager<T>) manager; }
}
