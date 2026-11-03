package net.diaithir.upgrades;

/**
 * Sum of module effects as fractions (0.2 = +20%). Immutable.
 * speed: work rate. productivity: free extra output. consumption: energy/fuel/ingredient draw.
 * quality: chance per craft to raise output quality one tier.
 */
public record Effects(double speed, double productivity, double consumption, double quality) {
    public static final Effects NONE = new Effects(0, 0, 0, 0);

    public Effects plus(Effects o) {
        return new Effects(speed + o.speed, productivity + o.productivity,
                consumption + o.consumption, quality + o.quality);
    }

    public Effects times(double k) {
        return new Effects(speed * k, productivity * k, consumption * k, quality * k);
    }

    /** Quality scales only the beneficial side; penalties stay as they are. */
    public Effects scaleUp(double k) {
        return new Effects(speed > 0 ? speed * k : speed, productivity > 0 ? productivity * k : productivity,
                consumption < 0 ? consumption * k : consumption, quality * k);
    }

    /** Limits: speed floor -80%, consumption floor -80%, productivity cap +300%, quality 0..1. */
    public Effects clamped() {
        return new Effects(Math.max(-0.8, speed), Math.min(3.0, Math.max(0, productivity)),
                Math.max(-0.8, consumption), Math.min(1.0, Math.max(0, quality)));
    }

    public double speedMultiplier() { return 1 + clamped().speed; }
    public double outputMultiplier() { return 1 + clamped().productivity; }
    public double consumptionMultiplier() { return 1 + clamped().consumption; }
}
