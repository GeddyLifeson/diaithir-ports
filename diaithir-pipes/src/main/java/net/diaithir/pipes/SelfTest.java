package net.diaithir.pipes;

import net.minecraft.core.BlockPos;

import java.util.List;

/** Static check of the pure logic. Run with main(). */
public final class SelfTest {
    private SelfTest() {
    }

    private static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    public static void main(String[] args) {
        check(FaceMode.BOTH.extracts() && FaceMode.BOTH.inserts(), "both");
        check(FaceMode.EXTRACT.extracts() && !FaceMode.EXTRACT.inserts(), "extract");
        check(!FaceMode.DISCONNECTED.extracts() && !FaceMode.DISCONNECTED.inserts(), "off");
        check(FaceMode.DISCONNECTED.next() == FaceMode.INSERT, "wrap");

        check(RedstoneMode.ALWAYS.allows(false) && RedstoneMode.ALWAYS.allows(true), "always");
        check(RedstoneMode.POWERED.allows(true) && !RedstoneMode.POWERED.allows(false), "powered");
        check(RedstoneMode.UNPOWERED.allows(false) && !RedstoneMode.UNPOWERED.allows(true), "unpowered");

        int[] d = {5, 1, 3};
        check(Distribution.order(Distribution.NEAREST, d, 0).equals(List.of(1, 2, 0)), "nearest");
        check(Distribution.order(Distribution.ROUND_ROBIN, d, 1).equals(List.of(1, 2, 0)), "rr 1");
        check(Distribution.order(Distribution.ROUND_ROBIN, d, 4).equals(List.of(1, 2, 0)), "rr wrap");
        check(Distribution.order(Distribution.ROUND_ROBIN, new int[0], 0).isEmpty(), "rr empty");

        check(ConnectorItem.linkable(new BlockPos(0, 0, 0), new BlockPos(0, 0, 2), 8), "link 2");
        check(ConnectorItem.linkable(new BlockPos(0, 0, 0), new BlockPos(9, 0, 0), 8), "link 9");
        check(!ConnectorItem.linkable(new BlockPos(0, 0, 0), new BlockPos(10, 0, 0), 8), "too far");
        check(!ConnectorItem.linkable(new BlockPos(0, 0, 0), new BlockPos(0, 0, 1), 8), "adjacent");
        check(!ConnectorItem.linkable(new BlockPos(0, 0, 0), new BlockPos(2, 0, 2), 8), "diagonal");

        check(IdMigration.migrate("pipez:energy_pipe").equals("diaithirpipes:energy_pipe_basic"), "migrate");
        check(IdMigration.migrate("other:thing").equals("other:thing"), "passthrough");
        check(PipeTier.ADVANCED.multiplier() == 16, "tier");
        System.out.println("diaithirpipes selftest ok");
    }
}
