package net.diaithir.units.fuel.station;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.diaithir.units.fuel.Fuel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Immersive Vehicles bridge by reflection (no compile dependency on its jar). A vehicle is a Minecraft entity of
 * {@code mcinterface261.BuilderEntityExisting} whose protected field {@code entity} is the vehicle; the vehicle's public
 * {@code fuelTank} is an {@code EntityFluidTank} in millibuckets (1 mB = 0.001 L).
 */
final class IvTargets {
    private static final String BUILDER = "mcinterface261.BuilderEntityExisting";
    private static Boolean present;
    private static Field entityField;

    private IvTargets() {}

    static List<FuelTarget> near(Level level, AABB box) {
        List<FuelTarget> out = new ArrayList<>();
        for (Object vehicle : vehicles(level, box)) {
            try {
                Object tank = vehicle.getClass().getField("fuelTank").get(vehicle);
                if (tank != null) out.add(new Tank(tank));
            } catch (ReflectiveOperationException | RuntimeException ex) {
                // not a powered vehicle, or IV changed: skip it
            }
        }
        return out;
    }

    /** The IV vehicle objects (not the Minecraft entities) inside the box. */
    static List<Object> vehicles(Level level, AABB box) {
        List<Object> out = new ArrayList<>();
        if (!present()) return out;
        for (Entity e : level.getEntities((Entity) null, box, x -> x.getClass().getName().equals(BUILDER))) {
            try {
                if (entityField == null) {
                    entityField = find(e.getClass(), "entity");
                    entityField.setAccessible(true);
                }
                Object vehicle = entityField.get(e);
                if (vehicle != null) out.add(vehicle);
            } catch (ReflectiveOperationException | RuntimeException ex) {
                // not a vehicle
            }
        }
        return out;
    }

    private static boolean present() {
        if (present == null) {
            try {
                Class.forName(BUILDER);
                present = true;
            } catch (ClassNotFoundException ex) {
                present = false;
            }
        }
        return present;
    }

    private static Field find(Class<?> c, String name) throws NoSuchFieldException {
        for (; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(name);
    }

    private record Tank(Object tank) implements FuelTarget {
        private String current() throws ReflectiveOperationException {
            return (String) tank.getClass().getMethod("getFluid").invoke(tank);
        }

        @Override
        public double free(Fuel f) {
            if (f.ivFuelType() == null) return 0;
            try {
                String cur = current();
                if (cur != null && !cur.isEmpty() && !cur.equals(f.ivFuelType())) return 0;
                double level = (double) tank.getClass().getMethod("getFluidLevel").invoke(tank);
                int max = (int) tank.getClass().getMethod("getMaxLevel").invoke(tank);
                return Math.max(0, max - level) / 1000.0;
            } catch (ReflectiveOperationException | RuntimeException ex) {
                return 0;
            }
        }

        @Override
        public double fill(Fuel f, double litres) {
            double ok = Math.min(litres, free(f));
            if (ok <= 0) return 0;
            try {
                String wildcard = (String) tank.getClass().getField("WILDCARD_FLUID_MOD").get(null);
                Method fill = tank.getClass().getMethod("fill", String.class, String.class, double.class, boolean.class);
                return (double) fill.invoke(tank, f.ivFuelType(), wildcard, ok * 1000.0, false) / 1000.0;
            } catch (ReflectiveOperationException | RuntimeException ex) {
                return 0;
            }
        }
    }
}
