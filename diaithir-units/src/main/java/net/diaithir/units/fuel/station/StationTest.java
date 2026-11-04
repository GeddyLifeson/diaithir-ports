package net.diaithir.units.fuel.station;

import java.util.List;
import net.diaithir.units.fuel.Fuel;
import net.diaithir.units.fuel.Fuels;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;

/** /diaithirfuel selftest: real blocks in the world, a fake vehicle tank. Checks rates, fuel never lost or created, no mixing, rack faster than pump, save and load. */
final class StationTest {
    private StationTest() {}

    private static final class Fake implements FuelTarget {
        final String accepts;
        double litres, capacity;

        Fake(String accepts, double capacity) { this.accepts = accepts; this.capacity = capacity; }

        @Override public double free(Fuel f) { return f.id().equals(accepts) ? capacity - litres : 0; }

        @Override public double fill(Fuel f, double l) { double ok = Math.min(l, free(f)); litres += ok; return ok; }
    }

    static String run(MinecraftServer server) {
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) return "no overworld";
        BlockPos base = new BlockPos(level.getRespawnData().pos().getX() + 8, 250, level.getRespawnData().pos().getZ() + 8);
        level.getChunk(base);
        for (BlockPos b : new BlockPos[]{base, base.east(), base.west()}) {
            if (!level.getBlockState(b).isAir()) return "test position occupied at " + b + "; the test would destroy it";
        }
        Fake car = new Fake("diesel", 50), petrolCar = new Fake("petrol", 50);
        var source = (java.util.function.BiFunction<Level, net.minecraft.world.phys.AABB, List<FuelTarget>>) (l, b) -> List.of(car, petrolCar);
        Targets.SOURCES.add(source);
        BlockPos tank = base, pump = base.east(), rack = base.west();
        try {
            level.setBlock(tank, Station.STEEL_TANK.get().defaultBlockState(), 3);
            level.setBlock(pump, Station.PUMP_STATION.get().defaultBlockState(), 3);
            level.setBlock(rack, Station.LOADING_RACK.get().defaultBlockState(), 3);
            TankBlockEntity t = (TankBlockEntity) level.getBlockEntity(tank);
            PumpBlockEntity p = (PumpBlockEntity) level.getBlockEntity(pump), r = (PumpBlockEntity) level.getBlockEntity(rack);
            if (t == null || p == null || r == null) return "block entities missing";
            Fuel diesel = Fuels.get("diesel"), petrol = Fuels.get("petrol");
            if (t.store.insert(diesel, 100) != 100) return "tank did not take 100 L";
            if (t.store.insert(petrol, 10) != 0) return "tank mixed fuels";
            if (Math.abs(p.pump(level, 1) - 1) > 1e-9) return "pump station rate wrong: " + p.lastMoved;
            if (Math.abs(r.pump(level, 20) - 20) > 1e-9) return "loading rack rate wrong: " + r.lastMoved;
            if (Math.abs(car.litres - 21) > 1e-9 || Math.abs(t.store.quantity() - 79) > 1e-9) return "fuel not conserved: car " + car.litres + " tank " + t.store.quantity();
            if (petrolCar.litres != 0) return "diesel went into a petrol car";
            r.pump(level, 100);
            if (Math.abs(car.litres - 50) > 1e-9 || Math.abs(t.store.quantity() - 50) > 1e-9) return "full target not respected: car " + car.litres + " tank " + t.store.quantity();
            var out = net.minecraft.world.level.storage.TagValueOutput.createWithoutContext(net.minecraft.util.ProblemReporter.DISCARDING);
            t.saveWithoutMetadata(out);
            level.setBlock(tank, Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(tank, Station.STEEL_TANK.get().defaultBlockState(), 3);
            TankBlockEntity t2 = (TankBlockEntity) level.getBlockEntity(tank);
            t2.loadWithComponents(TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), out.buildResult()));
            if (t2.store.fuel() != diesel || Math.abs(t2.store.quantity() - 50) > 1e-9) return "tank did not save and load: " + t2.store.fuel() + " " + t2.store.quantity();
            return null;
        } finally {
            Targets.SOURCES.remove(source);
            for (BlockPos b : new BlockPos[]{tank, pump, rack}) level.setBlock(b, Blocks.AIR.defaultBlockState(), 3);
        }
    }
}
