package net.diaithir.fauna.bees;

/** A bee's genes as one string, "species/productivity/endurance" (traits 0 to 3). Productivity speeds combs by 25 percent a level; endurance 1 lets it work in rain, 2 also after dark. */
public final class Genes {
    public static final int MAX = 3;

    private Genes() {}

    public static String of(String species, int prod, int end) {
        return species + "/" + Math.max(0, Math.min(MAX, prod)) + "/" + Math.max(0, Math.min(MAX, end));
    }

    public static String species(String g) {
        int i = g.indexOf('/');
        return i < 0 ? g : g.substring(0, i);
    }

    private static int part(String g, int n) {
        String[] p = g.split("/");
        if (p.length <= n) return 0;
        try { return Math.max(0, Math.min(MAX, Integer.parseInt(p[n]))); } catch (NumberFormatException e) { return 0; }
    }

    public static int prod(String g) { return part(g, 1); }

    public static int end(String g) { return part(g, 2); }

    public static boolean valid(String g) { return BeeSpecies.byId(species(g)) != null; }

    /** A child's trait: the parents' average, rounded randomly, then nudged by one one time in three. */
    public static int mix(int a, int b, net.minecraft.util.RandomSource r) {
        int v = (a + b + r.nextInt(2)) / 2;
        int roll = r.nextInt(6);
        if (roll == 0) v++;
        else if (roll == 1) v--;
        return Math.max(0, Math.min(MAX, v));
    }
}
