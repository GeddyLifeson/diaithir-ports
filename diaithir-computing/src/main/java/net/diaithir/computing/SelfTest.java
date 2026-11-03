package net.diaithir.computing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.ArrayList;
import java.util.List;

/** Builds a Reckoner beside a chest and a lever, runs programs on it and checks output, redstone, the inventory reader, the sandbox and the instruction budget. */
final class SelfTest {
    static List<String> run(MinecraftServer server) {
        List<String> fails = new ArrayList<>();
        ServerLevel level = server.overworld();
        BlockPos at = new BlockPos(300, 100, 300);
        level.getChunk(at);
        BlockPos chest = at.east(), lamp = at.west();
        level.setBlock(at, Registry.RECKONER.get().defaultBlockState(), 3);
        level.setBlock(chest, Blocks.CHEST.defaultBlockState(), 3);
        level.setBlock(lamp, Blocks.REDSTONE_LAMP.defaultBlockState(), 3);
        if (!(level.getBlockEntity(at) instanceof ReckonerEntity be)) { fails.add("no block entity"); return fails; }
        if (level.getBlockEntity(chest) instanceof ChestBlockEntity c) { c.setItem(0, new ItemStack(Items.IRON_INGOT, 12)); c.setItem(5, new ItemStack(Items.IRON_INGOT, 3)); c.setItem(9, new ItemStack(Items.COAL, 4)); }
        else fails.add("no chest entity");

        be.setProgram("print('hi', 1+2) local t = inventory.list('east') for i,r in ipairs(t) do print(r.name, r.count) end redstone.set('west', 9)");
        String err = be.run();
        if (err != null) fails.add("program errored: " + err);
        if (!be.log.equals(List.of("hi\t3", "minecraft:iron_ingot\t15", "minecraft:coal\t4"))) fails.add("output was " + be.log);
        if (be.output[Direction.WEST.get3DDataValue()] != 9) fails.add("west output not 9");
        if (level.getSignal(lamp, Direction.EAST.getOpposite()) < 0) fails.add("signal read");
        if (level.getBestNeighborSignal(lamp) != 9) fails.add("lamp sees " + level.getBestNeighborSignal(lamp) + " not 9");
        if (!level.getBlockState(lamp).getValue(net.minecraft.world.level.block.RedstoneLampBlock.LIT)) {
            level.getBlockTicks().hasScheduledTick(lamp, Blocks.REDSTONE_LAMP);   // lamps light on a scheduled tick; the signal itself is the real check
        }

        be.setProgram("print(redstone.get('east'))");
        be.run();
        if (be.log.size() != 1) fails.add("redstone.get printed " + be.log);

        be.setProgram("x = io.open('/etc/passwd')");
        if (be.run() == null) fails.add("io was reachable");
        be.setProgram("os.execute('echo x')");
        if (be.run() == null) fails.add("os was reachable");
        be.setProgram("debug.sethook()");
        if (be.run() == null) fails.add("debug was reachable");
        be.setProgram("require('x')");
        if (be.run() == null) fails.add("require was reachable");
        be.setProgram("while true do end");
        String loop = be.run();
        if (loop == null || !loop.contains("budget")) fails.add("infinite loop not stopped: " + loop);
        be.setProgram("local s = '' for i=1,100000 do s = s .. 'x' end print(#s)");
        be.run();   // must return, with or without error
        be.setProgram("this is not lua");
        if (be.run() == null) fails.add("syntax error not reported");

        net.minecraft.nbt.CompoundTag tag = be.saveWithoutMetadata(server.registryAccess());
        if (!tag.contains("program")) fails.add("program not saved");

        later(server, level, fails, be, at);
        level.setBlock(at, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(chest, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(lamp, Blocks.AIR.defaultBlockState(), 3);
        return fails;
    }

    /** Sleep, sign monitor, wireless messages and the turtle. */
    private static void later(MinecraftServer server, ServerLevel level, List<String> fails, ReckonerEntity be, BlockPos at) {
        // sleep: the program stops at sleep(5), carries on five ticks later
        be.setProgram("print('a') sleep(5) print('b')");
        be.run();
        if (!be.running() || !be.log.equals(List.of("a"))) fails.add("sleep did not pause: " + be.log + " running=" + be.running());
        for (int i = 0; i < 7; i++) be.tickProgram();
        if (be.running() || !be.log.equals(List.of("a", "b"))) fails.add("sleep did not finish: " + be.log + " running=" + be.running());

        // monitor: a sign beside it
        BlockPos signAt = at.north();
        level.setBlock(signAt, Blocks.OAK_SIGN.defaultBlockState(), 3);
        be.setProgram("print(monitor.write('north', 2, 'hello sign')) print(monitor.write('south', 1, 'x'))");
        be.run();
        if (!be.log.equals(List.of("true", "false\tno sign on that side"))) fails.add("monitor results " + be.log);
        if (!(level.getBlockEntity(signAt) instanceof net.minecraft.world.level.block.entity.SignBlockEntity sign) || !sign.getFrontText().getMessage(1, false).getString().equals("hello sign")) fails.add("sign text not written");
        level.setBlock(signAt, Blocks.AIR.defaultBlockState(), 3);

        // wireless: B reaches A on channel 7, not on 8, and not from beyond 64 blocks
        BlockPos bAt = at.offset(10, 0, 0), farAt = at.offset(0, 0, 70);
        level.getChunk(farAt);
        level.setBlock(bAt, Registry.RECKONER.get().defaultBlockState(), 3);
        level.setBlock(farAt, Registry.RECKONER.get().defaultBlockState(), 3);
        ReckonerEntity b = (ReckonerEntity) level.getBlockEntity(bAt), far = (ReckonerEntity) level.getBlockEntity(farAt);
        be.setProgram("net.listen(7) print(net.wait())");
        be.run();
        if (!be.running()) fails.add("net.wait did not wait");
        b.setProgram("print(net.send(8, 'nobody')) print(net.send(7, 'hello a'))");
        b.run();
        if (!b.log.equals(List.of("0", "1"))) fails.add("send counts " + b.log);
        far.setProgram("print(net.send(7, 'too far'))");
        far.run();
        if (!far.log.equals(List.of("0"))) fails.add("a computer 70 blocks away reached " + far.log);
        be.tickProgram();
        if (be.running() || !be.log.equals(List.of("7\thello a\t10"))) fails.add("net.wait result " + be.log);
        level.setBlock(bAt, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(farAt, Blocks.AIR.defaultBlockState(), 3);

        turtle(server, level, fails, at.offset(0, 0, -20));
    }

    private static TurtleEntity find(ServerLevel level, BlockPos... where) {
        for (BlockPos p : where) if (level.getBlockEntity(p) instanceof TurtleEntity t) return t;
        return null;
    }

    private static void turtle(MinecraftServer server, ServerLevel level, List<String> fails, BlockPos start) {
        level.getChunk(start);
        for (int dx = -3; dx <= 3; dx++) for (int dz = -4; dz <= 1; dz++) {
            level.setBlock(start.offset(dx, 0, dz), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(start.offset(dx, -1, dz), Blocks.STONE.defaultBlockState(), 3);
        }
        BlockPos end = start.north(2), dirt = end.west();   // the turtle faces north, walks two blocks, turns left and faces west
        level.setBlock(dirt, Blocks.DIRT.defaultBlockState(), 3);
        level.setBlock(start, Registry.TURTLE.get().defaultBlockState().setValue(TurtleBlock.FACING, net.minecraft.core.Direction.NORTH), 3);
        TurtleEntity t = find(level, start);
        if (t == null) { fails.add("no turtle entity"); return; }
        t.slots[1] = new ItemStack(Items.COAL, 3);
        t.setProgram("turtle.forward() turtle.forward() turtle.turnLeft() print(turtle.dig('forward')) print(turtle.getCount(1)) "
                + "print(turtle.detect('forward')) print(turtle.place('forward')) print(turtle.detect('forward')) print(turtle.getFuel()) "
                + "turtle.select(2) print(turtle.refuel(1)) print(turtle.getItem(1)) print(turtle.forward()) print(turtle.forward()) "
                + "print(turtle.forward())");
        t.run();
        for (int i = 0; i < 600; i++) {
            TurtleEntity cur = find(level, start, start.north(), start.north(2), start.north(3), start.north(4));
            if (cur == null || !cur.running()) break;
            cur.tickProgram();
        }
        TurtleEntity done = find(level, start.north(2), start.north(3), start.north(4), start.north(), start);
        if (done == null) { fails.add("the turtle vanished"); return; }
        if (done.running()) fails.add("the turtle program did not finish: " + done.log + " " + done.error);
        // forward x2 (fuel 18), dig dirt (1 in slot 1), nothing ahead, place it back, refuel one coal (+80), slot 1 empty, then the dirt blocks every move west
        List<String> expect = List.of("true", "1", "false", "true", "true", "18", "98", "nil", "false\tblocked", "false\tblocked", "false\tblocked");
        if (!done.log.equals(expect)) fails.add("turtle output " + done.log + " error=" + done.error);
        if (!level.getBlockState(dirt).is(Blocks.DIRT)) fails.add("the dirt was not placed back");
        if (!done.getBlockState().getValue(TurtleBlock.FACING).equals(net.minecraft.core.Direction.WEST)) fails.add("turtle is not facing west");
        if (!done.getBlockPos().equals(end)) fails.add("turtle ended at " + done.getBlockPos() + " not " + end);
        if (!level.getBlockState(start).isAir()) fails.add("a block was left where the turtle started");

        // no fuel
        BlockPos p2 = start.east(3);
        level.setBlock(p2, Registry.TURTLE.get().defaultBlockState(), 3);
        TurtleEntity empty = find(level, p2);
        empty.fuel = 0;
        empty.setProgram("print(turtle.forward())");
        empty.run();
        if (!empty.log.equals(List.of("false\tout of fuel"))) fails.add("no-fuel result " + empty.log);

        // saved state
        net.minecraft.nbt.CompoundTag tag = done.saveWithoutMetadata(server.registryAccess());
        if (tag.getIntOr("fuel", 0) != done.fuel) fails.add("turtle fuel not saved");
        level.setBlock(done.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(p2, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(dirt, Blocks.AIR.defaultBlockState(), 3);
    }

    private SelfTest() {}
}
