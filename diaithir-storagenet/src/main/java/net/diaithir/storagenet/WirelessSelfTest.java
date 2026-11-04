package net.diaithir.storagenet;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.List;

/** Two networks in two dimensions (overworld and nether), each with a controller, a transmitter and a chest; checks the link, the pull, the push and unlinking. */
final class WirelessSelfTest {
    private WirelessSelfTest() {}

    private static int total(ChestBlockEntity c, ItemResource r) {
        int n = 0;
        for (int i = 0; i < c.getContainerSize(); i++) if (r.matches(c.getItem(i))) n += c.getItem(i).getCount();
        return n;
    }

    private static void clear(ServerLevel level, BlockPos at) {
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-3, -3, -3), at.offset(3, 3, 3))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
    }

    static List<String> run(MinecraftServer server) {
        List<String> fails = new ArrayList<>();
        ServerLevel over = server.getLevel(Level.OVERWORLD), nether = server.getLevel(Level.NETHER);
        if (over == null || nether == null) return List.of("overworld or nether missing");
        BlockPos a = new BlockPos(over.getRespawnData().pos().getX() + 40, 240, over.getRespawnData().pos().getZ() + 40);
        BlockPos b = new BlockPos(7, 100, 7);
        clear(over, a);
        clear(nether, b);
        try {
            for (var s : new Object[][] {{over, a}, {nether, b}}) {
                ServerLevel l = (ServerLevel) s[0];
                BlockPos p = (BlockPos) s[1];
                l.setBlock(p, DiaithirStorageNet.CONTROLLER.get().defaultBlockState(), 3);
                l.setBlock(p.east(), DiaithirStorageNet.WIRELESS.get().defaultBlockState(), 3);
                l.setBlock(p.above(), Blocks.CHEST.defaultBlockState(), 3);
            }
            ChestBlockEntity chestA = (ChestBlockEntity) over.getBlockEntity(a.above()), chestB = (ChestBlockEntity) nether.getBlockEntity(b.above());
            WirelessBlockEntity ta = (WirelessBlockEntity) over.getBlockEntity(a.east()), tb = (WirelessBlockEntity) nether.getBlockEntity(b.east());
            if (chestA == null || chestB == null || ta == null || tb == null) return List.of("block entities missing");
            chestA.setItem(0, new ItemStack(Items.COBBLESTONE, 64));
            ta.setFrequency(7);
            tb.setFrequency(8);
            Network netA = Networks.find(over, a.east()), netB = Networks.find(nether, b.east());
            if (netA == null || netB == null) return List.of("a transmitter is not reached by its controller");
            ItemResource cobble = ItemResource.of(Items.COBBLESTONE), iron = ItemResource.of(Items.IRON_INGOT);
            if (netB.count(cobble) != 0) fails.add("different frequencies already linked: " + netB.count(cobble));
            tb.setFrequency(7);
            netA = Networks.find(over, a.east());
            netB = Networks.find(nether, b.east());
            if (netB.count(cobble) != 64) fails.add("nether sees " + netB.count(cobble) + " cobblestone, expected 64");
            if (netB.contents().getOrDefault(cobble, 0L) != 64) fails.add("contents() misses the linked chest");
            if (netB.peers().size() != 1) fails.add("peer count " + netB.peers().size());
            ItemStack rest = netB.insertStack(new ItemStack(Items.IRON_INGOT, 10));
            if (!rest.isEmpty()) fails.add("iron left over: " + rest.getCount());
            if (total(chestB, iron) != 10) fails.add("iron should land in the nearer chest first, nether chest has " + total(chestB, iron));
            if (netA.count(iron) != 10) fails.add("overworld sees " + netA.count(iron) + " iron, expected 10");
            ItemStack got = netB.extractStack(cobble, 30);
            if (got.getCount() != 30 || total(chestA, cobble) != 34) fails.add("pull across dimensions gave " + got.getCount() + ", overworld chest has " + total(chestA, cobble));
            tb.setFrequency(8);
            netB = Networks.find(nether, b.east());
            if (netB.count(cobble) != 0) fails.add("still linked after changing frequency");
            tb.setFrequency(7);
            if (Networks.find(nether, b.east()).count(cobble) != 34) fails.add("not relinked");
        } finally {
            clear(over, a);
            clear(nether, b);
        }
        return fails;
    }
}
