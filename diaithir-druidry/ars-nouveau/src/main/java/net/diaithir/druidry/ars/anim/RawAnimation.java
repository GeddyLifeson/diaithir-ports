package net.diaithir.druidry.ars.anim;

import java.util.ArrayList;
import java.util.List;

/** Minimal clip sequence (API shape of GeckoLib's RawAnimation). The names are read by the renderers' procedural poses. */
public final class RawAnimation {
    public enum Mode { PLAY_ONCE, LOOP, HOLD }
    public record Stage(String name, Mode mode) {}
    private final List<Stage> stages = new ArrayList<>();
    private RawAnimation() {}
    public static RawAnimation begin() { return new RawAnimation(); }
    public RawAnimation thenPlay(String n) { stages.add(new Stage(n, Mode.PLAY_ONCE)); return this; }
    public RawAnimation then(String n, Mode m) { stages.add(new Stage(n, m)); return this; }
    public RawAnimation thenLoop(String n) { stages.add(new Stage(n, Mode.LOOP)); return this; }
    public RawAnimation thenPlayAndHold(String n) { stages.add(new Stage(n, Mode.HOLD)); return this; }
    public RawAnimation thenWait(int ticks) { return this; }
    public List<Stage> stages() { return stages; }
    /** Name of the clip that is current (the last queued stage). */
    public String current() { return stages.isEmpty() ? "idle" : stages.get(stages.size() - 1).name(); }
}
