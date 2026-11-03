package net.diaithir.wiretech.circuit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.ToIntFunction;

/**
 * What a node does with its input. Pure functions of the input, so the whole circuit is reproducible and testable without a world.
 * Text form (used by the /circuit command and saved in the block):
 * <pre>
 *   const a=10 minecraft:iron_ingot=5          constant combinator
 *   arith each * 2 -> each                     arithmetic: left op right -> out (ops + - * / % ^ &lt;&lt; &gt;&gt; and or xor)
 *   decide a &gt; 5 and red:b &lt; 3 -> c input      decider: conditions (and binds tighter than or) -> out, one or input
 *   select max 0 | select min 2 | select count -> a | select random 40 | select stack
 *   when each &gt; 100                            gate, lamp and speaker condition
 * </pre>
 */
public sealed interface Logic permits Logic.Constant, Logic.Arith, Logic.Decider, Logic.Selector, Logic.When {
    /** salt differs per node so random selectors do not move in step. */
    record Ctx(long tick, long salt, ToIntFunction<String> stackSize) {}

    Signals output(In in, Ctx ctx);

    String text();

    record Constant(Signals values) implements Logic {
        public Signals output(In in, Ctx ctx) {
            return values.copy();
        }

        public String text() {
            StringBuilder b = new StringBuilder("const");
            for (Map.Entry<String, Integer> e : values.entries().entrySet()) b.append(' ').append(Parse.short_(e.getKey())).append('=').append(e.getValue());
            return b.toString();
        }
    }

    record Arith(Operand left, ArithOp op, Operand right, String out) implements Logic {
        public Signals output(In in, Ctx ctx) {
            Signals r = new Signals();
            int rv = right.value(in);
            if (Operand.EACH.equals(left.signal())) {
                for (var e : in.pick(left.mask()).entries().entrySet()) {
                    long v = op.apply(e.getValue(), rv);
                    r.add(Operand.EACH.equals(out) ? e.getKey() : out, v);
                }
            } else {
                r.add(out, op.apply(left.value(in), rv));
            }
            return r;
        }

        public String text() {
            return "arith " + left.text() + " " + op.sym + " " + right.text() + " -> " + Parse.short_(out);
        }
    }

    record Decider(Cond.Expr when, String out, boolean copyInput) implements Logic {
        public Signals output(In in, Ctx ctx) {
            Cond.Res res = when.eval(in);
            Signals r = new Signals();
            if (!res.pass()) return r;
            Signals all = in.pick(Operand.BOTH);
            if (Operand.EACH.equals(out)) {
                for (String s : res.wild() ? res.signals() : all.entries().keySet()) r.add(s, copyInput ? all.get(s) : 1);
            } else if (Operand.ALL.equals(out)) {
                for (var e : all.entries().entrySet()) r.add(e.getKey(), copyInput ? e.getValue() : 1);
            } else {
                r.add(out, copyInput ? all.get(out) : 1);
            }
            return r;
        }

        public String text() {
            return "decide " + when.text() + " -> " + Parse.short_(out) + (copyInput ? " input" : " one");
        }
    }

    enum Mode { MAX, MIN, COUNT, RANDOM, STACK }

    /** Picks, counts or measures the input signals. index is the rank for max and min, arg is the interval in ticks for random. */
    record Selector(Mode mode, int arg, String out) implements Logic {
        public Signals output(In in, Ctx ctx) {
            Signals all = in.pick(Operand.BOTH);
            Signals r = new Signals();
            switch (mode) {
                case MAX, MIN -> {
                    List<Map.Entry<String, Integer>> l = new ArrayList<>(all.entries().entrySet());
                    l.sort((a, b) -> mode == Mode.MAX ? Integer.compare(b.getValue(), a.getValue()) : Integer.compare(a.getValue(), b.getValue()));
                    if (arg >= 0 && arg < l.size()) r.set(l.get(arg).getKey(), l.get(arg).getValue());
                }
                case COUNT -> r.set(out, all.size());
                case RANDOM -> {
                    if (all.isEmpty()) break;
                    String[] ids = all.entries().keySet().toArray(new String[0]);
                    String pick = ids[new Random(ctx.salt() * 31 + ctx.tick() / Math.max(1, arg)).nextInt(ids.length)];
                    r.set(pick, all.get(pick));
                }
                case STACK -> {
                    for (String id : all.entries().keySet()) if (id.startsWith("minecraft:") || id.indexOf(':') > 0) r.set(id, ctx.stackSize().applyAsInt(id));
                }
            }
            return r;
        }

