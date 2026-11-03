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
}
