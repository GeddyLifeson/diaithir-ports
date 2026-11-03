package net.diaithir.upgrades;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Server-side check with no player: slots, kinds, persistence, removal, beacon sharing, planner swap.
 * Runs once at server start when the environment variable DIAITHIR_UPGRADES_SELFTEST is set.
 */
final class SelfTest {
    private SelfTest() {}

    private static ModuleSpec spec(String id) { return ModuleRegistry.get(id).orElseThrow(); }

    static List<String> run(MinecraftServer server) {
        List<String> f = new ArrayList<>();
        ServerLevel level = server.overworld();
        BlockPos m = new BlockPos(4800, 90, 4800), b = m.east(2), far = m.east(40);
        level.getChunk(m.getX() >> 4, m.getZ() >> 4);
        level.getChunk(far.getX() >> 4, far.getZ() >> 4);
        Upgrades.TARGETS.add(new TargetRegistry.Rule("minecraft:furnace", 0, 2,
                EnumSet.of(Kind.SPEED, Kind.PRODUCTIVITY, Kind.EFFICIENCY), 4));
        level.setBlock(m, Blocks.FURNACE.defaultBlockState(), 3);
        level.setBlock(b, Registry.BEACON.get().defaultBlockState(), 3);
        level.setBlock(far, Blocks.FURNACE.defaultBlockState(), 3);
        BlockEntity me = level.getBlockEntity(m), be = level.getBlockEntity(b), fe = level.getBlockEntity(far);
        if (me == null || be == null || fe == null) { f.add("block entities missing"); return f; }

        if (Upgrades.host(me).isEmpty()) f.add("furnace has no host despite rule");

        ModuleSpec spd1 = spec("circuit_speed_1"), spd2 = spec("circuit_speed_2"), prod1 = spec("ogham_productivity_1"),
                qual1 = spec("spice_quality_1");
        if (!Upgrades.insert(me, spd1, Quality.NORMAL)) f.add("insert speed_1 failed");
        if (Math.abs(Upgrades.host(me).get().effects().speedMultiplier() - 1.2) > 1e-9) f.add("speed multiplier after one module is not 1.2");
        if (Upgrades.host(me).get().slots().modules().size() != 1) f.add("module not persisted in attachment");
        if (Upgrades.insert(me, qual1, Quality.NORMAL)) f.add("quality module accepted where not allowed");
        if (!Upgrades.insert(me, prod1, Quality.NORMAL)) f.add("insert productivity failed");
        if (Upgrades.insert(me, spd1, Quality.NORMAL)) f.add("third module accepted into two slots");
        double outx = Upgrades.host(me).get().effects().outputMultiplier();
        if (Math.abs(outx - 1.04) > 1e-9) f.add("output multiplier " + outx + " is not 1.04");
        double w = Upgrades.host(me).get().powerWatts(1000);
        if (Math.abs(w - 1900) > 1e-6) f.add("power " + w + " W is not 1900 (speed +50%, productivity +40%)");

        // beacon: speed shared at half, nothing from an out-of-range machine
        if (!Upgrades.insert(be, spd1, Quality.NORMAL)) f.add("insert into beacon failed");
        if (Upgrades.insert(be, prod1, Quality.NORMAL)) f.add("beacon accepted productivity");
        double sp = Upgrades.host(me).get().effects().speedMultiplier();
        if (Math.abs(sp - 1.25) > 1e-9) f.add("speed with beacon " + sp + " is not 1.25 (own +15%, beacon +10%)");
        if (Upgrades.host(me).get().effects().outputMultiplier() != outx) f.add("beacon changed productivity");
        Upgrades.insert(fe, spd1, Quality.NORMAL);
        if (Math.abs(Upgrades.host(fe).get().effects().speedMultiplier() - 1.2) > 1e-9) f.add("out-of-range machine got beacon bonus");

        // planner swap: speed_1 -> speed_2 on both machines in range; far one is out of the 8-block radius
        int swaps = PlannerItem.upgradeArea(level, m, spd2, 99, old -> { });
        if (swaps != 2) f.add("planner swapped " + swaps + " modules, expected 2 (furnace and beacon in range)");
        if (!Upgrades.host(fe).get().slots().modules().get(0).spec().equals(spd1)) f.add("planner touched machine out of range");

        // removal
        int before = Upgrades.host(me).get().slots().modules().size();
        if (!Upgrades.remove(me, before - 1) || Upgrades.host(me).get().slots().modules().size() != before - 1) f.add("remove failed");

        // beacon gone: bonus gone
        level.setBlock(b, Blocks.AIR.defaultBlockState(), 3);
        if (!Upgrades.host(me).get().beacons().isEmpty()) f.add("removed beacon still covers machine");

        for (BlockPos p : List.of(m, far)) level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        return f;
    }
}
