package net.diaithir.gellings;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Gel rain: while it rains in the overworld, gellings drop in around each player, a few at a time, never more than {@link #CAP} nearby. */
@EventBusSubscriber(modid = GellingMod.ID)
public final class GelRain {
    static final int CAP = 8;
    static final int PERIOD = 80;

    private GelRain() {}

    @SubscribeEvent
    static void tick(LevelTickEvent.Post e) {
        if (!(e.getLevel() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) return;
        if (!level.isRaining() || level.getGameTime() % PERIOD != 0) return;
        for (ServerPlayer p : level.players()) {
            if (!p.isSpectator() && level.getRandom().nextInt(3) == 0) drop(level, p);
        }
    }

    /** One swarm around a player; returns how many gellings fell. */
    static int drop(ServerLevel level, Player p) {
        if (level.getEntitiesOfClass(Gelling.class, p.getBoundingBox().inflate(48)).size() >= CAP) return 0;
        double a = level.getRandom().nextDouble() * Math.PI * 2, r = 16 + level.getRandom().nextInt(16);
        BlockPos xz = BlockPos.containing(p.getX() + Math.cos(a) * r, 0, p.getZ() + Math.sin(a) * r);
        BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, xz);
        if (!level.canSeeSky(top) || !level.getFluidState(top.below()).isEmpty()) return 0;
        Species s = pick(level, top);
        int n = 1 + level.getRandom().nextInt(2), done = 0;
        for (int i = 0; i < n; i++) if (GellingMod.TYPES.get(s.id()).get().spawn(level, top.above(12 + i * 2), EntitySpawnReason.EVENT) != null) done++;
        return done;
    }

    static Species pick(ServerLevel level, BlockPos pos) {
        var biome = level.getBiome(pos);
        if (biome.is(BiomeTags.IS_JUNGLE)) return Species.byId("moss");
        if (biome.is(BiomeTags.IS_TAIGA) || biome.value().coldEnoughToSnow(pos, level.getSeaLevel())) return Species.byId("frost");
        if (biome.is(BiomeTags.IS_BADLANDS) || biome.is(net.minecraft.world.level.biome.Biomes.DESERT)) return Species.byId("dune");
        if (level.getRandom().nextInt(60) == 0) return Species.byId("blush");
        if (level.isDarkOutside() && level.getRandom().nextInt(3) == 0) return Species.byId("ink");
        Species[] common = {Species.byId("meadow"), Species.byId("tide"), Species.byId("rouge"), Species.byId("violet"), Species.byId("amber")};
        return common[level.getRandom().nextInt(common.length)];
    }
}
