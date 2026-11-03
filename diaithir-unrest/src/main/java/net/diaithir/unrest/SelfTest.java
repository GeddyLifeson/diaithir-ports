package net.diaithir.unrest;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Pure field maths plus a nest in a real level: absorbs, grows a tier, posts a wave, a cancelled wave costs nothing. */
public final class SelfTest {
    private SelfTest() {}

    /** Field-only checks, runnable without a server. */
    public static List<String> field() {
        List<String> f = new ArrayList<>();
        Field a = new Field();
        a.add(0, 0, 100);
        a.step(0.4, 0, 0.0);
        if (Math.abs(a.get(0, 0) - 60) > 1e-9) f.add("spread keeps 60 at origin, got " + a.get(0, 0));
        if (Math.abs(a.get(1, 0) - 10) > 1e-9) f.add("neighbour gets 10, got " + a.get(1, 0));
        if (Math.abs(a.total() - 100) > 1e-9) f.add("spread alone must keep the total, got " + a.total());
        a.step(0, 0.5, 0.0);
        if (Math.abs(a.total() - 50) > 1e-9) f.add("decay halves the total, got " + a.total());
        Field b = new Field();
        b.add(-3, -7, 5);
        if (b.get(-3, -7) != 5 || b.get(3, 7) != 0) f.add("negative chunk keys");
        if (Field.cx(Field.key(-3, -7)) != -3 || Field.cz(Field.key(-3, -7)) != -7) f.add("key round trip");
        double took = b.take(-3, -7, 9);
        if (took != 5 || b.size() != 0) f.add("take is capped by what is there");
        b.add(0, 0, 1);
        b.add(0, 0, -5);
        if (b.get(0, 0) != 0) f.add("quieting never goes below zero");
        if (NestBlockEntity.tierFor(0) != 0 || NestBlockEntity.tierFor(1500) != 2 || NestBlockEntity.tierFor(1e9) != 4) f.add("tier thresholds");
        return f;
    }

    static List<String> run(MinecraftServer server) {
        List<String> fails = field();
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) return List.of("no overworld");
        BlockPos at = new BlockPos(level.getRespawnData().pos().getX(), 240, level.getRespawnData().pos().getZ());
        AtomicInteger waves = new AtomicInteger();
        boolean[] cancel = {false};
        Object listener = new Object() {
            @SubscribeEvent
            public void on(NestWaveEvent e) {
                waves.incrementAndGet();
                e.setCanceled(cancel[0]);
            }
        };
        NeoForge.EVENT_BUS.register(listener);
        try {
            level.setBlock(at, DiaithirUnrest.NEST.get().defaultBlockState(), 3);
            NestBlockEntity nest = (NestBlockEntity) level.getBlockEntity(at);
            if (nest == null) return List.of("nest has no block entity");
            Field f = Unrest.field(level);
            net.minecraft.world.level.ChunkPos c = net.minecraft.world.level.ChunkPos.containing(at);
            // a loud chunk two away, plenty of Unrest
            f.add(c.x() + 2, c.z(), 4000);
            double before = f.total();
            nest.step(level);
            if (nest.store() <= 0) fails.add("nest absorbed nothing");
            if (f.total() >= before) fails.add("absorbing did not lower the field");
            if (waves.get() == 0) fails.add("no wave posted at store " + nest.store());
            // cancelled waves cost nothing
            cancel[0] = true;
            double store = nest.store();
            f.add(c.x() + 2, c.z(), 4000);
            nest.step(level);
            if (nest.store() < store) fails.add("a cancelled wave still cost store");
            // Meter reads the chunk
            level.setBlock(at.above(), DiaithirUnrest.METER.get().defaultBlockState(), 3);
            if (Unrest.at(level, at) < 0) fails.add("negative reading");
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
            level.setBlock(at, Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(at.above(), Blocks.AIR.defaultBlockState(), 3);
            Field f = Unrest.field(level);
            f.raw().clear();
        }
        return fails;
    }
}
