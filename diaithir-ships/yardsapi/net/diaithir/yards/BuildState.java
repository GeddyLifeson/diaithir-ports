package net.diaithir.yards;

import java.util.ArrayList;
import java.util.List;

/** Progress of one vehicle on a yard. Saved by the controller block entity. */
public final class BuildState {
    public enum InstallResult { OK, WRONG_STAGE, NO_SLOT, AGE_LOCKED, NEEDS_CRANE, DONE }

    private final YardDef def;
    private int stage;
    private int ticks;
    private final List<PartDef> installed = new ArrayList<>();
    private final List<PartDef> stageParts = new ArrayList<>();

    private BuildState(YardDef def) {
        this.def = def;
    }

    public static BuildState start(YardDef def) {
        return new BuildState(def);
    }

    public int stageIndex() {
        return stage;
    }

    public boolean complete() {
        return stage >= def.stages().size();
    }

    public List<PartDef> installed() {
        return List.copyOf(installed);
    }

    private int filled(String kind) {
        return (int) stageParts.stream().filter(p -> p.kind().equals(kind)).count();
    }

    private boolean slotsFull() {
        for (YardDef.Slot s : def.stages().get(stage).slots()) if (filled(s.kind()) < s.count()) return false;
        return true;
    }

    /** Install a part into the current stage. Heavy parts need craneLiftKg >= mass. */
    public InstallResult install(PartDef part, int currentAge, double craneLiftKg) {
        if (complete()) return InstallResult.DONE;
        if (part.minAge() > currentAge) return InstallResult.AGE_LOCKED;
        YardDef.Stage st = def.stages().get(stage);
        YardDef.Slot slot = st.slots().stream().filter(s -> s.kind().equals(part.kind())).findFirst().orElse(null);
        if (slot == null) {
            boolean laterStage = def.stages().stream().skip(stage + 1L).anyMatch(s -> s.slots().stream().anyMatch(x -> x.kind().equals(part.kind())));
            return laterStage ? InstallResult.WRONG_STAGE : InstallResult.NO_SLOT;
        }
        if (filled(slot.kind()) >= slot.count()) return InstallResult.NO_SLOT;
        if (part.heavy() && craneLiftKg < part.massKg()) return InstallResult.NEEDS_CRANE;
        stageParts.add(part);
        return InstallResult.OK;
    }

    /** Do work. Returns true when the stage finished and the build moved on. Work only counts once all slots are full. */
    public boolean advance(int workTicks, double craneSpeed) {
        if (complete() || !slotsFull()) return false;
        ticks += (int) Math.max(1, Math.round(workTicks * craneSpeed * def.craneSpeedup()));
        if (ticks < def.stages().get(stage).baseTicks()) return false;
        installed.addAll(stageParts);
        stageParts.clear();
        ticks = 0;
        stage++;
        return true;
    }

    /** 0..1 progress of the current stage, for the scaffold fade. */
    public double stageProgress() {
        return complete() ? 1 : Math.min(1, ticks / (double) def.stages().get(stage).baseTicks());
    }

    /** Stats of everything installed, including parts placed in the current unfinished stage. */
    public Stats stats() {
        List<PartDef> all = new ArrayList<>(installed);
        all.addAll(stageParts);
        return Stats.sum(all);
    }
}