        public String text() {
            return switch (mode) {
                case MAX -> "select max " + arg;
                case MIN -> "select min " + arg;
                case COUNT -> "select count -> " + Parse.short_(out);
                case RANDOM -> "select random " + arg;
                case STACK -> "select stack";
            };
        }
    }

    /** Condition only, for blocks that act (gate, lamp, speaker). Outputs nothing to the network. */
    record When(Cond.Expr cond) implements Logic {
        public Signals output(In in, Ctx ctx) {
            return Signals.EMPTY;
        }

        public boolean test(In in) {
            return cond.test(in);
        }

        public String text() {
            return "when " + cond.text();
        }
    }

    /** Text to logic. Throws IllegalArgumentException with a message fit for the player. */
    static Logic parse(String text) {
        return Parse.parse(text);
    }

    final class Parse {
        private Parse() {}

        static String short_(String id) {
            return id.startsWith("virtual:") ? id.substring(8) : id.startsWith("minecraft:") ? id.substring(10) : id;
        }

        static String name(String t) {
            String s = t.toLowerCase();
            if (s.matches("[a-z]")) return "virtual:" + s;
            if (s.indexOf(':') >= 0) return s;
            if (!s.matches("[a-z0-9_./-]+")) throw new IllegalArgumentException("Bad signal name: " + t);
            return "minecraft:" + s;
        }

        static Operand operand(String t) {
            if (t.matches("-?\\d+")) {
                try {
                    return Operand.of(Integer.parseInt(t));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Number too large: " + t);
                }
            }
            int mask = Operand.BOTH;
            String s = t.toLowerCase();
            if (s.startsWith("red:")) { mask = Operand.RED; s = s.substring(4); }
            else if (s.startsWith("green:")) { mask = Operand.GREEN; s = s.substring(6); }
            if (s.equals(Operand.EACH) || s.equals(Operand.ANY) || s.equals(Operand.ALL)) return new Operand(s, 0, mask);
            return new Operand(name(s), 0, mask);
        }

        static String out(String t) {
            String s = t.toLowerCase();
            if (s.equals(Operand.EACH) || s.equals(Operand.ALL)) return s;
            return name(s);
        }

        static Logic parse(String text) {
            String[] t = text.trim().split("\\s+");
            if (t.length == 0 || t[0].isEmpty()) throw new IllegalArgumentException("Empty program.");
            String[] a = Arrays.copyOfRange(t, 1, t.length);
            try {
                return switch (t[0].toLowerCase()) {
                    case "const" -> constant(a);
                    case "arith" -> arith(a);
                    case "decide" -> decide(a);
                    case "select" -> select(a);
                    case "when" -> new When(expr(a, a.length));
                    default -> throw new IllegalArgumentException("Unknown program '" + t[0] + "'. Use const, arith, decide, select or when.");
                };
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Program is incomplete: " + text);
            }
        }

        private static Logic constant(String[] a) {
            Signals s = new Signals();
            for (String p : a) {
                int i = p.lastIndexOf('=');
                if (i <= 0) throw new IllegalArgumentException("Use signal=value, got " + p);
                Operand o = operand(p.substring(0, i));
                if (o.isConstant() || o.isWild()) throw new IllegalArgumentException("Not a signal: " + p);
                try {
                    s.set(o.signal(), Long.parseLong(p.substring(i + 1)));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Not a number: " + p);
                }
            }
            return new Constant(s);
        }

