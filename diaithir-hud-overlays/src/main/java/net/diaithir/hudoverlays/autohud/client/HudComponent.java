// Ported from AutoHUD (LGPL-3.0-only) https://github.com/Crendgrim/AutoHUD
// Modifications: Simplified Component state machine (reveal timer, move/fade animation) driven by supplier-based config.
package net.diaithir.hudoverlays.autohud.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.diaithir.hudoverlays.autohud.config.AutoHudConfig;
import net.diaithir.hudoverlays.autohud.config.AutoHudConfig.Direction;
import net.diaithir.hudoverlays.autohud.config.AutoHudConfig.RevealPolicy;
import net.diaithir.hudoverlays.autohud.config.AutoHudConfig.RevealType;

public final class HudComponent {
    public static final List<HudComponent> ALL = new ArrayList<>();
    public static final List<HudComponent> MAIN_HUD = new ArrayList<>();

    public final String name;
    private final BooleanSupplier activeCfg;
    private final Supplier<Direction> direction;
    private final Supplier<RevealPolicy> policy; // null for non policy components
    private final BooleanSupplier onChangeCfg;
    private final Supplier<Double> maxFade;
    private final List<HudComponent> stack = new ArrayList<>();

    public double alpha = 1, alphaDelta = 0;
    public double offset = 0, offsetDelta = 0;
    private float visibleTime = 1;
    private boolean forceHidden;

    // state tracking
    private int lastValue = Integer.MIN_VALUE;
    private boolean updateNextTick = true;

    public HudComponent(String name, BooleanSupplier activeCfg, BooleanSupplier onChangeCfg, Supplier<RevealPolicy> policy,
                        Supplier<Direction> direction, Supplier<Double> maxFade, boolean mainHud) {
        this.name = name;
        this.activeCfg = activeCfg;
        this.onChangeCfg = onChangeCfg;
        this.policy = policy;
        this.direction = direction;
        this.maxFade = maxFade;
        ALL.add(this);
        if (mainHud) MAIN_HUD.add(this);
    }

    public HudComponent stack(HudComponent... c) {
        for (HudComponent x : c) stack.add(x);
        return this;
    }

    public boolean isActive() {
        if (policy != null) return policy.get() != RevealPolicy.ALWAYS;
        return activeCfg.getAsBoolean();
    }

    public boolean onChange() {
        if (policy != null) return policy.get() != RevealPolicy.ALWAYS;
        return onChangeCfg.getAsBoolean();
    }

    public RevealPolicy policy() { return policy == null ? null : policy.get(); }

    public double maximumFade() {
        double v = maxFade == null ? AutoHudConfig.DEFAULT_MAXIMUM_FADE.get() : maxFade.get();
        return Math.min(1.0, v < 0 ? AutoHudConfig.DEFAULT_MAXIMUM_FADE.get() : v);
    }

    public double distance() { return AutoHudConfig.DEFAULT_DISTANCE.get(); }
    public double speedMultiplier() { return AutoHudConfig.DEFAULT_SPEED_MULTIPLIER.get(); }
    public Direction direction() { return direction.get(); }

    public boolean fullyRevealed() { return offset == 0 && alpha == 1; }
    public boolean fullyHidden() { return offset == 1 && alpha == 0; }
    public boolean isHidden() { return !fullyRevealed(); }

    public boolean shouldRender() {
        if (!isActive()) return true;
        if (AutoHudConfig.ANIMATION_FADE.get()) return !fullyHidden() || maximumFade() > 0;
        return !fullyHidden();
    }

    public float getAlpha(float pt) {
        if (!AutoHudConfig.ANIMATION_FADE.get()) return 1f;
        double a = alpha + pt * alphaDelta;
        return (float) ((1.0 - maximumFade()) * a + maximumFade());
    }

    private double pos(float pt) { return offset + pt * offsetDelta; }

    public double offsetX(float pt) {
        if (!AutoHudConfig.ANIMATION_MOVE.get() && AutoHudConfig.ANIMATION_FADE.get()) return 0;
        return switch (direction()) {
            case LEFT -> -pos(pt) * distance();
            case RIGHT -> pos(pt) * distance();
            default -> 0;
        };
    }

    public double offsetY(float pt) {
        if (!AutoHudConfig.ANIMATION_MOVE.get() && AutoHudConfig.ANIMATION_FADE.get()) return 0;
        return switch (direction()) {
            case UP -> -pos(pt) * distance();
            case DOWN -> pos(pt) * distance();
            default -> 0;
        };
    }

    // reveal API

    public void reveal() { visibleTime = AutoHudConfig.TICKS_REVEALED.get(); forceHidden = false; }

    public void revealCombined() { revealCombined(AutoHudConfig.TICKS_REVEALED.get()); }

    public void revealCombined(float time) {
        forceHidden = false;
        visibleTime = time;
        if (!isActive()) return;
        RevealType t = AutoHudConfig.REVEAL_TYPE.get();
        switch (t) {
            case COMBINED -> ALL.forEach(c -> c.visibleTime = Math.max(c.visibleTime, time));
            case HIDE_COMBINED -> ALL.forEach(c -> c.keepRevealed(time));
            case STACKED -> keepRevealedStacked(time);
            case GROUPED -> { if (MAIN_HUD.contains(this)) MAIN_HUD.forEach(c -> c.visibleTime = Math.max(c.visibleTime, time)); }
            case INDIVIDUAL -> { }
        }
    }

