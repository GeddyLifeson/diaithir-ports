package net.diaithir.carts;

import net.diaithir.yards.BuildState;
import net.diaithir.yards.YardRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.properties.RailShape;

import java.util.ArrayList;
import java.util.List;

/** /diaithircarts yardtest: choose a yard, install parts in the wrong and right order, work stage by stage, check there is no roll-out without a rail, then roll a coupled train out. */
final class YardTest {
    private YardTest() {}

    static String run(ServerLevel level) {
        List<String> fails = new ArrayList<>();
        BlockPos at = new BlockPos(0, 244, 0);
        for (int cx = -1; cx <= 0; cx++) for (int cz = -1; cz <= 0; cz++) level.getChunk(cx, cz);
        for (int x = -2; x <= 8; x++) for (int z = -4; z <= 4; z++) {
            for (int y = 0; y <= 3; y++) level.setBlock(at.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(at.offset(x, -1, z), Blocks.STONE.defaultBlockState(), 3);
        }
        level.setBlock(at, DiaithirCarts.RAIL_YARD.get().defaultBlockState(), 3);
        var be = (RailYardBlock.Entity) level.getBlockEntity(at);
        Ages.override = 5;
        if (!be.choose(RailYard.FREIGHT)) fails.add("yard not registered");
        var chassis = YardRegistry.part("diaithircarts:cart_chassis").get();
        var wheels = YardRegistry.part("diaithircarts:wheelset").get();
        var body = YardRegistry.part("diaithircarts:freight_body").get();
        if (be.install(wheels) != BuildState.InstallResult.WRONG_STAGE) fails.add("wheels before the chassis should be the wrong stage");
        if (be.install(chassis) != BuildState.InstallResult.OK) fails.add("chassis refused");
        if (be.install(chassis) != BuildState.InstallResult.NO_SLOT) fails.add("a second chassis should not fit");
        if (be.work(1000)) {
            // stage finished by working a full slot set: expected
        } else fails.add("chassis stage did not finish");
        if (be.install(wheels) != BuildState.InstallResult.OK || be.work(1000)) fails.add("one wheelset should not complete the running gear");
        if (be.install(wheels) != BuildState.InstallResult.OK || !be.work(1000)) fails.add("running gear did not finish");
        if (be.install(body) != BuildState.InstallResult.OK || !be.work(1000)) fails.add("body stage did not finish");
        Ages.override = null;
        if (!be.build.complete()) fails.add("build not complete");
        if (be.launch() != null) fails.add("rolled out with no rail");
        if (!be.build.complete()) fails.add("failed roll-out consumed the build");
        level.setBlock(at.offset(2, 0, 0), Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.EAST_WEST), 3);
        List<?> train = be.launch();
        if (train == null || train.size() != RailYard.CARS) fails.add("no train");
        else {
            int chests = 0, coupled = 0;
            for (Object o : train) {
                AbstractMinecart c = (AbstractMinecart) o;
                if (c instanceof MinecartChest && c.isAlive()) chests++;
                if (Coupling.hasLeader(c)) coupled++;
                c.discard();
            }
            if (chests != 3 || coupled != 2) fails.add("chests " + chests + " coupled " + coupled);
        }
        if (be.build == null || be.build.stageIndex() != 0) fails.add("yard not reset after roll-out");
        level.setBlock(at.offset(2, 0, 0), Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(at, Blocks.AIR.defaultBlockState(), 3);
        return fails.isEmpty() ? "Rail yard self test PASS" : "Rail yard self test FAIL: " + String.join("; ", fails);
    }
}
