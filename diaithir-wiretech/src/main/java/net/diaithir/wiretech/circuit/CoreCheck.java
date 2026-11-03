// S6895 CoreCheck: plain-Java check of the circuit core.
package net.diaithir.wiretech.circuit;

/** Plain-Java checks of the circuit core. Run: javac -d /tmp/cc *.java && java -ea -cp /tmp/cc net.diaithir.wiretech.circuit.CoreCheck */
public final class CoreCheck {
    private static int fails;

    private static void eq(String what, Object want, Object got) {
        if (!want.equals(got)) {
            fails++;
            System.out.println("FAIL " + what + ": want " + want + " got " + got);
        }
    }

    private static Signals sig(Object... kv) {
        Signals s = new Signals();
        for (int i = 0; i < kv.length; i += 2) s.set((String) kv[i], ((Number) kv[i + 1]).longValue());
        return s;
    }

    private static boolean throwsOn(String prog) {
        try {
            Logic.parse(prog);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    public static void main(String[] args) {
        // constant -> arithmetic each * 2
        Sim s = new Sim();
        s.add(1, Tier.ELECTRONIC, "const a=10 b=-3 minecraft:iron_ingot=7");
        s.add(2, Tier.ELECTRONIC, "arith each * 2 -> each");
        s.wire(1, true, 2, false, Color.RED);
        s.run(3);
        eq("each*2", sig("virtual:a", 20, "virtual:b", -6, "minecraft:iron_ingot", 14), s.node(2).out);

        // each + const into a single signal sums
        s = new Sim();
        s.add(1, Tier.ELECTRONIC, "const a=10 b=5");
        s.add(2, Tier.ELECTRONIC, "arith each + 0 -> c");
        s.wire(1, true, 2, false, Color.GREEN);
        s.run(2);
        eq("each->c sum", sig("virtual:c", 15), s.node(2).out);

        // counter: output wired back to input, a + 1 -> a, adds one per tick
        s = new Sim();
        s.add(1, Tier.ELECTRONIC, "arith a + 1 -> a");
        s.wire(1, true, 1, false, Color.RED);
        s.run(10);
        eq("counter", 10, s.node(1).out.get("virtual:a"));

        // decider gate
        s = new Sim();
        s.add(1, Tier.ELECTRONIC, "const a=6");
        s.add(2, Tier.ELECTRONIC, "decide a > 5 -> b one");
        s.add(3, Tier.ELECTRONIC, "decide a > 5 and a < 6 or a = 6 -> a input");
        s.wire(1, true, 2, false, Color.RED);
        s.wire(1, true, 3, false, Color.RED);
        s.run(2);
        eq("decider one", 1, s.node(2).out.get("virtual:b"));
        eq("decider and/or input", 6, s.node(3).out.get("virtual:a"));
        s.node(1).logic = Logic.parse("const a=5");
        s.run(3);
        eq("decider off", true, s.node(2).out.isEmpty());

        // colours stay apart
        s = new Sim();
        s.add(1, Tier.ELECTRONIC, "const a=9");
        s.add(2, Tier.ELECTRONIC, "decide red:a > 0 -> x one");
        s.add(3, Tier.ELECTRONIC, "decide green:a > 0 -> x one");
        s.wire(1, true, 2, false, Color.GREEN);
        s.wire(1, true, 3, false, Color.GREEN);
        s.run(2);
        eq("red sees nothing on green wire", true, s.node(2).out.isEmpty());
        eq("green sees it", 1, s.node(3).out.get("virtual:x"));

        // two outputs on one network add
        s = new Sim();
        s.add(1, Tier.ELECTRONIC, "const a=4");
        s.add(2, Tier.ELECTRONIC, "const a=3 b=1");
        s.add(3, Tier.ELECTRONIC, "arith a + 0 -> a");
        s.wire(1, true, 2, true, Color.RED);
        s.wire(2, true, 3, false, Color.RED);
        s.run(2);
        eq("network sum", 7, s.seen(3).red().get("virtual:a"));

        // each / anything / everything
        s = new Sim();
        s.add(1, Tier.ELECTRONIC, "const a=10 b=2 c=30");
        s.add(2, Tier.ELECTRONIC, "decide each > 5 -> each input");
        s.add(3, Tier.ELECTRONIC, "decide anything > 25 -> z one");
        s.add(4, Tier.ELECTRONIC, "decide everything > 1 -> y one");
        s.add(5, Tier.ELECTRONIC, "decide everything > 5 -> y one");
        for (int i = 2; i <= 5; i++) s.wire(1, true, i, false, Color.RED);
        s.run(2);
        eq("each passing", sig("virtual:a", 10, "virtual:c", 30), s.node(2).out);
        eq("anything", 1, s.node(3).out.get("virtual:z"));
        eq("everything true", 1, s.node(4).out.get("virtual:y"));
        eq("everything false", true, s.node(5).out.isEmpty());

        // selector
        s = new Sim();
        s.add(1, Tier.ELECTRONIC, "const a=10 b=2 c=30");
        s.add(2, Tier.ELECTRONIC, "select max 0");
        s.add(3, Tier.ELECTRONIC, "select min 1");
        s.add(4, Tier.ELECTRONIC, "select count -> n");
        s.add(5, Tier.ELECTRONIC, "select random 5");
        s.add(6, Tier.ELECTRONIC, "select stack");
        for (int i = 2; i <= 6; i++) s.wire(1, true, i, false, Color.RED);
        s.run(2);
        eq("max", sig("virtual:c", 30), s.node(2).out);
        eq("min rank 1", sig("virtual:a", 10), s.node(3).out);
        eq("count", 3, s.node(4).out.get("virtual:n"));
        eq("random picks one", 1, s.node(5).out.size());

        // arithmetic edge cases
        eq("div0", 0L, ArithOp.DIV.apply(5, 0));
        eq("mod0", 0L, ArithOp.MOD.apply(5, 0));
        eq("pow", 1024L, ArithOp.POW.apply(2, 10));
        eq("negpow", 0L, ArithOp.POW.apply(2, -1));
        eq("shl", 8L, ArithOp.SHL.apply(1, 3));
        eq("xor", 6L, ArithOp.XOR.apply(5, 3));
        eq("saturate", Integer.MAX_VALUE, new Signals().add("x", Integer.MAX_VALUE).add("x", 5).get("x"));

        // tiers: mechanical clamps to 99 and updates once a second, relay every 4 ticks
        s = new Sim();
        s.add(1, Tier.ELECTRONIC, "const a=500");
        s.add(2, Tier.MECHANICAL, "arith a + 0 -> a");
        s.add(3, Tier.RELAY, "arith a + 0 -> a");
        s.wire(1, true, 2, false, Color.RED);
        s.wire(1, true, 3, false, Color.RED);
        s.run(2);
        eq("mechanical first update (tick 0 saw nothing yet)", true, s.node(2).out.isEmpty());
        s.run(20);
        eq("mechanical clamp", 99, s.node(2).out.get("virtual:a"));
        eq("relay value", 500, s.node(3).out.get("virtual:a"));
        Signals many = sig("a", 1, "b", 1, "c", 1, "d", 1, "e", 1);
        eq("mechanical type limit", 4, many.limited(99, Tier.MECHANICAL.maxTypes).size());

        // text round trip and errors
        for (String p : new String[] {"const virtual:a=10 minecraft:iron_ingot=5", "arith each * 2 -> each", "arith red:a << 3 -> b", "decide a > 5 and green:b < 3 or c = 1 -> d input",
                "decide each > 0 -> each one", "select max 2", "select count -> n", "select random 40", "select stack", "when each > 100"}) {
            String t = Logic.parse(p).text();
            eq("round trip " + p, t, Logic.parse(t).text());
        }
        for (String bad : new String[] {"", "frob a", "arith a + -> b", "arith a ?? 2 -> b", "arith 1 + 2 -> everything", "decide a > -> b", "decide a > 1 b", "arith a + each -> b", "const a", "select sideways", "arith 99999999999 + 1 -> a"}) {
            eq("rejects '" + bad + "'", true, throwsOn(bad));
        }
        System.out.println(fails == 0 ? "CORE CHECK PASS" : "CORE CHECK FAIL " + fails);
        if (fails > 0) System.exit(1);
    }
}
