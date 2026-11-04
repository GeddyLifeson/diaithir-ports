package net.diaithir.upgrades;

import java.util.ArrayList;
import java.util.List;

/**
 * Upgrade planner: bulk swap of modules (or of block ids) across many blocks. Pure: returns what
 * to do, the glue applies it and takes items from the player's inventory.
 */
public final class Planner {
    private Planner() {}

    public record Swap(int blockIndex, int slot, ModuleSpec from, ModuleSpec to, Quality quality) {}

    /**
     * For every block holding module `from` (any quality), replace it with `to` at quality `q`,
     * as long as the block accepts the new kind and `budget` (modules in hand) lasts.
     */
    public static List<Swap> planModuleSwap(List<Slotted> blocks, ModuleSpec from, ModuleSpec to,
                                            Quality q, int budget) {
        List<Swap> out = new ArrayList<>();
        for (int b = 0; b < blocks.size() && out.size() < budget; b++) {
            Slotted s = blocks.get(b);
            if (!s.target().accepts(to)) continue;
            List<Slotted.Installed> m = s.modules();
            for (int i = 0; i < m.size() && out.size() < budget; i++) {
                if (m.get(i).spec().id().equals(from.id())) out.add(new Swap(b, i, from, to, q));
            }
        }
        return out;
    }

    /** Apply a plan. Returns the number of swaps done. */
    public static int apply(List<Slotted> blocks, List<Swap> plan) {
        int done = 0;
        for (Swap w : plan) {
            if (blocks.get(w.blockIndex()).replace(w.slot(), w.to(), w.quality())) done++;
        }
        return done;
    }
}
