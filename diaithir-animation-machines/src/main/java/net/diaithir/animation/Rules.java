package net.diaithir.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Ordered rule list: first match wins, so write specific rules before generic ones.
 * Line format (tab separated, '#' starts a comment):
 *   pattern  kind  axis  speed  amount  idleFactor  doneFactor  easing  category
 * Pattern is a block/entity id or path glob with '*': "ns:name", "ns:*", "*door*", "ns:*_gear".
 * A pattern without ':' matches the path in any namespace.
 */
public final class Rules {
    public record Rule(String pattern, Spec spec, String category) {
        boolean matches(String id) {
            int c = pattern.indexOf(':');
            String ns = null, p = pattern;
            if (c >= 0) { ns = pattern.substring(0, c); p = pattern.substring(c + 1); }
            int ic = id.indexOf(':');
            String ins = ic >= 0 ? id.substring(0, ic) : "minecraft";
            String ipath = ic >= 0 ? id.substring(ic + 1) : id;
            if (ns != null && !glob(ns, ins)) return false;
            return glob(p, ipath);
        }
    }

    private final List<Rule> rules = new ArrayList<>();

    public static Rules parse(String text) {
        Rules r = new Rules();
        int n = 0;
        for (String line : text.split("\n")) {
            n++;
            line = line.strip();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] f = line.split("\t");
            if (f.length < 9) throw new IllegalArgumentException("line " + n + ": need 9 fields, got " + f.length);
            Spec s = new Spec(Kind.valueOf(f[1]), f[2].charAt(0), Float.parseFloat(f[3]), Float.parseFloat(f[4]),
                    Float.parseFloat(f[5]), Float.parseFloat(f[6]), Easing.valueOf(f[7]));
            r.rules.add(new Rule(f[0], s, f[8]));
        }
        return r;
    }

    public Optional<Rule> match(String id) {
        for (Rule r : rules) if (r.matches(id)) return Optional.of(r);
        return Optional.empty();
    }

    public int size() { return rules.size(); }

    static boolean glob(String pat, String s) {
        int p = 0, i = 0, star = -1, mark = 0;
        while (i < s.length()) {
            if (p < pat.length() && pat.charAt(p) == '*') { star = p++; mark = i; }
            else if (p < pat.length() && pat.charAt(p) == s.charAt(i)) { p++; i++; }
            else if (star >= 0) { p = star + 1; i = ++mark; }
            else return false;
        }
        while (p < pat.length() && pat.charAt(p) == '*') p++;
        return p == pat.length();
    }
}
