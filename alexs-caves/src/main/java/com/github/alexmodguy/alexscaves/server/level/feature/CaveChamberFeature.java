package com.github.alexmodguy.alexscaves.server.level.feature;

import com.github.alexmodguy.alexscaves.server.level.feature.config.CaveChamberConfiguration;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * An ellipsoid underground chamber: carves the volume, lines floor/wall/ceiling, optionally floods it, relabels the
 * chunk-section biome so the cave biome's mob spawns apply, then scatters biome decoration on the floor.
 * Only places when the whole chamber sits well below the terrain surface.
 */
public class CaveChamberFeature extends Feature<CaveChamberConfiguration> {
    public CaveChamberFeature(Codec<CaveChamberConfiguration> codec) {
        super(codec);
    }

    private static boolean keep(BlockState s) {
        return s.is(Blocks.BEDROCK) || s.is(Blocks.SPAWNER) || s.is(Blocks.CHEST) || s.is(Blocks.TRIAL_SPAWNER) || s.is(Blocks.VAULT);
    }

    @Override
    public boolean place(FeaturePlaceContext<CaveChamberConfiguration> ctx) {
        CaveChamberConfiguration cfg = ctx.config();
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        int rx = Math.min(cfg.radius(), 12) + rand.nextInt(3) - 1, ry = cfg.height() + rand.nextInt(3) - 1;
        int cx = (ctx.origin().getX() & ~15) + 8, cz = (ctx.origin().getZ() & ~15) + 8;
        int lo = Math.max(cfg.minY(), level.getMinY() + ry + 6);
        if (cfg.maxY() <= lo) return false;
        int cy = lo + rand.nextInt(cfg.maxY() - lo + 1);
        int surface = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, cx, cz);
        for (int dx = -rx; dx <= rx; dx += rx) for (int dz = -rx; dz <= rx; dz += rx) {
            surface = Math.min(surface, level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, cx + dx, cz + dz));
        }
        if (cy + ry + 10 > surface) return false;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        double seed = rand.nextDouble() * 6.28;
        int rim = 2;
        for (int x = -rx - rim; x <= rx + rim; x++) {
            for (int z = -rx - rim; z <= rx + rim; z++) {
                double ang = Math.atan2(z, x);
                double wob = 1.0 + 0.18 * Math.sin(ang * 3 + seed) + 0.1 * Math.sin(ang * 7 + seed * 2);
                for (int y = -ry - rim; y <= ry + rim; y++) {
                    double d = Math.sqrt((x * x) / (double) (rx * rx * wob * wob) + (y * y) / (double) (ry * ry)) ;
                    p.set(cx + x, cy + y, cz + z);
                    if (d > 1.0 + rim * 0.12) continue;
                    BlockState cur = level.getBlockState(p);
                    if (keep(cur)) continue;
                    if (d <= 1.0) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    } else if (!cur.isAir()) {
                        // lining shell, chosen by which face of the ellipsoid this block is on
                        level.setBlock(p, y < 0 ? cfg.floor() : (y > ry / 2 ? cfg.ceiling() : cfg.wall()), 2);
                    }
                }
            }
        }
        // second pass: floor/ceiling/wall lining directly adjacent to air, plus flooding
        BlockPos.MutableBlockPos q = new BlockPos.MutableBlockPos();
        int fluidTop = cy - ry + cfg.fluidDepth();
        java.util.List<BlockPos> floorSpots = new java.util.ArrayList<>();
        for (int x = -rx - 1; x <= rx + 1; x++) for (int z = -rx - 1; z <= rx + 1; z++) for (int y = -ry - 1; y <= ry + 1; y++) {
            p.set(cx + x, cy + y, cz + z);
            BlockState cur = level.getBlockState(p);
            if (cur.isAir()) {
                if (cfg.fluid().isPresent() && p.getY() <= fluidTop) level.setBlock(p, cfg.fluid().get(), 2);
                continue;
            }
            if (keep(cur) || !cur.getFluidState().isEmpty()) continue;
            if (level.getBlockState(q.set(p).move(0, 1, 0)).isAir() || level.getBlockState(q).getBlock() == cfg.fluid().map(BlockState::getBlock).orElse(null)) {
                if (!cur.is(cfg.floor().getBlock())) level.setBlock(p, cfg.floor(), 2);
                if (level.getBlockState(q).isAir() && Math.abs(x) <= 12 && Math.abs(z) <= 12) floorSpots.add(p.immutable());
            } else if (level.getBlockState(q.set(p).move(0, -1, 0)).isAir()) {
                level.setBlock(p, cfg.ceiling(), 2);
            } else if (level.getBlockState(q.set(p).move(1, 0, 0)).isAir() || level.getBlockState(q.set(p).move(-1, 0, 0)).isAir()
                    || level.getBlockState(q.set(p).move(0, 0, 1)).isAir() || level.getBlockState(q.set(p).move(0, 0, -1)).isAir()) {
                level.setBlock(p, cfg.wall(), 2);
            }
        }
        setBiome(level, cfg.biome(), cx, cy, cz, rx + 6, ry + 6);
        if (!cfg.decor().isEmpty()) {
            for (int i = 0; i < cfg.decorAttempts() && !floorSpots.isEmpty(); i++) {
                BlockPos at = floorSpots.get(rand.nextInt(floorSpots.size())).above();
                var f = cfg.decor().get(rand.nextInt(cfg.decor().size())).value();
                try {
                    f.place(level, ctx.chunkGenerator(), rand, at);
                } catch (RuntimeException ignored) {
                    // decoration is best-effort; never fail chunk generation over it
                }
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static void setBiome(WorldGenLevel level, Holder<Biome> biome, int cx, int cy, int cz, int rxz, int ry) {
        for (int qx = (cx - rxz) >> 2; qx <= (cx + rxz) >> 2; qx++) {
            for (int qz = (cz - rxz) >> 2; qz <= (cz + rxz) >> 2; qz++) {
                ChunkAccess chunk = level.getChunk(qx >> 2, qz >> 2);
                for (int qy = (cy - ry) >> 2; qy <= (cy + ry) >> 2; qy++) {
                    int si = chunk.getSectionIndex(qy << 2);
                    if (si < 0 || si >= chunk.getSections().length) continue;
                    var cont = chunk.getSection(si).getBiomes();
                    if (cont instanceof PalettedContainer<?> pc) {
                        ((PalettedContainer<Holder<Biome>>) pc).getAndSet(qx & 3, qy & 3, qz & 3, biome);
                    }
                }
                chunk.markUnsaved();
            }
        }
    }
}
