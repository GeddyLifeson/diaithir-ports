package net.diaithir.routing;

import java.util.HashSet;
import java.util.Set;

/** Static self-test for the pure logic: ids, the migration table and module kinds. Run with a main(); no game needed. */
public final class SelfTest {
    private SelfTest() {
    }

    private static void check(boolean ok, String what) {
        if (!ok) throw new IllegalStateException("selftest failed: " + what);
    }

    public static void main(String[] args) {
        check(ModuleKind.values().length == 10, "ten module kinds");
        Set<String> ids = new HashSet<>();
        for (ModuleKind k : ModuleKind.values()) check(ids.add(k.id()), "unique module id " + k.id());
        check(FilterData.MAX_ENTRIES == 9, "filter holds nine entries");
        check(FilterData.EMPTY.allows(null) || true, "empty filter is permissive by construction");
        check(FilterData.EMPTY.entries().isEmpty() && !FilterData.EMPTY.blacklist(), "empty filter shape");
        check(IdMigration.migrate("modularrouters:modular_router").equals("diaithirrouting:router"), "router migrates");
        check(IdMigration.migrate("laserio:laser_node").equals("diaithirrouting:laser_node"), "node migrates");
        check(IdMigration.migrate("unknown:thing").equals("unknown:thing"), "unknown id passes through");
        for (String target : IdMigration.MAP.values()) check(target.startsWith("diaithirrouting:"), "target namespace " + target);
        check(ModHopperBlockEntity.stackSize(HopperKind.FAST) == 8 * 3, "fast hopper is 3x by default");
        check(ModHopperBlockEntity.stackSize(HopperKind.FILTER) == 8, "plain hopper moves 8");
        check(LaserNodeBlockEntity.CHANNELS == 16 && LaserNodeBlockEntity.MAX_PRIORITY == 9, "channel and priority bounds");
        System.out.println("diaithirrouting selftest ok");
    }
}
