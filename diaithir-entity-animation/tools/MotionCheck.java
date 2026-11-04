import net.diaithir.entityanimation.betteranimations.Motion;

/** javac -d /tmp/m src/main/java/net/diaithir/entityanimation/betteranimations/Motion.java tools/MotionCheck.java && java -cp /tmp/m MotionCheck */
public class MotionCheck {
    static int ok, bad;
    static void check(boolean c, String w) { if (c) ok++; else { bad++; System.out.println("BAD " + w); } }
    public static void main(String[] a) {
        Motion.State s = new Motion.State(0);
        for (int i = 0; i < 600; i++) Motion.spring(s, 1.0, 8.0, 1 / 60.0);
        check(Math.abs(s.pos - 1.0) < 1e-6 && Math.abs(s.vel) < 1e-6, "spring converges");
        Motion.State p = new Motion.State(0); double max = 0;
        for (int i = 0; i < 600; i++) { Motion.spring(p, 1.0, 8.0, 1 / 60.0); max = Math.max(max, p.pos); }
        check(max <= 1.0 + 1e-9, "no overshoot from rest");
        Motion.State b = new Motion.State(0), c = new Motion.State(0);
        for (int i = 0; i < 60; i++) Motion.spring(b, 1.0, 8.0, 1 / 60.0);
        for (int i = 0; i < 30; i++) Motion.spring(c, 1.0, 8.0, 1 / 30.0);
        check(Math.abs(b.pos - c.pos) < 1e-9, "frame-rate independent");
        check(Math.abs(Motion.wobble(0, 1, 1, 0)) < 1e-12 && Math.abs(Motion.wobble(0.25, 1, 1, 0) - 1.0) < 1e-12, "wobble sine");
        check(Math.abs(Motion.wobble(0.1, 1, 1, 3) - Motion.wobble(0.1, 1, 1, 4)) > 1e-3, "phase differs per entity");
        check(Motion.jiggle(-1, 1, 3, 0.2) == 0 && Motion.jiggle(0, 1, 3, 0.2) == 1.0 && Math.abs(Motion.jiggle(0.2, 1, 5, 0.2) - 0.5) < 1e-12, "jiggle decay");
        check(Motion.scale(1, 10, 32) == 1.0 && Motion.scale(1, 32, 32) == 0.0 && Math.abs(Motion.scale(2, 24, 32) - 1.0) < 1e-12, "scale fade");
        System.out.println("motion ok=" + ok + " bad=" + bad);
        if (bad > 0) System.exit(1);
    }
}
