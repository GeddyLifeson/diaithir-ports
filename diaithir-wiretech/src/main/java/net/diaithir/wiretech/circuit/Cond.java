package net.diaithir.wiretech.circuit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** One comparison. With each, anything or everything on the left it tests every input signal and remembers which ones passed. */
public record Cond(Operand left, Cmp cmp, Operand right) {
    /** pass is the verdict, signals are the ones that passed when the comparison was wild. */
    public record Res(boolean pass, boolean wild, Set<String> signals) {}

    public Res eval(In in) {
        if (!left.isWild()) return new Res(cmp.test(left.value(in), right.value(in)), false, Set.of());
        Set<String> ok = new TreeSet<>();
        Signals all = in.pick(left.mask());
        int rv = right.value(in);
        for (var e : all.entries().entrySet()) if (cmp.test(e.getValue(), rv)) ok.add(e.getKey());
        boolean pass = Operand.ALL.equals(left.signal()) ? ok.size() == all.size() : !ok.isEmpty();
        return new Res(pass, true, pass ? ok : Set.of());
    }

    public String text() {
        return left.text() + " " + cmp.sym + " " + right.text();
    }

    /** Conditions joined by and (tighter) and or: a list of and-clauses. */
    public record Expr(List<List<Cond>> clauses) {
        public Res eval(In in) {
            boolean pass = false, wild = false;
            Set<String> out = new TreeSet<>();
            for (List<Cond> clause : clauses) {
                boolean cp = true, cw = false;
                Set<String> cs = null;
                for (Cond c : clause) {
                    Res r = c.eval(in);
                    cp &= r.pass();
                    if (r.wild()) {
                        cw = true;
                        if (cs == null) cs = new TreeSet<>(r.signals());
                        else cs.retainAll(r.signals());
                    }
                }
                if (!cp) continue;
                pass = true;
                if (cw) {
                    wild = true;
                    out.addAll(cs);
                }
            }
            return new Res(pass, wild, out);
        }

        public boolean test(In in) {
            return eval(in).pass();
        }

        public String text() {
            List<String> parts = new ArrayList<>();
            for (List<Cond> clause : clauses) {
                List<String> c = new ArrayList<>();
                for (Cond x : clause) c.add(x.text());
                parts.add(String.join(" and ", c));
            }
            return String.join(" or ", parts);
        }
    }
}
