package net.diaithir.upgrades;

import java.util.ArrayList;
import java.util.List;

/** Modules installed in one block. Order is irrelevant. */
public final class Slotted {
    public record Installed(ModuleSpec spec, Quality quality) {}

    private final Target target;
    private final List<Installed> modules = new ArrayList<>();

    public Slotted(Target target) { this.target = target; }

    public Target target() { return target; }
    public List<Installed> modules() { return List.copyOf(modules); }

    /** @return false if full or the module kind is not allowed here */
    public boolean install(ModuleSpec spec, Quality q) {
        if (modules.size() >= target.slots() || !target.accepts(spec)) return false;
        modules.add(new Installed(spec, q));
        return true;
    }

    public boolean remove(int slot) {
        if (slot < 0 || slot >= modules.size()) return false;
        modules.remove(slot);
        return true;
    }

    /** Swap one slot in place, so slot numbers stay stable during a bulk plan. */
    public boolean replace(int slot, ModuleSpec spec, Quality q) {
        if (slot < 0 || slot >= modules.size() || !target.accepts(spec)) return false;
        modules.set(slot, new Installed(spec, q));
        return true;
    }

    public Effects own() {
        Effects e = Effects.NONE;
        for (Installed i : modules) e = e.plus(i.spec().effects(i.quality()));
        return e;
    }
}
