package net.diaithir.upgrades;

/**
 * One module item. tier 1..3. Built from data (data/&lt;ns&gt;/upgrades/modules/*.json) by the glue layer.
 * minAge is the tech-tree Age (1..8) that unlocks it.
 */
public record ModuleSpec(String id, Kind kind, int tier, Theme theme, int minAge) {
    public ModuleSpec {
        if (tier < 1 || tier > 3) throw new IllegalArgumentException("tier " + tier);
    }

    /** Effect at a quality. Productivity and speed trade against consumption, like the genre. */
    public Effects effects(Quality q) {
        double s = q.scale();
        return switch (kind) {
            case SPEED -> new Effects(0.2 * tier, 0, 0.5 * tier, 0).scaleUp(s);
            case PRODUCTIVITY -> new Effects(-0.05 * tier, 0.04 * tier, 0.4 * tier, 0).scaleUp(s);
            case EFFICIENCY -> new Effects(0, 0, -0.3 * tier, 0).scaleUp(s);
            case QUALITY -> new Effects(-0.05, 0, 0.1 * tier, 0.01 * tier).scaleUp(s);
        };
    }
}