    private void keepRevealedStacked(float time) {
        keepRevealed(time);
        stack.forEach(c -> c.keepRevealedStacked(time));
    }

    private void keepRevealed(float time) {
        if (isActive() && visibleTime > 0 && visibleTime < time) visibleTime = time;
    }

    public void hide() {
        if (!isActive()) return;
        visibleTime = 0;
        updateNextTick = true;
    }

    public void forceHide() {
        if (!isActive()) return;
        visibleTime = 0;
        forceHidden = true;
    }

    public void hideNow() { visibleTime = 0; offset = 1; alpha = 0; offsetDelta = 0; alphaDelta = 0; }
    public void revealNow() { visibleTime = AutoHudConfig.TICKS_REVEALED.get(); offset = 0; alpha = 1; offsetDelta = 0; alphaDelta = 0; }

    public void synchronizeFrom(HudComponent... others) {
        for (HudComponent o : others) {
            visibleTime = Math.max(visibleTime, o.visibleTime);
            if (o.offset < offset) { offset = o.offset; offsetDelta = o.offsetDelta; }
            if (o.alpha > alpha) { alpha = o.alpha; alphaDelta = o.alphaDelta; }
        }
    }

    public void revealIfChanged(boolean trigger) {
        if (!isActive() || (onChange() && trigger)) revealCombined();
    }

    public void scheduleUpdate() { updateNextTick = true; }

    /** Value tracked reveal policy handling. max may be <= 0 if unknown. */
    public void trackValue(int value, int max, boolean forceReveal) {
        boolean reveal = false;
        if (!isActive()) {
            reveal = true;
        } else if (policy != null) {
            RevealPolicy p = policy.get();
            int last = lastValue == Integer.MIN_VALUE ? value : lastValue;
            reveal = forceReveal || switch (p) {
                case NOT_FULL -> max > 0 && value < max;
                case LOW -> max > 0 && value <= max / 3;
                case INCREASING -> value > last;
                case DECREASING -> value < last;
                case CHANGING -> value != last;
                case DISABLED -> !HudState.dynamic();
                case ALWAYS -> true;
            };
        } else {
            int last = lastValue == Integer.MIN_VALUE ? value : lastValue;
            reveal = forceReveal || (onChange() && value != last);
        }
        lastValue = value;
        if (reveal) revealCombined();
    }

    public void tick() {
        boolean dynamic = HudState.dynamic();
        if (visibleTime == 0) {
            if (!fullyHidden()) {
                if (!updateNextTick || forceHidden) {
                    if (AutoHudConfig.ANIMATION_MOVE.get() || AutoHudConfig.ANIMATION_FADE.get()) moveOut();
                    else hideNow();
                }
            }
            if (offset == 1) offsetDelta = 0;
            if (alpha == 0) alphaDelta = 0;
        } else if (!fullyRevealed()) {
            if (!AutoHudConfig.ANIMATION_MOVE.get() && !AutoHudConfig.ANIMATION_FADE.get()) revealNow();
            else moveIn();
        } else {
            if (offset == 0) offsetDelta = 0;
            if (alpha == 1) alphaDelta = 0;
        }
        if (isActive() && dynamic && visibleTime > 0) {
            visibleTime = Math.max(0, visibleTime - 1);
            if (visibleTime == 0) updateNextTick = true;
        }
        updateNextTick = false;
    }

    private double moveInSpeed() { double s = AutoHudConfig.SPEED_MOVE_IN.get(); return s == 0 ? AutoHudConfig.ANIMATION_SPEED.get() : s; }
    private double moveOutSpeed() { double s = AutoHudConfig.SPEED_MOVE_OUT.get(); return s == 0 ? AutoHudConfig.ANIMATION_SPEED.get() : s; }
    private double fadeInSpeed() { double s = AutoHudConfig.SPEED_FADE_IN.get(); return s == 0 ? AutoHudConfig.ANIMATION_SPEED.get() : s; }
    private double fadeOutSpeed() { double s = AutoHudConfig.SPEED_FADE_OUT.get(); return s == 0 ? AutoHudConfig.ANIMATION_SPEED.get() : s; }

    private void moveIn() {
        offset = Math.max(0, offset + offsetDelta);
        alpha = Math.min(1, alpha + alphaDelta);
        double os = Math.sqrt(0.01 + offset) * 0.1 * moveInSpeed() * speedMultiplier();
        double as = 0.05 * fadeInSpeed() * speedMultiplier();
        offsetDelta = offset - os <= 0 ? -offset : -os;
        alphaDelta = alpha + as >= 1 ? 1 - alpha : as;
    }

    private void moveOut() {
        offset = Math.min(1, offset + offsetDelta);
        alpha = Math.max(0, alpha + alphaDelta);
        double os = Math.sqrt(0.01 + offset) * 0.1 * moveOutSpeed() * speedMultiplier();
        double as = 0.05 * fadeOutSpeed() * speedMultiplier();
        offsetDelta = offset + os >= 1 ? 1 - offset : os;
        alphaDelta = alpha - as <= 0 ? -alpha : -as;
    }

    public static void revealAll() { ALL.forEach(HudComponent::revealCombined); }
    public static void revealAll(float t) { ALL.forEach(c -> c.revealCombined(t)); }
    public static void hideAll() { ALL.forEach(HudComponent::hide); }
    public static void forceHideAll() { ALL.forEach(HudComponent::forceHide); }
    public static void updateAll() { ALL.forEach(HudComponent::scheduleUpdate); }
    public static void tickAll() { ALL.forEach(HudComponent::tick); }
}
