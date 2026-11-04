package org.diaithir.mega.site;

import org.diaithir.mega.site.Blueprints.Kind;
import org.diaithir.mega.site.Blueprints.Stage;
import org.diaithir.mega.site.Blueprints.Structure;

/**
 * Pure staged-construction logic (no Minecraft types, so it is unit-testable). One engine per site office.
 * Work in a stage is limited by builder count x rate, by delivered material in the ledger, and stops while the crew is unpaid.
 */
public final class StageEngine {
    public final Structure structure;
    private int stage;
    private double done;       // work done in the current stage (m3 or t)
    private int builders;      // builders on site, capped at the stage fleet

    public StageEngine(Structure s, int stage, double done, int builders) {
        this.structure = s;
        this.stage = stage;
        this.done = done;
        this.builders = builders;
    }

    public int stageIndex() { return stage; }
    public double done() { return done; }
    public int builders() { return builders; }
    public void setBuilders(int n) { builders = Math.max(0, n); }
    public boolean finished() { return stage >= structure.stages().size(); }
    public Stage current() { return finished() ? null : structure.stages().get(stage); }

    public enum Block { WORKING, FINISHED, NO_BUILDERS, UNPAID, NO_MATERIAL }

    /** Advances by dt seconds of game time. Returns why it did or did not progress. */
    public Block tick(double dt, boolean paid, Ledger ledger) {
        Stage s = current();
        if (s == null) return Block.FINISHED;
        if (!paid) return Block.UNPAID;
        int n = s.kind() == Kind.REMOTE ? 1 : Math.min(builders, s.fleet());
        if (n <= 0) return Block.NO_BUILDERS;
        double work = Math.min(s.rate() * n * dt, s.amount() - done);
        for (String tag : s.materials().keySet()) work = Math.min(work, ledger.get(tag) / s.perUnit(tag));
        if (work <= 1e-12) return Block.NO_MATERIAL;
        for (String tag : s.materials().keySet()) ledger.take(tag, work * s.perUnit(tag));
        done += work;
        if (done >= s.amount() - 1e-9) { stage++; done = 0; }
        return Block.WORKING;
    }

    /** Share of the finished structure's function that is live: climbs from the last stage's share to this stage's as the work completes. */
    public double functionShare() {
        if (finished()) return 1.0;
        double prev = stage == 0 ? 0 : structure.stages().get(stage - 1).functionShare();
        Stage s = current();
        return prev + (s.functionShare() - prev) * (done / s.amount());
    }

    /** Net power in W (+ out, - in) at the current function share. */
    public double watts() { return structure.powerMW() * 1e6 * functionShare(); }

    /** Blocks of the whole structure that should exist now (dig stages count blocks removed). */
    public int blocksDone() {
        int n = 0;
        for (int i = 0; i < stage && i < structure.stages().size(); i++) n += structure.stages().get(i).blocks();
        Stage s = current();
        return s == null ? n : n + (int) Math.floor(s.blocks() * (done / s.amount()));
    }
}
