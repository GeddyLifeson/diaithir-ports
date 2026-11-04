package net.diaithir.animation;

/**
 * One animation recipe.
 * axis: 'x','y','z'. speed: degrees/s for SPIN, cycles/s otherwise.
 * amount: degrees (SWING, AIM, SWAY), blocks (PISTON, SLIDE, HOVER), scale delta (PULSE), blocks/s (SCROLL).
 * idleFactor and doneFactor scale speed in those states (0 stops it).
 */
public record Spec(Kind kind, char axis, float speed, float amount, float idleFactor, float doneFactor, Easing easing) {
    public Spec {
        if (axis != 'x' && axis != 'y' && axis != 'z') throw new IllegalArgumentException("axis " + axis);
    }
}
