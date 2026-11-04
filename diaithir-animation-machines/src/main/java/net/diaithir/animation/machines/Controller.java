package net.diaithir.animation.machines;

/**
 * Per-instance animation state. The block entity renderer ticks it with frame time and reads pose().
 * Blends between states over BLEND seconds so machines wind up and down instead of snapping.
 */
public final class Controller {
    public static final float BLEND = 0.35f;

    private final Spec spec;
    private State state = State.IDLE;
    private float speedNow;     // current effective rate multiplier, eased toward the target
    private float phase;        // accumulated phase (cycles for most kinds, degrees for SPIN)
    private float open;         // 0..1 for SWING/SLIDE
    private float aimYaw, aimPitch;
    private long shakeSeed = 12345;

    public Controller(Spec spec) { this.spec = spec; }

    public Spec spec() { return spec; }
    public State state() { return state; }
    public void setState(State s) { this.state = s; }
    public void aim(float yawDeg, float pitchDeg) { this.aimYaw = yawDeg; this.aimPitch = pitchDeg; }

    private float targetFactor() {
        switch (state) {
            case WORKING: return 1f;
            case DONE: return spec.doneFactor();
            default: return spec.idleFactor();
        }
    }

    public void tick(float dt) {
        float target = targetFactor();
        float k = Math.min(1f, dt / BLEND);
        speedNow += (target - speedNow) * k;
        switch (spec.kind()) {
            case SWING: case SLIDE: {
                float to = state == State.IDLE ? 0f : 1f;
                float step = dt * Math.max(0.1f, spec.speed());
                open = open < to ? Math.min(to, open + step) : Math.max(to, open - step);
                break;
            }
            case SPIN: phase = (phase + spec.speed() * speedNow * dt) % 360f; break;
            case AIM: break;
            default: phase = (phase + spec.speed() * speedNow * dt) % 1000f; break;
        }
    }

    /** Current rate multiplier (0 = stopped). Renderers use it to dim sounds and particles too. */
    public float activity() { return speedNow; }

    public Pose pose() {
        float a = spec.amount();
        float v;
        switch (spec.kind()) {
            case SPIN: return rot(phase);
            case PISTON: v = a * 0.5f * (1f - (float) Math.cos(phase * 2 * Math.PI)); return off(v * speedNow >= 0 ? v : 0);
            case SWING: return rot(a * spec.easing().apply(open));
            case SLIDE: return off(a * spec.easing().apply(open));
            case HOVER: {
                float y = a * (float) Math.sin(phase * 2 * Math.PI);
                return new Pose(0, phase * 36f % 360f, 0, 0, y, 0, 1);
            }
            case PULSE: return new Pose(0, 0, 0, 0, 0, 0, 1f + a * 0.5f * (float) Math.sin(phase * 2 * Math.PI) * speedNow);
            case AIM: return new Pose(aimPitch, aimYaw, 0, 0, 0, 0, 1);
            case SCROLL: return off(phase * a % 1f);
            case SHAKE: {
                shakeSeed = shakeSeed * 6364136223846793005L + 1442695040888963407L;
                float n = ((shakeSeed >>> 40) & 0xFFFF) / 65535f - 0.5f;
                return off(n * a * speedNow);
            }
            case SWAY: v = a * (float) Math.sin(phase * 2 * Math.PI); return rot(v * Math.max(speedNow, 0.15f));
            default: return Pose.NONE;
        }
    }

    private Pose rot(float deg) {
        switch (spec.axis()) {
            case 'x': return new Pose(deg, 0, 0, 0, 0, 0, 1);
            case 'y': return new Pose(0, deg, 0, 0, 0, 0, 1);
            default: return new Pose(0, 0, deg, 0, 0, 0, 1);
        }
    }

    private Pose off(float b) {
        switch (spec.axis()) {
            case 'x': return new Pose(0, 0, 0, b, 0, 0, 1);
            case 'y': return new Pose(0, 0, 0, 0, b, 0, 1);
            default: return new Pose(0, 0, 0, 0, 0, b, 1);
        }
    }
}
