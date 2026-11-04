package net.diaithir.portable;

import java.util.List;

/** Static selftest of the pure logic. Run its main(); it throws on any failure. */
public final class Selftest {
    private Selftest() {
    }

    private static void check(boolean ok, String what) {
        if (!ok) throw new IllegalStateException("selftest failed: " + what);
    }

    public static void main(String[] args) {
        check(Tier.WOOD.slots == 27 && Tier.IRON.slots == 54 && Tier.GOLD.slots == 81 && Tier.DIAMOND.slots == 108 && Tier.NETHERITE.slots == 162, "tier slots");
        check(Tier.NETHERITE.rows() * Tier.NETHERITE.columns >= Tier.NETHERITE.slots, "rows cover slots");
        check(Rules.stackSize(64, 0, 2) == 64, "stack none");
        check(Rules.stackSize(64, 3, 2) == 512, "stack three");
        check(Rules.stackSize(64, 40, 4) == 1_000_000, "stack saturates");
        check(Rules.deepAdd(1_999_999_990L, 100) == 10, "deep add clamps");
        check(Rules.deepAdd(0, 5) == 5, "deep add");
        check(Rules.deepTake(7, 100) == 7, "deep take clamps");
        check(Rules.cycle(2, 3) == 0 && Rules.cycle(0, 3) == 1, "cycle");
        check(Rules.sortedIds(List.of("b", "a", "c")).equals(List.of("a", "b", "c")), "sorted");
        check(UpgradeKind.byId("magnet_upgrade") == UpgradeKind.MAGNET, "upgrade id");
        check(UpgradeKind.byId("nope") == null, "upgrade unknown");
        check("diaithirportable:deep_null".equals(IdMigration.migrate("deepnull:deep_null")), "migrate deepnull");
        check("diaithirportable:iron_storage".equals(IdMigration.migrate("sophisticatedstorage:iron_chest")), "migrate chest");
        check(IdMigration.migrate("x:y") == null, "migrate unknown");
        System.out.println("diaithirportable selftest ok");
    }
}
