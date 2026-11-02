package net.diaithir.routing.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Barrel: capacity, side rules, one kind only, rollback. Filter: exact, half-pair, same-mod pair, empty. Sieve: shows and blocks by filter,
 * inverted by a redstone block. Router: matching face first, overflow to empty-filter faces, never back out the source face.
 */
final class RoutingSelfTest {
    private RoutingSelfTest() {}

    private static int total(ChestBlockEntity c, ItemResource r) {
        int n = 0;
        for (int i = 0; i < c.getContainerSize(); i++) if (r.matches(c.getItem(i))) n += c.getItem(i).getCount();
        return n;
    }

    private static ChestBlockEntity chest(ServerLevel l, BlockPos p) {
        l.setBlock(p, Blocks.CHEST.defaultBlockState(), 3);
        return (ChestBlockEntity) l.getBlockEntity(p);
    }

    private static int insert(ResourceHandler<ItemResource> h, ItemResource r, int n, boolean commit) {
        try (Transaction tx = Transaction.openRoot()) {
            int in = h.insert(r, n, tx);
            if (commit) tx.commit();
            return in;
        }
    }

    private static int extract(ResourceHandler<ItemResource> h, ItemResource r, int n) {
        try (Transaction tx = Transaction.openRoot()) {
            int out = h.extract(r, n, tx);
            tx.commit();
            return out;
        }
    }

    static List<String> run(MinecraftServer server) {
        List<String> fails = new ArrayList<>();
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) return List.of("no overworld");
        BlockPos at = new BlockPos(level.getRespawnData().pos().getX(), 240, level.getRespawnData().pos().getZ());
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-4, -4, -4), at.offset(4, 4, 4))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
        ItemResource stone = ItemResource.of(Items.STONE), log = ItemResource.of(Items.OAK_LOG), dirt = ItemResource.of(Items.DIRT), sword = ItemResource.of(Items.IRON_SWORD);
        try {
            // filter logic
            Filter f = new Filter();
            if (f.matches(stone)) fails.add("empty filter matched something");
            f.add(stone);
            if (!f.matches(stone) || f.matches(dirt)) fails.add("single entry is not exact");
            f.add(log);
            if (!f.matches(log) || f.matches(dirt)) fails.add("second entry not exact");
            Filter pair = new Filter();
            pair.add(stone); pair.add(log); pair.add(ItemResource.of(Items.IRON_INGOT)); pair.add(ItemResource.of(Items.GOLD_INGOT)); // fills even slots
            pair.add(ItemResource.of(Items.COBBLESTONE)); // slot 1, pairs with stone: same mod, so all minecraft items pass
            if (!pair.matches(dirt)) fails.add("same-mod pair did not match the whole mod");

            // barrel
            level.setBlock(at, DiaithirRouting.BARREL.get().defaultBlockState(), 3);
            BarrelBlockEntity barrel = (BarrelBlockEntity) level.getBlockEntity(at);
            if (barrel == null) return List.of("barrel did not place");
            ResourceHandler<ItemResource> top = barrel.handler(Direction.UP), bottom = barrel.handler(Direction.DOWN);
            int cap = 64 * 64;
            if (insert(top, stone, 100000, true) != cap) fails.add("barrel took " + barrel.store.count() + ", wanted " + cap);
            if (insert(top, dirt, 1, true) != 0) fails.add("barrel took a second kind");
            if (extract(top, stone, 1) != 0) fails.add("barrel gave items out of the top");
            if (extract(bottom, stone, 100) != 100 || barrel.store.count() != cap - 100) fails.add("barrel bottom extraction wrong");
            if (insert(top, stone, 50, false) != 50 || barrel.store.count() != cap - 100) fails.add("barrel transaction did not roll back");
            if (extract(bottom, stone, cap) != cap - 100 || !barrel.store.resource().isEmpty()) fails.add("emptied barrel kept its kind");
            insert(top, sword, 1000, true);
            if (barrel.store.count() != 64) fails.add("barrel held " + barrel.store.count() + " unstackable items, wanted 64");

            // sieve: chest at north, sieve at `at`, pointing north
            level.setBlock(at, Blocks.AIR.defaultBlockState(), 3);
            ChestBlockEntity chest = chest(level, at.north());
            level.setBlock(at, DiaithirRouting.SIEVE.get().defaultBlockState().setValue(SieveBlock.FACING, Direction.NORTH), 3);
            SieveBlockEntity sieve = (SieveBlockEntity) level.getBlockEntity(at);
            chest.setItem(0, new ItemStack(Items.STONE, 10));
            chest.setItem(1, new ItemStack(Items.DIRT, 10));
            ResourceHandler<ItemResource> view = level.getCapability(Capabilities.Item.BLOCK, at, Direction.SOUTH);
            if (view == null) return List.of("sieve has no item capability");
            if (level.getCapability(Capabilities.Item.BLOCK, at, Direction.NORTH) != null) fails.add("sieve front face exposes an inventory");
            if (extract(view, dirt, 1) != 1) fails.add("empty sieve blocked an item");
            sieve.filter.add(stone);
            if (extract(view, dirt, 1) != 0) fails.add("sieve passed an item outside its filter");
            if (insert(view, dirt, 1, false) != 0) fails.add("sieve accepted an item outside its filter");
            if (extract(view, stone, 1) != 1) fails.add("sieve blocked a filtered item");
            if (insert(view, stone, 1, true) != 1) fails.add("sieve refused a filtered item");
            level.setBlock(at.south(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
            if (extract(view, stone, 1) != 0) fails.add("redstone did not invert the sieve (blocked item passed)");
            if (extract(view, dirt, 1) != 1) fails.add("redstone did not invert the sieve (other item blocked)");
            level.setBlock(at.south(), Blocks.AIR.defaultBlockState(), 3);

            // router: input from north; east and west filtered, up open
            chest.clearContent();
            level.setBlock(at, Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(at.north(), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(at, DiaithirRouting.ROUTER.get().defaultBlockState(), 3);
            RouterBlockEntity router = (RouterBlockEntity) level.getBlockEntity(at);
            ChestBlockEntity east = chest(level, at.east()), west = chest(level, at.west()), up = chest(level, at.above()), north = chest(level, at.north());
            router.filter(Direction.EAST).add(stone);
            router.filter(Direction.WEST).add(log);
            ResourceHandler<ItemResource> in = level.getCapability(Capabilities.Item.BLOCK, at, Direction.NORTH);
            if (in == null) return List.of("router has no item capability");
            insert(in, stone, 20, true);
            insert(in, log, 5, true);
            insert(in, dirt, 7, true);
            if (total(east, stone) != 20 || total(west, log) != 5 || total(up, dirt) != 7) fails.add("router sent stone/log/dirt to " + total(east, stone) + "/" + total(west, log) + "/" + total(up, dirt));
            if (total(east, dirt) + total(west, dirt) + total(east, log) + total(west, stone) != 0) fails.add("router leaked an item onto a filtered face");
            if (total(north, stone) + total(north, dirt) != 0) fails.add("router sent an item back out of its input face");
            if (insert(in, stone, 20, false) != 20 || total(east, stone) != 20) fails.add("router transaction did not roll back");
        } finally {
            for (BlockPos p : BlockPos.betweenClosed(at.offset(-4, -4, -4), at.offset(4, 4, 4))) {
                if (level.getBlockEntity(p) instanceof ChestBlockEntity ch) ch.clearContent();
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
            }
        }
        return fails;
    }
}
