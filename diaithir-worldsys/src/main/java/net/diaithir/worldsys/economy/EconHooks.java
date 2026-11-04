package net.diaithir.worldsys.economy;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Where other modules plug into the economy. The defaults make a pack without them work: the world is in Age VII, every tech is known, no calendar year,
 * room temperature. The tech, calendar and climate modules set these at startup.
 */
public final class EconHooks {
    private EconHooks() {}

    /** The Age a culture has reached. */
    public static volatile Function<String, String> ageOf = culture -> null;
    /** Does a culture know a tech? */
    public static volatile BiPredicate<String, String> techKnown = (culture, tech) -> true;
    /** The calendar year (negative before the common era), or null when none is published. */
    public static volatile Supplier<Double> calendarYear = () -> null;
    /** The outdoor temperature in C at a town's position, or null for room temperature. */
    public static volatile java.util.function.BiFunction<Double, Double, Double> temperatureAt = (x, z) -> null;
    /** The Age a good opens in (the tech tree's techAgeOf); "I" when unknown. */
    public static volatile Function<String, String> goodAge = id -> "I";
}
