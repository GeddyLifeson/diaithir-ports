package net.diaithir.filters;

import java.util.List;
import java.util.Set;

/** Static selftest for the pure logic. Run its main(); exits non-zero on failure. */
public final class SelfTest {
    private SelfTest() {
    }

    private record P(String id, Set<String> tags, int dur, Set<String> ench) implements Rule.Probe {
        public String itemId() {
            return id;
        }

        public boolean hasTag(String t) {
            return tags.contains(t.startsWith("#") ? t.substring(1) : t);
        }

        public int durabilityPercent() {
            return dur;
        }

        public boolean hasEnchantment(String e) {
            return ench.contains(e);
        }
    }

    private static void check(boolean ok, String what) {
        if (!ok) {
            System.err.println("FAIL: " + what);
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        P pick = new P("minecraft:diamond_pickaxe", Set.of("minecraft:pickaxes"), 40, Set.of("minecraft:efficiency"));
        P dirt = new P("minecraft:dirt", Set.of("minecraft:dirt"), 100, Set.of());
        P modded = new P("create:cogwheel", Set.of(), 100, Set.of());

        check(new Rule.ItemRule("minecraft:dirt").test(dirt), "item match");
        check(!new Rule.ItemRule("minecraft:dirt").test(pick), "item miss");
        check(new Rule.TagRule("#minecraft:pickaxes").test(pick), "tag with hash");
        check(new Rule.ModRule("create").test(modded), "mod match");
        check(!new Rule.ModRule("create").test(dirt), "mod miss");
        check(new Rule.DurabilityRule(0, 50).test(pick), "durability in range");
        check(!new Rule.DurabilityRule(50, 100).test(pick), "durability out of range");
        check(new Rule.EnchantRule("minecraft:efficiency").test(pick), "enchant");

        Rule and = new Rule.And(List.of(new Rule.ModRule("minecraft"), new Rule.TagRule("minecraft:pickaxes")));
        check(and.test(pick) && !and.test(dirt), "and");
        Rule or = new Rule.Or(List.of(new Rule.ItemRule("minecraft:dirt"), new Rule.ModRule("create")));
        check(or.test(dirt) && or.test(modded) && !or.test(pick), "or");
        Rule not = new Rule.Not(new Rule.ModRule("minecraft"));
        check(not.test(modded) && !not.test(dirt), "not");

        Rule nested = new Rule.And(List.of(or, new Rule.Not(new Rule.ItemRule("create:cogwheel"))));
        check(nested.test(dirt) && !nested.test(modded), "nested");
        check(nested.depth() == 3 && nested.size() == 7, "depth and size");

        FilterData empty = FilterData.EMPTY;
        check(Filters.matches(empty, dirt), "empty filter passes all");
        FilterData d = empty.with(new Rule.ModRule("minecraft")).with(new Rule.Not(new Rule.ItemRule("minecraft:dirt")));
        check(Filters.matches(d, pick) && !Filters.matches(d, dirt) && !Filters.matches(d, modded), "filter data");
        check(d.without(1).rules().size() == 1, "remove rule");
        check(d.withinLimits(32, 4) && !d.withinLimits(1, 4), "limits");

        check(IdMigration.item("ftbfiltersystem:smart_filter").equals("diaithirfilters:filter"), "migration");
        check(IdMigration.item("other:thing").equals("other:thing"), "migration passthrough");
        check(Rule.describe(nested).contains("NOT"), "describe");
        System.out.println("diaithirfilters selftest OK");
    }
}
