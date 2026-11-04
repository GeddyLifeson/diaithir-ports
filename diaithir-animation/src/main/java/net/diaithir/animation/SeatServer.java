package net.diaithir.animation;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Tank crew seats. Immersive Vehicles seats the rider on a mts:builder_seat and every vehicle is one mts:builder_existing entity whose saved data names it
 * (packID diaithirtanks, systemName tank_mk1). Seat 1 (local 0, 0.95, 1.9) is the driver, seat 2 (0.6, 1.2, -0.4) is the gunner; the rider is matched to the
 * nearer seat in the vehicle's own frame. Only visual clips are sent (seat.driver / seat.turret / seat.none); everything is optional and a no-op without that mod.
 */
public final class SeatServer {
    private static final Map<UUID, String> LAST = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> TANK = new ConcurrentHashMap<>();
    private static final Vec3 DRIVER = new Vec3(0, 0.95, 1.9), GUNNER = new Vec3(0.6, 1.2, -0.4);

    @SubscribeEvent
    public void tick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || p.tickCount % 10 != 0) return;
        String now = "seat.none";
        Entity seat = p.getVehicle();
        if (seat != null && BuiltInRegistries.ENTITY_TYPE.getKey(seat.getType()).toString().equals("mts:builder_seat")) {
            Entity tank = nearestTank(p, seat);
            if (tank != null) now = kind(p, tank);
        }
        String last = LAST.getOrDefault(p.getUUID(), "seat.none");
        if (!now.equals(last)) { LAST.put(p.getUUID(), now); Anim.play(p, now); }
    }

    @SubscribeEvent public void out(PlayerEvent.PlayerLoggedOutEvent e) { LAST.remove(e.getEntity().getUUID()); }

    private static Entity nearestTank(ServerPlayer p, Entity seat) {
        Entity best = null; double bd = 64;
        for (Entity v : p.level().getEntities(seat, seat.getBoundingBox().inflate(8))) {
            if (!BuiltInRegistries.ENTITY_TYPE.getKey(v.getType()).toString().equals("mts:builder_existing") || !isTank(v)) continue;
            double d = v.distanceToSqr(seat);
            if (d < bd) { bd = d; best = v; }
        }
        return best;
    }

    private static boolean isTank(Entity v) {
        return TANK.computeIfAbsent(v.getUUID(), k -> {
            var out = net.minecraft.world.level.storage.TagValueOutput.createWithoutContext(net.minecraft.util.ProblemReporter.DISCARDING);
            v.saveWithoutId(out);
            CompoundTag t = out.buildResult();
            return t.toString().contains("diaithirtanks");
        });
    }

    private static String kind(ServerPlayer p, Entity tank) {
        Vec3 d = p.position().subtract(tank.position());
        double yaw = Math.toRadians(tank.getYRot()), c = Math.cos(yaw), s = Math.sin(yaw);
        Vec3 local = new Vec3(d.x * c + d.z * s, d.y, -d.x * s + d.z * c); // tank frame: +z forward
        return local.distanceToSqr(DRIVER) <= local.distanceToSqr(GUNNER) ? "seat.driver" : "seat.turret";
    }
}
