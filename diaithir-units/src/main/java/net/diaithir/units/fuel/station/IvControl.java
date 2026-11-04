package net.diaithir.units.fuel.station;

import java.util.List;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Engine start and stop for Immersive Vehicles, which has no ignition key (its only way is the panel GUI). Works on the nearest vehicle within
 * reach of a player; the vehicle's engines are driven through their own start method and magneto variable, no physics added.
 */
final class IvControl {
    private IvControl() {}

    /** Returns 1 when the engines were started, 0 when stopped, -1 when no vehicle with engines is near. */
    static int toggle(Level level, Vec3 at, double reach, Boolean forceStart) {
        List<Object> near = IvTargets.vehicles(level, new AABB(at, at).inflate(reach));
        Object best = null;
        double bestD = Double.MAX_VALUE;
        for (Object v : near) {
            try {
                Object pos = v.getClass().getField("position").get(v);
                double x = (double) pos.getClass().getField("x").get(pos), y = (double) pos.getClass().getField("y").get(pos), z = (double) pos.getClass().getField("z").get(pos);
                double d = at.distanceToSqr(x, y, z);
                if (d < bestD && v.getClass().getField("engines").get(v) instanceof List<?> l && !l.isEmpty()) { best = v; bestD = d; }
            } catch (ReflectiveOperationException | RuntimeException ex) {
                // skip
            }
        }
        if (best == null) return -1;
        try {
            boolean running = best.getClass().getField("enginesRunning").getBoolean(best);
            boolean start = forceStart != null ? forceStart : !running;
            for (Object engine : (List<?>) best.getClass().getField("engines").get(best)) {
                if (start) {
                    engine.getClass().getMethod("autoStartEngine").invoke(engine);
                } else {
                    Object magneto = engine.getClass().getField("magnetoVar").get(engine);
                    magneto.getClass().getMethod("setTo", double.class, boolean.class).invoke(magneto, 0.0, true);
                }
            }
            return start ? 1 : 0;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            return -1;
        }
    }

    /** Pulls the trigger of every gun on the nearest vehicle for one tick (server-side equivalent of IV's fire key). Returns gun count, -1 when no vehicle. */
    static int trigger(Level level, Vec3 at, double reach, boolean first) {
        Object v = nearest(level, at, reach);
        if (v == null) return -1;
        int n = 0;
        try {
            for (Object part : (List<?>) v.getClass().getField("allParts").get(v)) {
                if (!part.getClass().getSimpleName().equals("PartGun")) continue;
                part.getClass().getField("playerHoldingTrigger").setBoolean(part, true);
                part.getClass().getField("playerPressedTrigger").setBoolean(part, first);
                n++;
            }
        } catch (ReflectiveOperationException | RuntimeException ex) {
            return -1;
        }
        return n;
    }

    private static Object nearest(Level level, Vec3 at, double reach) {
        Object best = null;
        double bestD = Double.MAX_VALUE;
        for (Object v : IvTargets.vehicles(level, new AABB(at, at).inflate(reach))) {
            try {
                Object pos = v.getClass().getField("position").get(v);
                double d = at.distanceToSqr((double) pos.getClass().getField("x").get(pos), (double) pos.getClass().getField("y").get(pos), (double) pos.getClass().getField("z").get(pos));
                if (d < bestD) { best = v; bestD = d; }
            } catch (ReflectiveOperationException | RuntimeException ex) {
                // skip
            }
        }
        return best;
    }

    private static double var(Object vehicle, String field) throws ReflectiveOperationException {
        Object cv = vehicle.getClass().getField(field).get(vehicle);
        return (double) cv.getClass().getMethod("getValue").invoke(cv);
    }

    private static double named(Object vehicle, String key) throws ReflectiveOperationException {
        Object cv = vehicle.getClass().getMethod("getOrCreateVariable", String.class).invoke(vehicle, key);
        return (double) cv.getClass().getMethod("getValue").invoke(cv);
    }

