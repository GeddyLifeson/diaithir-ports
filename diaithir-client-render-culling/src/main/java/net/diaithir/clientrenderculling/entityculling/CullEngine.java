package net.diaithir.clientrenderculling.entityculling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class CullEngine {
    record Result(boolean hidden, long time) {}
    record Target(Object key, AABB box, boolean entity) {}

    static final Map<Object, Result> CACHE = new ConcurrentHashMap<>();
    static volatile boolean enabled = true;
    static volatile long disabledUntil = 0;
    static final AtomicInteger entTotal = new AtomicInteger(), entCulled = new AtomicInteger();
    static final AtomicInteger beTotal = new AtomicInteger(), beCulled = new AtomicInteger();
    private static ExecutorService executor;
    private static final AtomicInteger inFlight = new AtomicInteger();
    private static long lastSubmit = 0;

    private CullEngine() {}

    private static synchronized ExecutorService pool() {
        if (executor == null) {
            int n = CullConfig.THREADS.get();
            executor = Executors.newFixedThreadPool(n, r -> {
                Thread t = new Thread(r, "diaithir-culling");
                t.setDaemon(true);
                return t;
            });
        }
        return executor;
    }

    static synchronized void resize() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        inFlight.set(0);
    }

    static synchronized void shutdown() {
        resize();
        CACHE.clear();
    }

    static boolean active() {
        return enabled && CullConfig.ENABLED.get() && System.currentTimeMillis() >= disabledUntil;
    }

    static boolean isHidden(Object key, long maxAgeMs) {
        Result r = CACHE.get(key);
        if (r == null) return false;
        return r.hidden() && System.currentTimeMillis() - r.time() < maxAgeMs;
    }

    static boolean isHidden(Object key) {
        return isHidden(key, Math.max(500, CullConfig.RECHECK_MS.get() * 5L));
    }

    static void tick(Minecraft mc) {
        if (!active() || mc.level == null || mc.player == null) return;
        long now = System.currentTimeMillis();
        if (now - lastSubmit < CullConfig.RECHECK_MS.get() || inFlight.get() > 0) return;
        lastSubmit = now;

        Camera cam = mc.gameRenderer.getMainCamera();
        Vec3 origin = cam.position();
        double maxD = CullConfig.MAX_DIST.get();
        double minD = CullConfig.ALWAYS_DIST.get();
        double maxSq = maxD * maxD, minSq = minD * minD;
        int limit = CullConfig.MAX_TARGETS.get();
        List<Target> targets = new ArrayList<>();
        int et = 0, bt = 0;

        if (CullConfig.CULL_ENTITIES.get()) {
            for (Entity e : mc.level.entitiesForRendering()) {
                if (targets.size() >= limit) break;
                et++;
                double d = e.distanceToSqr(origin);
                if (d < minSq || d > maxSq || neverCull(mc, e)) {
                    CACHE.remove(e);
                    continue;
                }
                targets.add(new Target(e, e.getBoundingBox(), true));
            }
        }
        if (CullConfig.CULL_BLOCK_ENTITIES.get()) {
            int cr = (int) Math.min(16, Math.ceil(maxD / 16.0));
            ChunkPos cp = mc.player.chunkPosition();
            for (int cx = cp.x() - cr; cx <= cp.x() + cr && targets.size() < limit; cx++) {
                for (int cz = cp.z() - cr; cz <= cp.z() + cr && targets.size() < limit; cz++) {
                    ChunkAccess ch = mc.level.getChunk(cx, cz, ChunkStatus.FULL, false);
                    if (ch == null) continue;
                    for (BlockPos p : new ArrayList<>(ch.getBlockEntitiesPos())) {
                        if (targets.size() >= limit) break;
                        bt++;
                        double d = origin.distanceToSqr(p.getX() + .5, p.getY() + .5, p.getZ() + .5);
                        if (d < minSq || d > maxSq) {
                            CACHE.remove(p);
                            continue;
                        }
                        targets.add(new Target(p.immutable(), new AABB(p), false));
                    }
                }
            }
        }
        entTotal.set(et);
        beTotal.set(bt);
        if (targets.isEmpty()) return;

        Level level = mc.level;
        int samples = CullConfig.SAMPLES.get();
        inFlight.incrementAndGet();
        try {
            pool().execute(() -> {
                int ec = 0, bc = 0;
                try {
                    long t = System.currentTimeMillis();
                    for (Target tg : targets) {
                        boolean hidden = occluded(level, origin, tg.box(), samples);
                        CACHE.put(tg.key(), new Result(hidden, t));
                        if (hidden) {
                            if (tg.entity()) ec++;
                            else bc++;
                        }
                    }
                    entCulled.set(ec);
                    beCulled.set(bc);
                } catch (Throwable ex) {
                    CACHE.clear();
                    disabledUntil = System.currentTimeMillis() + 10_000;
                } finally {
                    inFlight.decrementAndGet();
                }
            });
        } catch (Throwable ex) {
            inFlight.decrementAndGet();
        }
    }

    private static boolean neverCull(Minecraft mc, Entity e) {
        if (e == mc.player || e == mc.getCameraEntity() || e == mc.crosshairPickEntity) return true;
        if (e instanceof net.minecraft.world.entity.player.Player && !CullConfig.CULL_PLAYERS.get()) return true;
        if (e.hasCustomName() || e.isCurrentlyGlowing() && !CullConfig.CULL_INVIS_GLOW.get()) return true;
        if (e.isInvisible() && !CullConfig.CULL_INVIS_GLOW.get()) return true;
        Entity veh = mc.player.getVehicle();
        if (veh != null && (e == veh || e.getRootVehicle() == veh.getRootVehicle())) return true;
        if (e.isPassenger() && e.getVehicle() == mc.player) return true;
        if (e instanceof net.minecraft.world.entity.Leashable l && l.isLeashed()) return true;
        if (e.getType().builtInRegistryHolder().is(CullEvents.NEVER_CULL)) return true;
        var id = net.minecraft.world.entity.EntityType.getKey(e.getType());
        return CullConfig.entityWhitelist.contains(id.toString());
    }

    private static boolean occluded(Level level, Vec3 o, AABB b, int samples) {
        for (Vec3 p : points(b, samples)) {
            if (!blocked(level, o, p)) return false;
        }
        return true;
    }

    private static Vec3[] points(AABB b, int n) {
        Vec3 c = b.getCenter();
        Vec3[] all = {
            c,
            new Vec3(b.minX, b.minY, b.minZ), new Vec3(b.maxX, b.maxY, b.maxZ),
            new Vec3(b.minX, b.maxY, b.minZ), new Vec3(b.maxX, b.minY, b.maxZ),
            new Vec3(b.minX, b.minY, b.maxZ), new Vec3(b.maxX, b.maxY, b.minZ),
            new Vec3(b.minX, b.maxY, b.maxZ), new Vec3(b.maxX, b.minY, b.minZ)
        };
        Vec3[] out = new Vec3[Math.min(n, all.length)];
        System.arraycopy(all, 0, out, 0, out.length);
        return out;
    }

    /** Voxel DDA from o to t; true if an opaque full cube lies strictly between. */
    private static boolean blocked(Level level, Vec3 o, Vec3 t) {
        double dx = t.x - o.x, dy = t.y - o.y, dz = t.z - o.z;
        int x = (int) Math.floor(o.x), y = (int) Math.floor(o.y), z = (int) Math.floor(o.z);
        int ex = (int) Math.floor(t.x), ey = (int) Math.floor(t.y), ez = (int) Math.floor(t.z);
        int sx = Integer.signum((int) Math.signum(dx)), sy = Integer.signum((int) Math.signum(dy)), sz = Integer.signum((int) Math.signum(dz));
        double tdx = dx == 0 ? Double.MAX_VALUE : Math.abs(1 / dx);
        double tdy = dy == 0 ? Double.MAX_VALUE : Math.abs(1 / dy);
        double tdz = dz == 0 ? Double.MAX_VALUE : Math.abs(1 / dz);
        double mx = dx == 0 ? Double.MAX_VALUE : (sx > 0 ? (x + 1 - o.x) : (o.x - x)) * tdx;
        double my = dy == 0 ? Double.MAX_VALUE : (sy > 0 ? (y + 1 - o.y) : (o.y - y)) * tdy;
        double mz = dz == 0 ? Double.MAX_VALUE : (sz > 0 ? (z + 1 - o.z) : (o.z - z)) * tdz;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int max = Math.abs(ex - x) + Math.abs(ey - y) + Math.abs(ez - z) + 1;
        for (int i = 0; i < max; i++) {
            if (x == ex && y == ey && z == ez) return false;
            if (mx < my && mx < mz) { x += sx; mx += tdx; }
            else if (my < mz) { y += sy; my += tdy; }
            else { z += sz; mz += tdz; }
            if (x == ex && y == ey && z == ez) return false;
            pos.set(x, y, z);
            if (level.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, false) == null) continue;
            BlockState s = level.getBlockState(pos);
            if (s.isSolidRender()) return true;
        }
        return false;
    }
}
