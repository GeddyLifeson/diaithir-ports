package net.diaithir.carts;

import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/** /diaithircarts loadertest: chest, loader, rail with a chest cart. Load moves coal into the cart, a redstone signal stops it, unload moves it back. */
final class LoaderTest {
    private LoaderTest() {}

    private static final Logger LOG = LogUtils.getLogger();
    private static CommandSourceStack source;
    private static ServerLevel level;
    private static BlockPos chest, loader, rail;
    private static AbstractMinecart cart;
    private static int t = -1;
    private static final List<String> fails = new ArrayList<>();

    static String start(CommandSourceStack s) {
        if (t >= 0) return "already running";
        source = s;
        level = s.getLevel();
        BlockPos at = new BlockPos(0, 240, 0);
        for (int cx = -1; cx <= 1; cx++) for (int cz = -1; cz <= 0; cz++) { level.getChunk(cx, cz); level.setChunkForced(cx, cz, true); }
        fails.clear();
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            for (int y = 0; y <= 3; y++) level.setBlock(at.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(at.offset(x, -1, z), Blocks.STONE.defaultBlockState(), 3);
        }
        chest = at;
        loader = at.east();
        rail = at.east(2);
        level.setBlock(chest, Blocks.CHEST.defaultBlockState(), 3);
        level.setBlock(loader, DiaithirCarts.CART_LOADER.get().defaultBlockState().setValue(CartLoader.FACING, Direction.EAST), 3);
        level.setBlock(rail, Blocks.RAIL.defaultBlockState().setValue(net.minecraft.world.level.block.RailBlock.SHAPE, RailShape.EAST_WEST), 3);
        ((Container) level.getBlockEntity(chest)).setItem(0, new ItemStack(Items.COAL, 20));
        cart = EntityType.CHEST_MINECART.create(level, EntitySpawnReason.COMMAND);
        cart.setPos(rail.getX() + 0.5, rail.getY() + 0.1, rail.getZ() + 0.5);
        cart.setDeltaMovement(0, 0, 0);
        level.addFreshEntity(cart);
        t = 0;
        return null;
    }

    private static int count(Container c) {
        int n = 0;
        for (int i = 0; i < c.getContainerSize(); i++) n += c.getItem(i).is(Items.COAL) ? c.getItem(i).getCount() : 0;
        return n;
    }

    static void tick(MinecraftServer server) {
        if (t < 0) return;
        t++;
        Container box = (Container) level.getBlockEntity(chest);
        Container cartBox = (Container) cart;
        if (t == 60) {
            if (count(cartBox) != 20 || count(box) != 0) fails.add("load: cart " + count(cartBox) + " chest " + count(box));
            level.setBlock(loader, level.getBlockState(loader).setValue(CartLoader.UNLOAD, true), 3);
            level.setBlock(loader.north(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
        }
        if (t == 100) {
            if (count(cartBox) != 20) fails.add("powered loader moved items");
            level.setBlock(loader.north(), Blocks.AIR.defaultBlockState(), 3);
        }
        if (t == 160) {
            if (count(box) != 20 || count(cartBox) != 0) fails.add("unload: cart " + count(cartBox) + " chest " + count(box));
            cart.discard();
            String line = fails.isEmpty() ? "Loader self test PASS" : "Loader self test FAIL: " + String.join("; ", fails);
            LOG.info(line);
            source.sendSuccess(() -> Component.literal(line), false);
            for (int cx = -1; cx <= 1; cx++) for (int cz = -1; cz <= 0; cz++) level.setChunkForced(cx, cz, false);
            t = -1;
        }
    }
}
