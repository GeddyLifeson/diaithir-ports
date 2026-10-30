package net.diaithir.steamrails;

import com.mojang.logging.LogUtils;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.contraptions.glue.SuperGlueEntity;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackShape;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A real train on real Create track. Two yards side by side: a station on a straight track and, east of it, a carriage of a driver bogey, a pony
 * bogey, a deck, a boiler with two cylinders and train controls. The first boiler is hot, the second cold. Both trains are assembled by
 * Create's station and driven at full throttle. The hot one must move, the cold one must stay where it is.
 */
final class TrainTest {
    private TrainTest() {}

    private static final org.slf4j.Logger LOG = LogUtils.getLogger();

    private static ServerLevel level;
    private static CommandSourceStack source;
    private static int tick = -1;
    private static final List<String> fails = new ArrayList<>();
    private static final List<ChunkPos> forced = new ArrayList<>();
    private static final List<BlockPos> stations = new ArrayList<>();
    private static final List<BlockPos> boilers = new ArrayList<>();
    private static final Train[] trains = new Train[2];
    private static final Vec3[] startAt = new Vec3[2];
    private static final double[] moved = new double[2];
    private static BlockPos base;
    /** Leaves the yard and the trains standing, so a client can be pointed at them. */
    private static boolean keep;
    private static final List<SuperGlueEntity> glues = new ArrayList<>();

