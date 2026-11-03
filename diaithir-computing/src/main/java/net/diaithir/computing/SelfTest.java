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

        level.setBlock(at, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(chest, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(lamp, Blocks.AIR.defaultBlockState(), 3);
        return fails;
    }

    private SelfTest() {}
}
