package net.diaithir.animation.machines;

import java.nio.file.Files;
import java.nio.file.Path;

/** Dependency-free tests: javac + java, see tools/run_tests.sh. */
public final class TestMain {
    static int fails;

    static void check(boolean ok, String what) {
        if (!ok) { fails++; System.out.println("FAIL " + what); }
    }

    public static void main(String[] a) throws Exception {
        Rules rules = Rules.parse(Files.readString(Path.of(a[0])));
        check(rules.size() > 40, "rules loaded " + rules.size());
        check(rules.match("diaithircrafting:foundry_operator_seat").get().spec().kind() == Kind.PULSE, "operator seat pulses");
        check(rules.match("minecraft:oak_door").get().spec().kind() == Kind.SWING, "door swings");
        check(rules.match("minecraft:chest").get().spec().kind() == Kind.SWING, "chest lid");
        check(rules.match("minecraft:lever").get().spec().kind() == Kind.SWING, "lever");
        check(rules.match("create:cogwheel").get().spec().kind() == Kind.SPIN, "cog spins");
        check(rules.match("diaithir:upgrade_beacon").get().spec().kind() == Kind.HOVER, "beacon hovers");
        check(rules.match("Keelwright ship piloting").get().spec().kind() == Kind.SPIN, "bone rule beats keyword");
        check(rules.match("minecraft:stone").isEmpty(), "stone has no rule");

        // SPIN: idle stopped, winds up when working
        Controller c = new Controller(new Spec(Kind.SPIN, 'y', 90f, 0f, 0f, 0f, Easing.LINEAR));
        for (int i = 0; i < 20; i++) c.tick(0.05f);
        check(c.pose().ry() == 0f, "idle spin stopped");
        c.setState(State.WORKING);
        for (int i = 0; i < 100; i++) c.tick(0.05f);
        check(c.activity() > 0.99f, "wound up " + c.activity());
        float r1 = c.pose().ry(); c.tick(0.05f);
        check(c.pose().ry() != r1, "spins when working");
        c.setState(State.DONE);
        for (int i = 0; i < 100; i++) c.tick(0.05f);
        check(c.activity() < 0.01f, "winds down when done");

        // SWING: closes and opens fully, never overshoots
        Controller d = new Controller(new Spec(Kind.SWING, 'y', 3f, 90f, 0f, 1f, Easing.IN_OUT));
        d.setState(State.WORKING);
        for (int i = 0; i < 100; i++) { d.tick(0.05f); check(d.pose().ry() <= 90.0001f, "no overshoot"); }
        check(Math.abs(d.pose().ry() - 90f) < 0.01f, "fully open");
        d.setState(State.IDLE);
        for (int i = 0; i < 100; i++) d.tick(0.05f);
        check(Math.abs(d.pose().ry()) < 0.01f, "closed again");

        // PISTON stays within [0, amount]
        Controller p = new Controller(new Spec(Kind.PISTON, 'y', 1f, 0.5f, 0f, 0f, Easing.LINEAR));
        p.setState(State.WORKING);
        for (int i = 0; i < 400; i++) { p.tick(0.05f); float y = p.pose().oy(); check(y >= -1e-4f && y <= 0.5001f, "piston range " + y); }

        // AIM
        Controller t = new Controller(new Spec(Kind.AIM, 'y', 1f, 180f, 1f, 1f, Easing.LINEAR));
        t.aim(45f, -10f);
        check(t.pose().ry() == 45f && t.pose().rx() == -10f, "aim");

        check(Easing.OUT_BOUNCE.apply(1f) > 0.999f && Easing.IN_OUT.apply(0f) == 0f, "easing ends");
        check(Rules.glob("*door*", "iron_door_top") && !Rules.glob("door*", "iron_door"), "glob");
        System.out.println(fails == 0 ? "ALL PASS" : fails + " FAILED");
        System.exit(fails == 0 ? 0 : 1);
    }
}
