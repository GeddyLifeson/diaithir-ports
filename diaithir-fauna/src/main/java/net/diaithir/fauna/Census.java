package net.diaithir.fauna;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/** Debug summary of the loaded fauna: counts per plan, habitat mismatches and animals that have not moved since the last call. */
final class Census {
    private static final Map<UUID, Vec3> LAST = new HashMap<>();

    private Census() {}

    static String run(ServerLevel level) {
        Map<String, Integer> byPlan = new TreeMap<>();
        Map<String, Integer> bySpecies = new TreeMap<>();
        int total = 0, wetLand = 0, dryWater = 0, flyGround = 0, still = 0, revived = 0, vanilla = 0, far = 0, noGoals = 0, near = 0;
        Map<UUID, Vec3> now = new HashMap<>();
        for (Entity e : level.getAllEntities()) {
            if (e instanceof net.minecraft.world.entity.animal.Animal || e instanceof net.minecraft.world.entity.animal.fish.AbstractFish) vanilla++;
            if (!(e instanceof FaunaMob m)) continue;
            if (!level.isPositionEntityTicking(m.blockPosition())) {
                far++;
                continue;
            }
            total++;
            byPlan.merge(m.plan.name, 1, Integer::sum);
            Species s = m.species();
            if (s != null) bySpecies.merge(s.id, 1, Integer::sum);
            if (m.entityTags().contains("Revived")) revived++;
            if (m.plan.water() && !m.isInWater()) dryWater++;
            if (!m.plan.water() && !m.plan.fly() && m.isInWater()) wetLand++;
            if (m.plan.fly() && m.onGround()) flyGround++;
            if (m.goalSelector.getAvailableGoals().isEmpty()) noGoals++;
            Vec3 p = m.position();
            now.put(m.getUUID(), p);
            Vec3 old = LAST.get(m.getUUID());
            net.minecraft.world.entity.player.Player pl = level.getNearestPlayer(m, 24);
            if (pl != null) near++;
            if (pl != null && old != null && old.distanceToSqr(p) < 0.01) still++;
        }
        LAST.clear();
        LAST.putAll(now);
        Runtime rt = Runtime.getRuntime();
        double mspt = level.getServer().getAverageTickTimeNanos() / 1e6;
        return "ticking=" + total + " notTicking=" + far + " vanillaAnimals=" + vanilla + " revived=" + revived + " plans=" + byPlan.size() + " species=" + bySpecies.size() + " landInWater=" + wetLand + " waterOutOfWater=" + dryWater
                + " noGoals=" + noGoals + " flyersOnGround=" + flyGround + " within24=" + near + " stillSinceLast=" + still + " mspt=" + String.format("%.1f", mspt)
                + " heapMB=" + (rt.totalMemory() - rt.freeMemory()) / 1048576 + " tries(ok,wrongBlock,noSpecies)=" + java.util.Arrays.toString(FaunaMod.TRIES) + " plan=" + byPlan + " species=" + bySpecies;
    }
}