    /** One-line state of the nearest vehicle: engines, fuel, brakes, throttle, speed, mass. Null when none is near. */
    static String status(Level level, Vec3 at, double reach) {
        Object v = nearest(level, at, reach);
        if (v == null) return null;
        StringBuilder sb = new StringBuilder();
        try {
            List<?> engines = (List<?>) v.getClass().getField("engines").get(v);
            try {
                sb.append("creativeVehicle=").append(v.getClass().getField("isCreative").getBoolean(v)).append(' ');
            } catch (ReflectiveOperationException | RuntimeException ex) {
                sb.append("creativeVehicle=? ");
            }
            sb.append("engines=").append(engines.size()).append(" running=").append(v.getClass().getField("enginesRunning").getBoolean(v));
            try {
                sb.append(" rpm=").append(Math.round(named(v, "engine_rpm_1")));
                sb.append(" gear=").append(named(v, "engine_gear_1"));
                sb.append(" fuelFlow=").append(String.format("%.4f", named(v, "engine_fuel_flow_1")));
            } catch (ReflectiveOperationException | RuntimeException ex) {
                sb.append(" rpm=?");
            }
        } catch (ReflectiveOperationException | RuntimeException ex) {
            sb.append("engines=?");
        }
        try {
            Object tank = v.getClass().getField("fuelTank").get(v);
            sb.append(" tankFluid=").append(tank.getClass().getMethod("getFluid").invoke(tank));
            sb.append(" fuel=").append(tank.getClass().getMethod("getFluidLevel").invoke(tank)).append("/").append(tank.getClass().getMethod("getMaxLevel").invoke(tank)).append(" mB");
        } catch (ReflectiveOperationException | RuntimeException ex) {
            sb.append(" fuel=?");
        }
        String[][] vars = {{"throttleVar", "throttle", "throttle"}, {"brakeVar", "brake", "brake"}, {"parkingBrakeVar", "parkingBrake", "parking_brake"}};
        for (String[] f : vars) {
            String text;
            try {
                text = String.format("%.2f", var(v, f[0]));
            } catch (ReflectiveOperationException | RuntimeException ex) {
                try {
                    text = String.format("%.2f", named(v, f[2]));
                } catch (ReflectiveOperationException | RuntimeException ex2) {
                    text = "?";
                }
            }
            sb.append(' ').append(f[1]).append('=').append(text);
        }
        try {
            sb.append(" speed=").append(String.format("%.3f", named(v, "velocity"))).append(" m/s");
            sb.append(" mass=").append(Math.round((double) v.getClass().getMethod("getMass").invoke(v))).append(" kg");
        } catch (ReflectiveOperationException | RuntimeException ex) {
            sb.append(" speed=?");
        }
        return sb.toString();
    }

    /** Sets the throttle (0..1) and releases both brakes on the nearest vehicle; for tests and for tools without a driver seat. False when none near. */
    static boolean drive(Level level, Vec3 at, double reach, double throttle) {
        Object v = nearest(level, at, reach);
        if (v == null) return false;
        try {
            set(v, "throttleVar", throttle);
            set(v, "brakeVar", 0.0);
            set(v, "parkingBrakeVar", 0.0);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            return false;
        }
    }

    private static void set(Object vehicle, String field, double value) throws ReflectiveOperationException {
        Object cv = vehicle.getClass().getField(field).get(vehicle);
        cv.getClass().getMethod("setTo", double.class, boolean.class).invoke(cv, value, true);
    }

    /** Dashboard numbers for the nearest vehicle: {km/h, fuel 0..1, rpm, gear, brake 0..1, engines running 0/1}. Null when none is near or it is idle and unmanned. */
    static double[] dash(Level level, Vec3 at, double reach) {
        Object v = nearest(level, at, reach);
        if (v == null) return null;
        try {
            boolean running = v.getClass().getField("enginesRunning").getBoolean(v);
            double kmh = Math.abs(named(v, "velocity")) * 3.6;
            if (!running && kmh < 0.5) return null;
            Object tank = v.getClass().getField("fuelTank").get(v);
            double max = ((Number) tank.getClass().getMethod("getMaxLevel").invoke(tank)).doubleValue();
            double lvl = ((Number) tank.getClass().getMethod("getFluidLevel").invoke(tank)).doubleValue();
            double rpm = 0, gear = 0;
            try {
                rpm = named(v, "engine_rpm_1");
                gear = named(v, "engine_gear_1");
            } catch (ReflectiveOperationException | RuntimeException ex) {
                // no engine numbers
            }
            double brake;
            try {
                brake = var(v, "brakeVar");
            } catch (ReflectiveOperationException | RuntimeException ex) {
                brake = 0;
            }
            return new double[] {kmh, max > 0 ? lvl / max : 0, rpm, gear, brake, running ? 1 : 0};
        } catch (ReflectiveOperationException | RuntimeException ex) {
            return null;
        }
    }
}
