package org.diaithir.archimedes;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Carries entities that stand on a ship's deck along with it, so you can walk about on a moving ship (the Archimedes port had no walking inside).
 * Before the ship moves, {@link #collect} records each free entity standing on a deck cell as a point in ship-local space; after the ship has moved
 * and turned, {@link #carry} puts it back at the same local point and turns it by the ship's yaw change. Idea after aleki's Nifty Ships (MIT).
 */
public final class DeckWalk {
    /** How far above a floor an entity's feet may be and still count as standing on it, in blocks. */
    public static final double STAND_TOLERANCE = 0.15;
    /** An entity this far above its floor (a fall, a jump) is left behind. */
    public static final double STEP_UP = 0.6;

    private DeckWalk() {}

    /** Entity feet position in ship-local space, for every non-riding entity standing on a deck cell. */
    public static Map<Entity, Vec3> collect(ShipEntity ship) {
        Map<Entity, Vec3> out = new LinkedHashMap<>();
        if (ship.level().isClientSide() || !ArchimedesConfig.DECK_WALK.get()) return out;
        AABB area = ship.getBoundingBox().inflate(0.5, 2.0, 0.5);
        for (Entity e : ship.level().getEntities(ship, area, x -> eligible(ship, x))) {
            Vec3 local = ship.toLocalPoint(e.position());
            double floor = DeckColliders.floorAt(ship.chunk(), local.x, local.z, local.y, STEP_UP);
            if (Double.isNaN(floor)) continue;
            if (local.y - floor > STAND_TOLERANCE || local.y < floor - STAND_TOLERANCE) continue;
            out.put(e, local);
        }
        return out;
    }

    private static boolean eligible(ShipEntity ship, Entity e) {
        return !(e instanceof ShipEntity) && !e.isPassenger() && !e.isSpectator() && !e.noPhysics && !e.isRemoved() && !ship.hasPassenger(e);
    }

    /** Moves the collected entities with the ship. {@code yawDelta} is the ship's yaw change this tick in degrees. */
    public static void carry(ShipEntity ship, Map<Entity, Vec3> riders, float yawDelta) {
        for (Map.Entry<Entity, Vec3> r : riders.entrySet()) {
            Entity e = r.getKey();
            if (e.isRemoved()) continue;
            Vec3 target = ship.toWorldPoint(r.getValue());
            Vec3 delta = target.subtract(e.position());
            if (delta.lengthSqr() > 16) continue; // teleported or fell off: not ours to drag
            float yaw = e.getYRot() + yawDelta;
            if (e instanceof ServerPlayer sp) {
                sp.connection.teleport(target.x, target.y, target.z, yaw, sp.getXRot());
            } else {
                e.move(MoverType.SELF, delta);
                e.setYRot(yaw);
                if (e instanceof LivingEntity le) {
                    le.setYHeadRot(le.getYHeadRot() + yawDelta);
                    le.setYBodyRot(le.yBodyRot + yawDelta);
                }
            }
        }
    }
}
