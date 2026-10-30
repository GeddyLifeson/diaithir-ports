package com.github.alexmodguy.alexscaves.server.level.structure;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

import java.util.stream.Stream;

/**
 * Stand-in for the upstream 3D cave biome source. Upstream's cave structures size themselves by sampling the biome
 * source for their cave biome. Here the biome is "virtual": it exists inside a disc around the start chunk of that
 * biome's parent bowl structure (cake cave, dino bowl ...), whose placement is recomputed from the world seed.
 * Everywhere else it falls through to the real biome source.
 */
public class CaveMaskBiomeSource extends BiomeSource {
    public static final MapCodec<CaveMaskBiomeSource> CODEC = MapCodec.unit(() -> {
        throw new UnsupportedOperationException("not serialisable");
    });

    /** spacing, separation, salt of a parent bowl set (must match its structure_set json) and the disc radius in blocks. */
    public record Parent(int spacing, int separation, int salt, int radius, int centerY, int footprint, boolean oceanOnly) {
    }

    private final BiomeSource base;
    private final Holder<Biome> cave;
    private final RandomSpreadStructurePlacement placement;
    private final Parent parent;
    private final long seed;
    private final Structure.GenerationContext ctx;
    private final java.util.Map<Long, Boolean> viable = new java.util.HashMap<>();

    public CaveMaskBiomeSource(BiomeSource base, Holder<Biome> cave, Parent parent, long seed, Structure.GenerationContext ctx) {
        this.ctx = ctx;
        this.base = base;
        this.cave = cave;
        this.parent = parent;
        this.seed = seed;
        this.placement = new RandomSpreadStructurePlacement(parent.spacing(), parent.separation(), RandomSpreadType.LINEAR, parent.salt());
    }

    public static BiomeSource of(Structure.GenerationContext ctx, ResourceKey<Biome> key) {
        Parent p = ACCaveParents.forBiome(key);
        if (p == null) return ctx.biomeSource();
        Holder<Biome> holder = ctx.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME).getOrThrow(key);
        return new CaveMaskBiomeSource(ctx.biomeSource(), holder, p, ctx.seed(), ctx);
    }

    public static boolean insideParent(Structure.GenerationContext ctx, ResourceKey<Biome> key) {
        return of(ctx, key) instanceof CaveMaskBiomeSource m && m.inside(ctx.chunkPos().getMiddleBlockX(), ctx.chunkPos().getMiddleBlockZ());
    }

    public boolean inside(int blockX, int blockZ) {
        int cx = blockX >> 4, cz = blockZ >> 4;
        long r2 = (long) parent.radius() * parent.radius();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPos c = placement.getPotentialStructureChunk(seed, cx + dx * parent.spacing(), cz + dz * parent.spacing());
                long ox = blockX - c.getMinBlockX(), oz = blockZ - c.getMinBlockZ();
                if (ox * ox + oz * oz <= r2 && parentViable(c)) return true;
            }
        }
        return false;
    }

    private int surface(int x, int z) {
        return ctx.chunkGenerator().getBaseHeight(x, z, net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG, ctx.heightAccessor(), ctx.randomState());
    }

    /** Mirrors the terrain gate the parent bowl structure applies, so children never appear without their parent. */
    private boolean parentViable(ChunkPos c) {
        return viable.computeIfAbsent(((long) c.x() << 32) ^ (c.z() & 0xffffffffL), k -> {
            int x = c.getMinBlockX(), z = c.getMinBlockZ();
            if (parent.oceanOnly()) {
                return surface(x, z) < ctx.chunkGenerator().getSeaLevel() - 6;
            }
            int low = surface(x, z);
            int w = parent.footprint();
            for (int dx = -1; dx <= 1; dx += 2) for (int dz = -1; dz <= 1; dz += 2) {
                low = Math.min(low, surface(x + dx * w / 2, z + dz * w / 2));
            }
            return low - 14 - parent.centerY() >= 40;
        });
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(Stream.of(cave), base.possibleBiomes().stream());
    }

    @Override
    public Holder<Biome> getNoiseBiome(int qx, int qy, int qz, Climate.Sampler sampler) {
        if (inside(qx << 2, qz << 2)) return cave;
        return base.getNoiseBiome(qx, qy, qz, sampler);
    }
}
