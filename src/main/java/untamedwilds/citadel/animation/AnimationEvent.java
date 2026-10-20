package untamedwilds.citadel.animation;

import net.minecraft.world.entity.Entity;
// EventBus 7 (Forge 1.21.6) has no api.Event and no @Cancelable: the base is MutableEvent,
// a parent with event subclasses declares InheritableEvent, and cancellability is the
// Cancellable marker interface — the same shape NeoForge has had since 1.20.6.
import net.neoforged.bus.api.Event;

public class AnimationEvent<T extends Entity & IAnimatedEntity> extends Event {
    protected Animation animation;
    private final T entity;

    AnimationEvent(T entity, Animation animation) {
        this.entity = entity;
        this.animation = animation;
    }

    public T getEntity() {
        return this.entity;
    }

    public Animation getAnimation() {
        return this.animation;
    }

    // Cancellability moved from an annotation (Forge < 1.21.6) to a marker interface — NeoForge's
    // ICancellableEvent, and EventBus 7's Cancellable.
    public static class Start<T extends Entity & IAnimatedEntity> extends AnimationEvent<T>
            implements net.neoforged.bus.api.ICancellableEvent
    {
        public Start(T entity, Animation animation) {
            super(entity, animation);
        }

        public void setAnimation(Animation animation) {
            this.animation = animation;
        }
    }

    public static class Tick<T extends Entity & IAnimatedEntity> extends AnimationEvent<T> {
        protected int tick;

        public Tick(T entity, Animation animation, int tick) {
            super(entity, animation);
            this.tick = tick;
        }

        public int getTick() {
            return this.tick;
        }
    }
}