package net.diaithir.vehicles;

import net.diaithir.vehicles.tracks.TrackedChassis;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Blocks;

/** /diaithirvehicles trackstest: a chassis on a stone pad high above spawn drives straight, burns diesel, pivots, and coasts to a stop when dry. */
final class TrackTest {
    private TrackTest() {}

    static String run(ServerLevel level) {
        StringBuilder bad = new StringBuilder();
        int n = 0;
        BlockPos base = level.getRespawnData().pos().above(80);
        level.getChunkAt(base);
        for (int x = -12; x <= 12; x++) for (int z = -12; z <= 12; z++) level.setBlockAndUpdate(base.offset(x, -1, z), Blocks.STONE.defaultBlockState());
        TrackedChassis c = DiaithirVehicles.TRACKED_CHASSIS.get().create(level, EntitySpawnReason.COMMAND);
        c.setPos(base.getX() + 0.5, base.getY(), base.getZ() + 0.5);
        c.setYRot(0);
        level.addFreshEntity(c);
        c.tank.insert(net.diaithir.units.fuel.Fuels.get("diesel"), 100);
        double z0 = c.getZ();
        double burnt = 0;
        for (int i = 0; i < 100; i++) burnt += c.driveStep(1, 0);
        n++; if (c.getZ() - z0 < 15) bad.append(" drove only ").append(c.getZ() - z0);
        n++; if (c.speed() < 9.5) bad.append(" speed ").append(c.speed());
        n++; if (burnt <= 0 || c.tank.quantity() >= 100) bad.append(" no fuel burnt");
        n++; if (Math.abs(c.getYRot()) > 0.01) bad.append(" drifted");
        for (int i = 0; i < 40; i++) c.driveStep(0, 0);
        c.drive.stop();
        float y0 = c.getYRot();
        double x1 = c.getX(), z1 = c.getZ();
        for (int i = 0; i < 100; i++) c.driveStep(0, 1);
        n++; if (c.getYRot() - y0 < 90) bad.append(" pivot ").append(c.getYRot() - y0);
        n++; if (Math.hypot(c.getX() - x1, c.getZ() - z1) > 2.0) bad.append(" pivot moved");
        c.tank.extract(1000);
        for (int i = 0; i < 100; i++) c.driveStep(1, 0);
        n++; if (Math.abs(c.speed()) > 0.01) bad.append(" dry speed ").append(c.speed());
        c.discard();
        for (int x = -12; x <= 12; x++) for (int z = -12; z <= 12; z++) level.removeBlock(base.offset(x, -1, z), false);
        return bad.length() == 0 ? "PASS " + n + " checks" : "FAIL" + bad;
    }
}
