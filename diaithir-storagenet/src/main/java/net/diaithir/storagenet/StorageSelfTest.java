package net.diaithir.storagenet;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Builds controller, cables, chests, filter, import, export and terminal high above spawn, then checks scan, insert, extract and the grid. */
final class StorageSelfTest {
    private StorageSelfTest() {}

    private static int total(ChestBlockEntity c, ItemResource r) {
        int n = 0;
        for (int i = 0; i < c.getContainerSize(); i++) if (r.matches(c.getItem(i))) n += c.getItem(i).getCount();
        return n;
    }

    static List<String> run(MinecraftServer server) {
        List<String> fails = new ArrayList<>();
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) return List.of("no overworld");
        BlockPos at = new BlockPos(level.getRespawnData().pos().getX(), 240, level.getRespawnData().pos().getZ());
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-6, -6, -6), at.offset(8, 6, 6))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
        FakePlayer player = FakePlayerFactory.get(level, new GameProfile(UUID.nameUUIDFromBytes("sn-tester".getBytes()), "sn_tester"));
        try {
            BlockPos cable1 = at.offset(1, 0, 0), cable2 = at.offset(2, 0, 0), terminal = at.offset(3, 0, 0);
            BlockPos chestA = at.offset(1, 1, 0), chestB = at.offset(2, -1, 0);
            BlockPos filterCable = at.offset(2, 1, 0);
            BlockPos importCable = at.offset(0, 0, 1), chestC = at.offset(0, 0, 2);
            BlockPos exportCable = at.offset(0, 0, -1), chestD = at.offset(0, 0, -2);
            level.setBlock(at, DiaithirStorageNet.CONTROLLER.get().defaultBlockState(), 3);
            level.setBlock(cable1, DiaithirStorageNet.CABLE.get().defaultBlockState(), 3);
            level.setBlock(cable2, DiaithirStorageNet.CABLE.get().defaultBlockState(), 3);
            level.setBlock(terminal, DiaithirStorageNet.TERMINAL.get().defaultBlockState(), 3);
            level.setBlock(chestA, Blocks.CHEST.defaultBlockState(), 3);
            level.setBlock(chestB, Blocks.CHEST.defaultBlockState(), 3);
            ChestBlockEntity a = (ChestBlockEntity) level.getBlockEntity(chestA);
            ChestBlockEntity b = (ChestBlockEntity) level.getBlockEntity(chestB);
            if (a == null || b == null) return List.of("chests have no block entity");
            a.setItem(0, new ItemStack(Items.COBBLESTONE, 64));
            b.setItem(0, new ItemStack(Items.IRON_INGOT, 10));

            Network net = Networks.find(level, terminal);
            if (net == null) return List.of("terminal not reached by the controller");
            if (net.stores().size() != 2) fails.add("expected 2 inventories, found " + net.stores().size());
            if (net.members().size() != 4) fails.add("expected 4 network blocks, found " + net.members().size());
            ItemResource cobble = ItemResource.of(Items.COBBLESTONE), diamond = ItemResource.of(Items.DIAMOND), iron = ItemResource.of(Items.IRON_INGOT);
            if (net.count(cobble) != 64) fails.add("cobble count " + net.count(cobble));
            if (net.contents().getOrDefault(iron, 0L) != 10) fails.add("iron count in contents");

            // insert and extract through the network
            ItemStack rest = net.insertStack(new ItemStack(Items.DIAMOND, 100));
            if (!rest.isEmpty()) fails.add("diamonds left over: " + rest.getCount());
            if (net.count(diamond) != 100) fails.add("diamond count after insert " + net.count(diamond));
            if (total(a, diamond) + total(b, diamond) != 100) fails.add("diamonds not in the chests");
            ItemStack got = net.extractStack(diamond, 40);
            if (got.getCount() != 40) fails.add("extracted " + got.getCount() + " of 40");
            if (net.count(diamond) != 60) fails.add("diamond count after extract " + net.count(diamond));
            if (!net.extractStack(ItemResource.of(Items.EMERALD), 5).isEmpty()) fails.add("extracted an item that is not stored");

            // terminal grid: left click takes a stack, click with cursor stores, shift click from inventory stores
            player.snapTo(terminal.getX() + 0.5, terminal.getY() + 1, terminal.getZ() + 0.5);
            TerminalMenu menu = new TerminalMenu(1, player.getInventory(), level, terminal);
            int slot = menu.shown().indexOf(cobble);
            if (slot < 0) {
                fails.add("cobble not shown in the grid");
            } else {
                menu.clicked(slot, 0, ContainerInput.PICKUP, player);
                if (menu.getCarried().getCount() != 64 || !menu.getCarried().is(Items.COBBLESTONE)) fails.add("grid click gave " + menu.getCarried());
                if (net.count(cobble) != 0) fails.add("cobble still in network after taking");
                menu.clicked(slot, 0, ContainerInput.PICKUP, player); // click again with cursor: store it back
                if (!menu.getCarried().isEmpty() || net.count(cobble) != 64) fails.add("cursor stack was not stored");
            }
            player.getInventory().setItem(0, new ItemStack(Items.IRON_INGOT, 5));
            menu.clicked(54 + 27, 0, ContainerInput.QUICK_MOVE, player); // hotbar slot 0 is menu slot 54 + 27
            if (net.count(iron) != 15) fails.add("shift click stored wrong amount, iron " + net.count(iron));
            TerminalMenu.setSearch(player, "diam");
            menu.refresh(player);
            if (menu.shown().size() != 1 || !menu.shown().get(0).equals(diamond)) fails.add("search for diam showed " + menu.shown());
            TerminalMenu.setSearch(player, "@minecraft");
            menu.refresh(player);
            if (menu.shown().size() != 3) fails.add("@minecraft search showed " + menu.shown().size() + " kinds");
            TerminalMenu.clearSearch(player);
            menu.setSort(TerminalMenu.Sort.COUNT);
            menu.refresh(player);
            if (!menu.shown().get(0).equals(cobble)) fails.add("count sort did not put the biggest stack first");

            // crafting terminal: craft sticks from two planks; the emptied grid slots refill from the network
            net.insertStack(new ItemStack(Items.OAK_PLANKS, 10));
            NetCraftingMenu cm = new NetCraftingMenu(2, player.getInventory(), ContainerLevelAccess.create(level, terminal), level, terminal);
            cm.getSlot(1).set(new ItemStack(Items.OAK_PLANKS, 1));
            cm.getSlot(4).set(new ItemStack(Items.OAK_PLANKS, 1));
            if (!cm.getSlot(0).getItem().is(Items.STICK)) {
                fails.add("crafting grid made " + cm.getSlot(0).getItem());
            } else {
                cm.clicked(0, 0, ContainerInput.PICKUP, player);
                if (!cm.getCarried().is(Items.STICK) || cm.getCarried().getCount() != 4) fails.add("crafted " + cm.getCarried());
                if (!cm.getSlot(1).getItem().is(Items.OAK_PLANKS) || !cm.getSlot(4).getItem().is(Items.OAK_PLANKS)) fails.add("grid not refilled from the network");
                if (net.count(ItemResource.of(Items.OAK_PLANKS)) != 8) fails.add("planks left in network " + net.count(ItemResource.of(Items.OAK_PLANKS)));
            }

            // filter cable: gold only on chest A, so diamonds go to chest B
            level.setBlock(filterCable, DiaithirStorageNet.FILTER_CABLE.get().defaultBlockState(), 3);
            PortCableBlockEntity fbe = (PortCableBlockEntity) level.getBlockEntity(filterCable);
            fbe.filter().toggle(new ItemStack(Items.GOLD_INGOT));
            Networks.markDirty(level);
            net = Networks.find(level, terminal);
            int beforeA = total(a, diamond);
            net.insertStack(new ItemStack(Items.DIAMOND, 20));
            if (total(a, diamond) != beforeA) fails.add("filter cable let a diamond into the chest it guards");
            net.insertStack(new ItemStack(Items.GOLD_INGOT, 7));
            if (total(a, ItemResource.of(Items.GOLD_INGOT)) != 7) fails.add("whitelisted gold did not go to the guarded chest");

            // import cable pulls from chest C into the network
            level.setBlock(importCable, DiaithirStorageNet.IMPORT_CABLE.get().defaultBlockState(), 3);
            level.setBlock(chestC, Blocks.CHEST.defaultBlockState(), 3);
            ChestBlockEntity c = (ChestBlockEntity) level.getBlockEntity(chestC);
            c.setItem(0, new ItemStack(Items.SAND, 12));
            Networks.markDirty(level);
            int moved = ((PortCableBlockEntity) level.getBlockEntity(importCable)).work(level);
            net = Networks.find(level, terminal);
            ItemResource sand = ItemResource.of(Items.SAND);
            if (moved != 12 || net.count(sand) != 12 || total(c, sand) != 0) fails.add("import moved " + moved + ", network sand " + net.count(sand));
            if (net.stores().size() != 2) fails.add("import chest was counted as storage");

            // export cable with a sand filter pushes into chest D
            level.setBlock(exportCable, DiaithirStorageNet.EXPORT_CABLE.get().defaultBlockState(), 3);
            level.setBlock(chestD, Blocks.CHEST.defaultBlockState(), 3);
            ChestBlockEntity dd = (ChestBlockEntity) level.getBlockEntity(chestD);
            ((PortCableBlockEntity) level.getBlockEntity(exportCable)).filter().toggle(new ItemStack(Items.SAND));
            Networks.markDirty(level);
            moved = ((PortCableBlockEntity) level.getBlockEntity(exportCable)).work(level);
            net = Networks.find(level, terminal);
            if (moved != 12 || total(dd, sand) != 12 || net.count(sand) != 0) fails.add("export moved " + moved + ", chest sand " + total(dd, sand));

            // breaking a cable splits the network
            level.setBlock(cable2, Blocks.AIR.defaultBlockState(), 3);
            if (Networks.find(level, terminal) != null) fails.add("terminal still on the network after its cable was removed");
            if (Networks.find(level, cable1) == null) fails.add("cable next to the controller left the network");
        } catch (Throwable t) {
            fails.add("exception " + t);
            t.printStackTrace();
        } finally {
            for (BlockPos p : BlockPos.betweenClosed(at.offset(-6, -6, -6), at.offset(8, 6, 6))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
            Networks.markDirty(level);
        }
        return fails;
    }
}
