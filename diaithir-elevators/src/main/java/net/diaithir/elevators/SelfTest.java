package net.diaithir.elevators;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

/** Builds a three floor shaft high over the overworld, rides a pig up and down, checks a blocked shaft, and tears it all down. */
final class SelfTest {
    private SelfTest() {}

    static List<String> run(MinecraftServer server) {
        List<String> f = new ArrayList<>();
        ServerLevel level = server.overworld();
        BlockPos spawn = level.getRespawnData().pos();
        int bx = spawn.getX() + 96, bz = spawn.getZ(), by = 150;
        level.getChunk(bx >> 4, bz >> 4);
        level.getChunk((bx + 1) >> 4, bz >> 4);
        for (int y = by - 1; y <= by + 16; y++) level.setBlock(new BlockPos(bx, y, bz), Blocks.AIR.defaultBlockState(), 3);
        for (int i = 0; i < 3; i++) level.setBlock(new BlockPos(bx + 1, by + 5 * i, bz), ElevatorsMod.FLOOR.get().defaultBlockState(), 3);
        BlockPos ctl = new BlockPos(bx, by + 14, bz);
        level.setBlock(ctl, ElevatorsMod.CONTROLLER.get().defaultBlockState(), 3);
        Entity pig = null;
        try {
            if (!(level.getBlockEntity(ctl) instanceof ControllerEntity c)) {
                f.add("no controller block entity");
                return f;
            }
            if (!c.floors().equals(List.of(by, by + 5, by + 10))) f.add("floors found " + c.floors());
            pig = EntityType.PIG.spawn(level, new BlockPos(bx, by + 1, bz), EntitySpawnReason.COMMAND);
            if (pig == null) {
                f.add("pig did not spawn");
                return f;
            }
            String e = c.request(by + 10);
            if (e != null) f.add("request refused: " + e);
            if (c.phase != ControllerEntity.MOVING) f.add("cabin not moving");
            int ticks = settle(c);
            if (c.phase != ControllerEntity.PARKED || c.parkedY != by + 10) f.add("cabin not parked at top: " + c.phase + " " + c.parkedY);
            if (ticks < 20) f.add("moved implausibly fast: " + ticks + " ticks");
            if (!level.getBlockState(new BlockPos(bx, by + 10, bz)).is(ElevatorsMod.PLATE.get())) f.add("no plate at top");
            if (!level.getBlockState(new BlockPos(bx, by, bz)).isAir()) f.add("plate left at bottom");
            if (Math.abs(pig.getY() - (by + 11)) > 0.05) f.add("pig at " + pig.getY() + " expected " + (by + 11));
            String again = c.request(by + 10);
            if (again == null) f.add("request to current floor was accepted");

            e = c.cycle(true);
            if (e != null) f.add("cycle down refused: " + e);
            settle(c);
            if (c.parkedY != by + 5) f.add("cycle down parked at " + c.parkedY);
            if (Math.abs(pig.getY() - (by + 6)) > 0.05) f.add("pig at " + pig.getY() + " expected " + (by + 6));

            level.setBlock(new BlockPos(bx, by + 8, bz), Blocks.STONE.defaultBlockState(), 3);
            if (c.request(by + 10) == null) f.add("blocked shaft was not refused");
            level.setBlock(new BlockPos(bx, by + 8, bz), Blocks.AIR.defaultBlockState(), 3);
            if (c.request(by) != null) f.add("request to bottom refused");
            settle(c);
            if (c.parkedY != by || Math.abs(pig.getY() - (by + 1)) > 0.05) f.add("not back at the bottom: " + c.parkedY + " pig " + pig.getY());
        } finally {
            if (pig != null) pig.discard();
            for (int y = by - 1; y <= by + 16; y++) level.setBlock(new BlockPos(bx, y, bz), Blocks.AIR.defaultBlockState(), 3);
            for (int i = 0; i < 3; i++) level.setBlock(new BlockPos(bx + 1, by + 5 * i, bz), Blocks.AIR.defaultBlockState(), 3);
        }
        return f;
    }

    private static int settle(ControllerEntity c) {
        int n = 0;
        while (c.phase == ControllerEntity.MOVING && n < 2000) {
            c.serverTick();
            n++;
        }
        return n;
    }
}
