package net.diaithir.worldvisuals.lambdynamiclights.client;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class DynamicLightEngine {
    private static final Map<Integer, LightSource> SOURCES = new ConcurrentHashMap<>();
    private static final int MAX_DIRTY_PER_UPDATE = 64;
    private static boolean userEnabled = true;
    private static int mode = 2;
    private static int tickCounter;

    private DynamicLightEngine() {}

    public static boolean isActive() {
        return userEnabled && mode > 0 && DynamicLightsConfig.ENABLED.get();
    }

    public static boolean toggle() {
        userEnabled = !userEnabled;
        if (!userEnabled) clear();
        return userEnabled;
    }

    public static int cycleMode() {
        mode = (mode + 1) % 3;
        if (mode == 0) clear();
        return mode;
    }

    public static int mode() {
        return mode;
    }

    public static int sourceCount() {
        return SOURCES.size();
    }

    public static void clear() {
        Set<BlockPos> old = new HashSet<>();
        SOURCES.values().forEach(s -> old.add(s.pos()));
        SOURCES.clear();
        markDirty(old);
    }

    public static void remove(int entityId) {
        LightSource s = SOURCES.remove(entityId);
        if (s != null) markDirty(Set.of(s.pos()));
    }

    public static int getLevelAt(BlockPos pos) {
        if (SOURCES.isEmpty()) return 0;
        int best = 0;
        for (LightSource s : SOURCES.values()) {
            int dist = Math.abs(s.pos().getX() - pos.getX()) + Math.abs(s.pos().getY() - pos.getY()) + Math.abs(s.pos().getZ() - pos.getZ());
            int v = s.level() - dist;
            if (v > best) best = v;
        }
        return best;
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (!isActive()) {
            if (!SOURCES.isEmpty()) clear();
            return;
        }
        if (++tickCounter < DynamicLightsConfig.UPDATE_RATE_TICKS.get()) return;
        tickCounter = 0;

        double maxDistSq = Math.pow(DynamicLightsConfig.MAX_DISTANCE.get(), 2);
        Set<Integer> seen = new HashSet<>();
        Set<BlockPos> dirty = new HashSet<>();
        int cap = mode == 1 ? 16 : Integer.MAX_VALUE;
        int count = 0;

        for (Entity e : mc.level.entitiesForRendering()) {
            if (e.isRemoved()) continue;
            if (DynamicLightsConfig.SELF_ONLY.get() && e != mc.player && !(e instanceof net.minecraft.world.entity.item.ItemEntity) && !(e instanceof Player p && p == mc.player)) {
                if (!DynamicLightsConfig.ENTITIES.get() && !DynamicLightsConfig.DROPPED_ITEMS.get()) continue;
            }
            if (e.distanceToSqr(mc.player) > maxDistSq) continue;
            int level = LuminanceResolver.resolve(e);
            if (level <= 0) continue;
            if (++count > cap) break;
            BlockPos pos = e.blockPosition();
            if (e.getBbHeight() > 1.0F) pos = BlockPos.containing(e.getX(), e.getY() + e.getBbHeight() * 0.5, e.getZ());
            seen.add(e.getId());
            LightSource prev = SOURCES.get(e.getId());
            if (prev == null || !prev.pos().equals(pos) || prev.level() != level) {
                if (prev != null) dirty.add(prev.pos());
                dirty.add(pos);
                SOURCES.put(e.getId(), new LightSource(e.getId(), pos.immutable(), level));
            }
        }

        for (Integer id : SOURCES.keySet()) {
            if (!seen.contains(id)) {
                LightSource s = SOURCES.remove(id);
                if (s != null) dirty.add(s.pos());
            }
        }
        markDirty(dirty);
    }

    private static void markDirty(Set<BlockPos> positions) {
        if (positions.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Set<Long> sections = new HashSet<>();
        int n = 0;
        for (BlockPos p : positions) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int sx = (p.getX() >> 4) + dx;
                        int sy = (p.getY() >> 4) + dy;
                        int sz = (p.getZ() >> 4) + dz;
                        if (sections.add(net.minecraft.core.SectionPos.asLong(sx, sy, sz))) {
                            if (++n > MAX_DIRTY_PER_UPDATE) return;
                            mc.levelRenderer.setSectionDirty(sx, sy, sz);
                        }
                    }
                }
            }
        }
    }
}
