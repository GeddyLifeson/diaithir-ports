package com.diaithir.overworld_biomes.nomorecraters;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Depth solver, structure query and result type for buried meteor placement. */
public final class MeteorPlacement {
    private static final Logger LOG = LoggerFactory.getLogger("nomorecraters");
    private static final TagKey<Biome> FULL_BURIAL = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(NoMoreCraters.MOD_ID, "full_burial_meteors"));
    private static final TagKey<Structure> PROTECTED = TagKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(NoMoreCraters.MOD_ID, "meteor_protected"));
    private static final int MIN_Y_MARGIN = 5;
    private static boolean errorLogged;

    private MeteorPlacement() {}

    public record Result(boolean cancelled, BlockPos core, boolean skipCrater, boolean hint) {
        public static Result cancel() { return new Result(true, BlockPos.ZERO, true, false); }
    }

    /** Compute the adjusted placement; falls back to null (vanilla AE2 behaviour) on any error. */
    public static Result solve(WorldGenLevel level, BlockPos origin, int radius) {
        if (!MeteorConfig.ENABLED.get() || !Ae2Adapter.isPresent()) {
            return null;
        }
        try {
            return doSolve(level, origin, radius);
        } catch (RuntimeException e) {
            if (!errorLogged) {
                errorLogged = true;
                LOG.error("Meteor suppression failed, falling back to default placement", e);
            }
            return null;
        }
    }

    private static Result doSolve(WorldGenLevel level, BlockPos origin, int radius) {
        int minY = level.getMinY() + MIN_Y_MARGIN;
        int surface = surfaceMin(level, origin, radius);
        int coreY = Math.max(minY, surface - MeteorConfig.BURIAL_DEPTH.get() - radius);

        MeteorConfig.Protection mode = MeteorConfig.STRUCTURE_PROTECTION.get();
        if (mode != MeteorConfig.Protection.OFF) {
            Optional<Integer> floor = protectedFloor(level, origin, radius);
            if (floor.isPresent()) {
                if (mode == MeteorConfig.Protection.CANCEL) {
                    return Result.cancel();
                }
                coreY = Math.max(minY, Math.min(coreY, floor.get() - radius - 1));
            }
        }

        BlockPos core = new BlockPos(origin.getX(), coreY, origin.getZ());
        Holder<Biome> biome = level.getBiome(origin);
        boolean fullBurial = biome.is(FULL_BURIAL);
        boolean hint = MeteorConfig.SURFACE_HINT.get() && !fullBurial;
        if (MeteorConfig.LOG_SUPPRESSIONS.get()) {
            LOG.info("Buried meteor at {} (surface {}, radius {})", core, surface, radius);
        }
        return new Result(false, core, MeteorConfig.REMOVE_CRATER_BLOCKS.get(), hint);
    }

    private static int surfaceMin(WorldGenLevel level, BlockPos c, int r) {
        int h = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, c.getX(), c.getZ());
        h = Math.min(h, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, c.getX() + r, c.getZ()));
        h = Math.min(h, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, c.getX() - r, c.getZ()));
        h = Math.min(h, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, c.getX(), c.getZ() + r));
        return Math.min(h, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, c.getX(), c.getZ() - r));
    }

    /** Lowest Y of any protected structure overlapping the footprint, if any. */
    private static Optional<Integer> protectedFloor(WorldGenLevel level, BlockPos c, int r) {
        Map<ChunkPos, Boolean> seen = new HashMap<>();
        BoundingBox foot = new BoundingBox(c.getX() - r, level.getMinY(), c.getZ() - r, c.getX() + r, level.getMaxY(), c.getZ() + r);
        int low = Integer.MAX_VALUE;
        var sm = level.getLevel().structureManager();
        for (int cx = (c.getX() - r) >> 4; cx <= (c.getX() + r) >> 4; cx++) {
            for (int cz = (c.getZ() - r) >> 4; cz <= (c.getZ() + r) >> 4; cz++) {
                ChunkPos cp = new ChunkPos(cx, cz);
                if (seen.putIfAbsent(cp, Boolean.TRUE) != null) {
                    continue;
                }
                List<StructureStart> starts = sm.startsForStructure(cp, s -> isProtected(level, s));
                for (StructureStart start : starts) {
                    BoundingBox bb = start.getBoundingBox();
                    if (bb.intersects(foot)) {
                        low = Math.min(low, bb.minY());
                    }
                }
            }
        }
        return low == Integer.MAX_VALUE ? Optional.empty() : Optional.of(low);
    }

    private static boolean isProtected(WorldGenLevel level, Structure s) {
        var reg = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        ResourceKey<Structure> key = reg.getResourceKey(s).orElse(null);
        if (key == null) {
            return false;
        }
        Holder<Structure> h = reg.getOrThrow(key);
        if (h.is(PROTECTED)) {
            return true;
        }
        for (String entry : MeteorConfig.PROTECTED_STRUCTURE_TAGS.get()) {
            if (entry.startsWith("#")) {
                Identifier id = Identifier.tryParse(entry.substring(1));
                if (id != null && h.is(TagKey.create(Registries.STRUCTURE, id))) {
                    return true;
                }
            } else if (key.identifier().toString().equals(entry)) {
                return true;
            }
        }
        return false;
    }
}
