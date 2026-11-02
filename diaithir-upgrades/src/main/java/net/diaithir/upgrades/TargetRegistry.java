package net.diaithir.upgrades;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Cross-mod lookup. Any mod opts a block in with one rule, from its own data pack, with no code:
 * a block id, a "namespace:*" wildcard, or a tag id ("#ns:path", expanded by the glue). Kept
 * third-party mods get the same rules from our compat data (data/diaithircompat/upgrades/targets).
 * First rule that matches wins; our own modules ship at priority 0, compat shims at 100, so a mod
 * author's own rule overrides a shim.
 */
public final class TargetRegistry {
    public record Rule(String pattern, int priority, int slots, Set<Kind> allowed, int maxQuality) {}

    private final List<Rule> rules = new ArrayList<>();

    public void add(Rule r) {
        rules.add(r);
        rules.sort((a, b) -> Integer.compare(a.priority(), b.priority()));
    }

    /** @param blockId "ns:path"; @param tags tag ids the block is in, as "#ns:path" */
    public Optional<Target> find(String blockId, Set<String> tags) {
        for (Rule r : rules) {
            if (matches(r.pattern(), blockId, tags)) {
                return Optional.of(new Target(blockId, r.slots(), r.allowed(), r.maxQuality()));
            }
        }
        return Optional.empty();
    }

    static boolean matches(String pattern, String blockId, Set<String> tags) {
        if (pattern.startsWith("#")) return tags.contains(pattern);
        if (pattern.endsWith(":*")) return blockId.startsWith(pattern.substring(0, pattern.length() - 1));
        return pattern.equals(blockId);
    }
}
