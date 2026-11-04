package net.diaithir.animation;

/** Easing curves on t in [0,1]. */
public enum Easing {
    LINEAR, IN_OUT, OUT_BACK, OUT_BOUNCE;

    public float apply(float t) {
        t = Math.max(0f, Math.min(1f, t));
        switch (this) {
            case IN_OUT: return t * t * (3f - 2f * t);
            case OUT_BACK: { float c = 1.70158f, u = t - 1f; return 1f + (c + 1f) * u * u * u + c * u * u; }
            case OUT_BOUNCE: {
                float n = 7.5625f, d = 2.75f;
                if (t < 1f / d) return n * t * t;
                if (t < 2f / d) { t -= 1.5f / d; return n * t * t + 0.75f; }
                if (t < 2.5f / d) { t -= 2.25f / d; return n * t * t + 0.9375f; }
                t -= 2.625f / d; return n * t * t + 0.984375f;
            }
            default: return t;
        }
    }
}
