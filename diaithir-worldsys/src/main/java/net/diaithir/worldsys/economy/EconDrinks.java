package net.diaithir.worldsys.economy;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** What a bottle is worth: stronger and oak-aged bottles fetch more than the table price. Port of 06_drinks.js. */
public final class EconDrinks {
    private EconDrinks() {}

    public static final double AGED_PREMIUM = 0.5;

    /** A drink's style data from drink_value.json: abv, whether it improves in oak, and the days at which it peaks. */
    public record Style(double abv, boolean aged, double peak) {}
    public record Bottle(double abv, double aged) {}

    private static final Pattern ABV = Pattern.compile("abv:([0-9.]+)d"), AGED = Pattern.compile("aged:([0-9.]+)d");

    public static double factor(Map<String, Style> styles, String id, double abv, double aged) {
        Style rec = styles.get(id.substring(id.indexOf(':') + 1));
        if (rec == null) return 1;
        double strength = abv > 0 && rec.abv() > 0 ? Math.pow(abv / rec.abv(), 0.5) : 1;
        double age = rec.aged() && rec.peak() > 0 ? 1 + AGED_PREMIUM * Math.min(1, Math.max(0, aged) / Math.min(12, rec.peak())) : 1;
        return strength * age;
    }

    /** The bottle data in a stack's item string ("diaithir_drinks:stout[custom_data={abv:5.2d,aged:6.0d}]"); 0 when absent. */
    public static Bottle bottleData(String text) {
        Matcher a = ABV.matcher(text), g = AGED.matcher(text);
        return new Bottle(a.find() ? Double.parseDouble(a.group(1)) : 0, g.find() ? Double.parseDouble(g.group(1)) : 0);
    }
}