    static String start(CommandSourceStack src, boolean keepYard) {
        if (tick >= 0) return "already running";
        keep = keepYard;
        level = src.getServer().getLevel(Level.OVERWORLD);
        if (level == null) return "no overworld";
        source = src;
        fails.clear();
        stations.clear();
        boilers.clear();
        trains[0] = trains[1] = null;
        int sx = level.getRespawnData().pos().getX();
        int sz = level.getRespawnData().pos().getZ();
        // the kept yard stands on the ground next to spawn, where a client can look at it
        base = keep ? new BlockPos(sx + 8, level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, sx + 8, sz + 8) + 1, sz + 8) : new BlockPos(sx + 40, 200, sz + 40);
        for (int cx = (base.getX() - 40) >> 4; cx <= (base.getX() + 20) >> 4; cx++) {
            for (int cz = (base.getZ() - 4) >> 4; cz <= (base.getZ() + 20) >> 4; cz++) {
                level.getChunk(cx, cz);
                level.setChunkForced(cx, cz, true);
                forced.add(new ChunkPos(cx, cz));
            }
        }
        clear();
        tick = 0;
        return null;
    }

    private static void check(boolean ok, String what) {
        if (!ok) fails.add(what);
    }

    static void tick() {
        if (tick < 0) return;
        try {
            step();
        } catch (RuntimeException | LinkageError e) {
            LOG.error("steamrails train test crashed", e);
            fails.add("exception " + e);
            finish();
            return;
        }
        if (tick >= 0) tick++;
    }

    private static void yard(int n, boolean hot) {
        int ty = base.getY();
        int x0 = base.getX();
        int z0 = base.getZ() + n * 12;
        BlockState track = AllBlocks.TRACK.defaultBlockState().setValue(TrackBlock.SHAPE, TrackShape.XO);
        for (int x = x0 - 36; x <= x0 + 12; x++) {
            level.setBlock(new BlockPos(x, ty, z0), track, 3);
        }
        BlockPos st = new BlockPos(x0, ty, z0 + 1);
        level.setBlock(st, AllBlocks.TRACK_STATION.defaultBlockState(), 3);
        stations.add(st);
        CompoundTag tag = new CompoundTag();
        var ops = BoilerMovement.ops(level);
        tag.store("TargetTrack", BlockPos.CODEC, ops, new BlockPos(0, 0, -1));
        tag.putBoolean("TargetDirection", true);
        tag.putBoolean("Ortho", true);
        tag.store("Id", net.minecraft.core.UUIDUtil.CODEC, ops, UUID.randomUUID());
        level.getBlockEntity(st).loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
        // the carriage, floor row ty+1: driver bogey at x0+1, deck, pony bogey at x0+5
        BlockPos b1 = new BlockPos(x0 + 1, ty + 1, z0);
        level.setBlock(b1, SteamBlocks.DRIVER_BOGEY.get().defaultBlockState(), 3);
        for (int i = 2; i <= 4; i++) level.setBlock(new BlockPos(x0 + i, ty + 1, z0), Blocks.IRON_BLOCK.defaultBlockState(), 3);
        level.setBlock(new BlockPos(x0 + 5, ty + 1, z0), SteamBlocks.PONY_BOGEY.get().defaultBlockState(), 3);
        // boiler on the deck, row ty+2: firebox, two shells, stack, whistle, two cylinders beside, controls behind
        BlockPos f = new BlockPos(x0 + 1, ty + 2, z0);
        level.setBlock(f, SteamBlocks.BOILER.get().defaultBlockState(), 3);
        level.setBlock(f.east(), SteamBlocks.STEEL_SHELL.get().defaultBlockState(), 3);
        level.setBlock(f.east(2), SteamBlocks.STEEL_SHELL.get().defaultBlockState(), 3);
        level.setBlock(f.above(), SteamBlocks.SMOKESTACK.get().defaultBlockState(), 3);
        level.setBlock(f.east(2).above(), SteamBlocks.WHISTLE.get().defaultBlockState(), 3);
        level.setBlock(f.south(), SteamBlocks.CYLINDER.get().defaultBlockState(), 3);
        level.setBlock(f.east().south(), SteamBlocks.CYLINDER.get().defaultBlockState(), 3);
        level.setBlock(f.east(3), AllBlocks.TRAIN_CONTROLS.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.EAST), 3);
        boilers.add(f);
        // Create holds a carriage together with super glue
        Set<BlockPos> parts = new HashSet<>();
        for (BlockPos p : BlockPos.betweenClosed(new BlockPos(x0 + 1, ty + 1, z0), new BlockPos(x0 + 5, ty + 3, z0 + 1))) {
            if (!level.getBlockState(p).isAir()) parts.add(p.immutable());
        }
        for (BlockPos p : parts) {
            for (Direction d : new Direction[] {Direction.EAST, Direction.UP, Direction.SOUTH}) {
                BlockPos q = p.relative(d);
                if (!parts.contains(q)) continue;
                SuperGlueEntity glue = new SuperGlueEntity(level, SuperGlueEntity.span(p, q));
                level.addFreshEntity(glue);
                glues.add(glue);
            }
        }
        if (hot && level.getBlockEntity(f) instanceof BoilerBlockEntity be) {
            be.state.water = 3000;
            be.state.pressure = 9F;
            be.state.addFuel(new ItemStack(Items.COAL, 32), Hand.burn(level));
            be.setChanged();
        }
    }

    private static void step() {
        if (tick == 0) {
            yard(0, true);
            yard(1, false);
        }
        if (tick == 40) {
            for (BlockPos f : boilers) LOG.info("steamrails controls block: {} boiler {} bogey {} deck {}", level.getBlockState(f.east(3)), level.getBlockState(f), level.getBlockState(f.below()), level.getBlockState(f.below().east(3)));
            Set<UUID> before = new HashSet<>(Create.RAILWAYS.sided(level).trains.keySet());
            for (int i = 0; i < 2; i++) {
                if (!(level.getBlockEntity(stations.get(i)) instanceof StationBlockEntity se)) {
                    fails.add("no station block entity " + i);
                    continue;
                }
                check(se.getStation() != null, "station " + i + " is not on the track graph");
                se.assemble(UUID.randomUUID());
                String ex = se.lastException == null ? "" : se.lastException.component.getString();
                if (!ex.isEmpty()) fails.add("assembly " + i + ": " + ex);
                Train found = null;
                for (Train t : Create.RAILWAYS.sided(level).trains.values()) {
                    if (!before.contains(t.id) && t.carriages.get(0).leadingBogey().leading().node1 != null) {
                        Vec3 p = t.carriages.get(0).leadingBogey().leading().getPosition(t.graph);
                        if (Math.abs(p.z - (stations.get(i).getZ() - 0.5)) < 1.5) found = t;
                    }
                }
                trains[i] = found;
                check(found != null, "train " + i + " was not assembled");
                if (found != null) {
                    before.add(found.id);
                    startAt[i] = found.carriages.get(0).leadingBogey().leading().getPosition(found.graph);
                }
            }
            if (trains[0] == null || trains[1] == null) {
                finish();
                return;
            }
            LOG.info("steamrails train test: assembled {} and {}", trains[0].id, trains[1].id);
        }
        if (tick > 40 && tick <= 400) {
            for (int i = 0; i < 2; i++) {
                Train t = trains[i];
                t.manualTick = true;
                t.targetSpeed = t.maxSpeed();
                t.approachTargetSpeed(1F);
                Vec3 p = t.carriages.get(0).leadingBogey().leading().getPosition(t.graph);
                moved[i] = Math.max(moved[i], p.distanceTo(startAt[i]));
            }
            if (tick % 40 == 0) {
                LOG.info("steamrails train test tick {} hot: speed={} max={} moved={} thrust={}; cold: speed={} max={} moved={}", tick, trains[0].speed, trains[0].maxSpeed(), moved[0],
                        TrainPower.thrustOf(trains[0].id), trains[1].speed, trains[1].maxSpeed(), moved[1]);
            }
        }
        if (tick == 400) {
            check(TrainPower.isSteamTrain(trains[0].id), "the hot train never reported steam: its boiler did not tick on the carriage");
            check(moved[0] > 4.0, "the hot train moved only " + moved[0] + " blocks");
            check(moved[1] < 0.05, "the cold train moved " + moved[1] + " blocks");
            finish();
        }
    }

    private static void clear() {
        for (BlockPos p : BlockPos.betweenClosed(base.offset(-40, -1, -4), base.offset(16, 6, 26))) {
            if (!level.getBlockState(p).isAir()) level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static void finish() {
        if (!keep) {
            for (SuperGlueEntity g : glues) g.discard();
            for (Train t : trains) if (t != null) Create.RAILWAYS.removeTrain(t.id);
            clear();
        }
        glues.clear();
        if (!keep) for (ChunkPos c : forced) level.setChunkForced(c.x(), c.z(), false);
        forced.clear();
        String line = fails.isEmpty() ? "Steamrails train test PASS" : "Steamrails train test FAIL: " + String.join("; ", fails);
        LOG.info(line);
        source.sendSuccess(() -> Component.literal(line), false);
        tick = -1;
        moved[0] = moved[1] = 0;
    }
}
