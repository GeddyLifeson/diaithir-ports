package net.diaithir.crafting.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

/** Operator Seat: food only, one kind, rollback, work is all or nothing, lamp follows food, ranged lookup, eating refills the reserve. */
final class CraftingSelfTest {
    private CraftingSelfTest() {}

    private static int insert(ResourceHandler<ItemResource> h, ItemResource r, int n, boolean commit) {
        try (Transaction tx = Transaction.openRoot()) {
            int in = h.insert(r, n, tx);
            if (commit) tx.commit();
            return in;
        }
    }

    private static void check(List<String> fails, boolean ok, String what) {
        if (!ok) fails.add(what);
    }

    static List<String> run(MinecraftServer server) {
        List<String> fails = new ArrayList<>();
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) return List.of("no overworld");
        BlockPos at = new BlockPos(level.getRespawnData().pos().getX(), 240, level.getRespawnData().pos().getZ());
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-12, -2, -12), at.offset(12, 2, 12))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
        level.setBlock(at, DiaithirCrafting.OPERATOR_SEAT.get().defaultBlockState(), 3);
        if (!(level.getBlockEntity(at) instanceof OperatorSeatBlockEntity seat)) return List.of("no seat block entity");
        ResourceHandler<ItemResource> h = seat.food.view();
        ItemResource bread = ItemResource.of(Items.BREAD), apple = ItemResource.of(Items.APPLE), stone = ItemResource.of(Items.STONE);
        double perNut = Config.SECONDS_PER_NUTRITION.get();

        check(fails, !level.getBlockState(at).getValue(OperatorSeatBlock.STAFFED), "empty seat is lit");
        check(fails, !Operators.work(level, at, 1), "empty seat paid work");
        check(fails, insert(h, stone, 8, true) == 0, "seat took a non-food item");
        check(fails, insert(h, bread, 4, false) == 4 && seat.food.count() == 0, "rollback did not undo the insert");
        check(fails, insert(h, bread, 4, true) == 4 && seat.food.count() == 4, "bread not stored");
        check(fails, insert(h, apple, 2, true) == 0, "seat mixed two kinds of food");
        check(fails, level.getBlockState(at).getValue(OperatorSeatBlock.STAFFED), "fed seat is not lit");

        double bread5 = 5 * perNut;
        check(fails, Operators.work(level, at, bread5 * 0.5), "half a bread of work refused");
        check(fails, seat.food.count() == 3 && Math.abs(seat.reserveSeconds() - bread5 * 0.5) < 1.0E-6, "eating or reserve wrong after half a bread");
        double all = seat.reserveSeconds() + 3 * bread5;
        check(fails, !Operators.work(level, at, all + 1), "work beyond all food was paid");
        check(fails, seat.food.count() == 3, "a refused job still ate food");
        check(fails, Operators.work(level, at, all), "exact remaining work refused");
        check(fails, seat.food.count() == 0 && seat.reserveSeconds() < 1.0E-6, "seat not drained to empty");
        check(fails, !level.getBlockState(at).getValue(OperatorSeatBlock.STAFFED), "drained seat still lit");

        insert(h, bread, 1, true);
        BlockPos far = at.offset(Config.RANGE.get() + 3, 0, 0);
        check(fails, !Operators.staffed(level, far), "seat served a machine out of range");
        check(fails, Operators.staffed(level, at.offset(Config.RANGE.get(), 0, 0)), "seat did not serve a machine in range");

        MachineStats.Stat asm = MachineStats.of("tech.assembler");
        check(fails, asm != null && asm.watts() == 75000.0 && asm.seconds() == 2.0, "assembler stats are not 75 kW / 2 s");
        check(fails, MachineStats.of("tech.reclaimer") != null && MachineStats.of("tech.flint") == null, "stat table has the wrong rows");
        return fails;
    }
}
