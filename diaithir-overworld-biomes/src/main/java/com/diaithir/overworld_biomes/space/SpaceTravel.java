package com.diaithir.overworld_biomes.space;

import com.diaithir.overworld_biomes.planets.PlanetGate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

/**
 * Where a launch can go. Moon and Earth orbit are Stellaris dimensions; any other target is a Panscriptum planet id and goes
 * through PlanetGate, so the Keelwright ships lane and the rocket share one entry point.
 */
public final class SpaceTravel {
    public static final ResourceKey<Level> MOON = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("stellaris", "moon"));
    public static final ResourceKey<Level> ORBIT = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("stellaris", "earth_orbit"));

    private SpaceTravel() {}

    /** Airless dimensions: a player needs a sealed suit or an oxygen distributor there. Planets keep their own air. */
    public static boolean isVacuum(Level l) {
        return l.dimension().equals(MOON) || l.dimension().equals(ORBIT);
    }

    /** target: "moon", "earth_orbit", "terra" (the Overworld), or a planet id. Returns false if the target does not exist. */
    public static boolean launch(ServerPlayer p, String target) {
        var server = p.level().getServer();
        switch (target) {
            case "terra", "overworld" -> {
                ServerLevel ow = server.overworld();
                BlockPos sp = p.getRespawnConfig() != null && p.getRespawnConfig().respawnData().dimension().equals(Level.OVERWORLD)
                        ? p.getRespawnConfig().respawnData().pos() : ow.getRespawnData().pos();
                ow.getChunk(sp.getX() >> 4, sp.getZ() >> 4);
                int y = ow.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sp.getX(), sp.getZ()) + 1;
                p.teleport(new TeleportTransition(ow, new Vec3(sp.getX() + 0.5, y, sp.getZ() + 0.5), Vec3.ZERO, p.getYRot(), 0f, TeleportTransition.DO_NOTHING));
                return true;
            }
            case "moon" -> {
                ServerLevel l = server.getLevel(MOON);
                return l != null && land(p, l, p.getBlockX(), p.getBlockZ());
            }
            case "earth_orbit" -> {
                ServerLevel l = server.getLevel(ORBIT);
                if (l == null) return false;
                int x = p.getBlockX(), z = p.getBlockZ();
                l.getChunk(x >> 4, z >> 4);
                for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) l.setBlock(new BlockPos(x + dx, 100, z + dz), net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("stellaris", "meteorite")).defaultBlockState(), 3);
                p.teleport(new TeleportTransition(l, new Vec3(x + 0.5, 101, z + 0.5), Vec3.ZERO, p.getYRot(), 0f, TeleportTransition.DO_NOTHING));
                return true;
            }
            default -> {
                return PlanetGate.enter(p, target);
            }
        }
    }

    private static boolean land(ServerPlayer p, ServerLevel l, int x, int z) {
        l.getChunk(x >> 4, z >> 4);
        int y = l.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) + 1;
        if (l.getBlockState(new BlockPos(x, y - 1, z)).is(Blocks.AIR)) y = l.getMinY() + 80;
        p.teleport(new TeleportTransition(l, new Vec3(x + 0.5, y, z + 0.5), Vec3.ZERO, p.getYRot(), 0f, TeleportTransition.DO_NOTHING));
        return true;
    }
}