        private static Logic arith(String[] a) {
            if (a.length != 5 || !a[3].equals("->")) throw new IllegalArgumentException("Use: arith <left> <op> <right> -> <out>");
            Operand l = operand(a[0]), r = operand(a[2]);
            ArithOp op = ArithOp.of(a[1].toLowerCase());
            if (op == null) throw new IllegalArgumentException("Unknown operator " + a[1] + ". Use + - * / % ^ << >> and or xor.");
            if (r.isWild() || (l.isWild() && !Operand.EACH.equals(l.signal()))) throw new IllegalArgumentException("Only each may stand on the left of arith.");
            String out = out(a[4]);
            if (Operand.ALL.equals(out)) throw new IllegalArgumentException("Arith cannot output everything.");
            if (Operand.EACH.equals(out) && !Operand.EACH.equals(l.signal())) throw new IllegalArgumentException("Output each needs each on the left.");
            return new Arith(l, op, r, out);
        }

        private static Logic decide(String[] a) {
            int arrow = Arrays.asList(a).indexOf("->");
            if (arrow < 0 || a.length - arrow < 2) throw new IllegalArgumentException("Use: decide <conditions> -> <out> [one|input]");
            Cond.Expr e = expr(a, arrow);
            String out = out(a[arrow + 1]);
            boolean copy = a.length > arrow + 2 && a[arrow + 2].equalsIgnoreCase("input");
            if (a.length > arrow + 2 && !copy && !a[arrow + 2].equalsIgnoreCase("one")) throw new IllegalArgumentException("Output mode is one or input.");
            return new Decider(e, out, copy);
        }

        private static Cond.Expr expr(String[] a, int end) {
            List<List<Cond>> clauses = new ArrayList<>();
            List<Cond> cur = new ArrayList<>();
            int i = 0;
            while (i < end) {
                if (i + 3 > end) throw new IllegalArgumentException("A condition is three words: <left> <cmp> <right>.");
                Operand l = operand(a[i]);
                Cmp c = Cmp.of(a[i + 1]);
                if (c == null) throw new IllegalArgumentException("Unknown comparison " + a[i + 1] + ". Use < > = >= <= !=.");
                Operand r = operand(a[i + 2]);
                if (r.isWild()) throw new IllegalArgumentException("Only the left side may be each, anything or everything.");
                cur.add(new Cond(l, c, r));
                i += 3;
                if (i < end) {
                    String j = a[i].toLowerCase();
                    if (j.equals("or")) { clauses.add(cur); cur = new ArrayList<>(); }
                    else if (!j.equals("and")) throw new IllegalArgumentException("Join conditions with and or or, got " + a[i]);
                    i++;
                    if (i >= end) throw new IllegalArgumentException("Dangling " + j + ".");
                }
            }
            if (cur.isEmpty()) throw new IllegalArgumentException("No condition given.");
            clauses.add(cur);
            return new Cond.Expr(clauses);
        }

        private static Logic select(String[] a) {
            return switch (a[0].toLowerCase()) {
                case "max" -> new Selector(Mode.MAX, a.length > 1 ? Integer.parseInt(a[1]) : 0, null);
                case "min" -> new Selector(Mode.MIN, a.length > 1 ? Integer.parseInt(a[1]) : 0, null);
                case "count" -> new Selector(Mode.COUNT, 0, out(a[2]));
                case "random" -> new Selector(Mode.RANDOM, Math.max(1, a.length > 1 ? Integer.parseInt(a[1]) : 20), null);
                case "stack" -> new Selector(Mode.STACK, 0, null);
                default -> throw new IllegalArgumentException("Select mode is max, min, count, random or stack.");
            };
        }
    }
}
