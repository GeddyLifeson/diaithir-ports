package net.diaithir.upgrades;

import java.util.EnumSet;
import java.util.Set;

/**
 * Anything that can take modules: a machine, altar, oven, drill, ship part. Declared in data
 * (data/&lt;ns&gt;/upgrades/targets/*.json) or by tag, so any module or ported mod can opt in.
 */
public record Target(String blockId, int slots, Set<Kind> allowed, int maxQuality) {
    public Target {
        allowed = allowed.isEmpty() ? EnumSet.noneOf(Kind.class) : EnumSet.copyOf(allowed);
    }

    public boolean accepts(ModuleSpec m) { return allowed.contains(m.kind()); }
}
