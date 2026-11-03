package net.diaithir.units;

/** Player-facing text: SI prefixes for J, W, L and bar-less quantities, degrees C for temperature. */
public final class Fmt {
    private Fmt() {}

    private static final String[] PREFIX = {"", "k", "M", "G", "T"};

    /** Formats a canonical amount of an SI-prefixed quantity, e.g. 1500000 J as "1.5 MJ". */
    public static String si(double canonical, String baseSymbol) {
        double v = Math.abs(canonical);
        int i = 0;
        while (v >= 1000 && i < PREFIX.length - 1) {
            v /= 1000;
            i++;
        }
        return trim(Math.signum(canonical) * v) + " " + PREFIX[i] + baseSymbol;
    }

    public static String energy(double joules) { return si(joules, "J"); }

    public static String power(double watts) { return si(watts, "W"); }

    /** Litres below one cubic metre, then m³. */
    public static String volume(double litres) {
        return Math.abs(litres) >= 1000 ? trim(litres / 1000) + " m³" : trim(litres) + " L";
    }

    public static String temperature(double kelvin) { return trim(kelvin - 273.15) + " °C"; }

    public static String pressure(double bar) { return trim(bar) + " bar"; }

    private static String trim(double v) {
        String s = String.format(java.util.Locale.ROOT, "%.2f", v);
        s = s.indexOf('.') < 0 ? s : s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s;
    }
}
