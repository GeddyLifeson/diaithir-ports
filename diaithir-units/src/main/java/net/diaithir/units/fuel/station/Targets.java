package net.diaithir.units.fuel.station;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** Where pumps find things to fuel. Other modules add a source (rail tank cars, ships); Immersive Vehicles is added when it is present. */
public final class Targets {
    public static final List<BiFunction<Level, AABB, List<FuelTarget>>> SOURCES = new CopyOnWriteArrayList<>();

    static {
        SOURCES.add(IvTargets::near);
    }

    public static List<FuelTarget> near(Level level, AABB box) {
        List<FuelTarget> out = new ArrayList<>();
        for (var s : SOURCES) out.addAll(s.apply(level, box));
        return out;
    }

    private Targets() {}
}
