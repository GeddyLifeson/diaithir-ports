package net.diaithir.druidry.ars.anim;

import java.util.function.Function;

public class AnimationController<T> {
    @FunctionalInterface public interface AnimationStateHandler<A> { PlayState handle(AnimationState<A> state); }
    private final T animatable;
    private final String name;
    private final AnimationStateHandler<T> handler;
    private RawAnimation current;
    private long startTick;

    public AnimationController(T animatable, String name, int transitionTicks, AnimationStateHandler<T> handler) {
        this.animatable = animatable; this.name = name; this.handler = handler;
    }
    public AnimationController(T animatable, int transitionTicks, AnimationStateHandler<T> handler) { this(animatable, "base_controller", transitionTicks, handler); }
    public AnimationController(T animatable, AnimationStateHandler<T> handler) { this(animatable, "base_controller", 0, handler); }
    public String getName() { return name; }
    public void setAnimation(RawAnimation a) { if (a != current) { current = a; } }
    public RawAnimation getCurrentRawAnimation() { return current; }
    public RawAnimation getCurrentAnimation() { return current; }
    /** Runs the handler for one render pass; returns the clip name that is playing, or null when the handler stopped. */
    public String update(boolean moving) {
        PlayState s = handler.handle(new AnimationState<>(animatable, this, moving));
        return s == PlayState.STOP || current == null ? null : current.current();
    }
}
