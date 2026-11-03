package net.diaithir.stations;

import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
import net.diaithir.crafting.foundry.DiaithirCrafting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Recipe codec (good and bad), ingredient matching by item and tag, load and start, timing, operator gate, empty station. */
final class StationsSelfTest {
    private StationsSelfTest() {}

    private static StationRecipe parse(ServerLevel level, String json) {
        return StationRecipe.CODEC.parse(level.registryAccess().createSerializationContext(JsonOps.INSTANCE), JsonParser.parseString(json)).result().orElse(null);
    }

    private static void check(List<String> fails, boolean ok, String what) {
        if (!ok) fails.add(what);
    }

    static List<String> run(MinecraftServer server) {
        List<String> fails = new ArrayList<>();
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) return List.of("no overworld");
        BlockPos at = new BlockPos(level.getRespawnData().pos().getX(), 240, level.getRespawnData().pos().getZ());
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-4, -2, -4), at.offset(4, 3, 4))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);

        StationRecipe nail = parse(level, "{\"station\":\"station.wright_bench\",\"stage\":\"part\",\"time_s\":1,"
                + "\"ingredients\":[{\"item\":\"minecraft:iron_ingot\",\"count\":2},{\"tag\":\"minecraft:planks\"}],\"result\":{\"id\":\"minecraft:iron_nugget\",\"count\":9}}");
        check(fails, nail != null, "good recipe did not parse");
        check(fails, parse(level, "{\"station\":\"s\",\"stage\":\"magic\",\"ingredients\":[{\"item\":\"minecraft:stick\"}],\"result\":{\"id\":\"minecraft:stick\"}}") == null, "bad stage parsed");
        check(fails, parse(level, "{\"station\":\"s\",\"stage\":\"part\",\"ingredients\":[{\"item\":\"minecraft:stick\",\"tag\":\"minecraft:planks\"}],\"result\":{\"id\":\"minecraft:stick\"}}") == null, "item plus tag parsed");
        if (nail == null) return fails;

        StationRecipe gated = new StationRecipe("station.wright_bench", "assembly", nail.ingredients(), new ItemStack(Items.DIAMOND), 1, true);
        StationRecipes.setForTest(Map.of(Identifier.parse("test:nail"), nail, Identifier.parse("test:gated"), gated));
        check(fails, StationRecipes.forStation("station.wright_bench").size() == 2 && StationRecipes.forStation("station.smeltery").isEmpty(), "station lookup wrong");

        level.setBlock(at, DiaithirStations.BLOCK_LIST.get(1).get().defaultBlockState(), 3);
        if (!(level.getBlockEntity(at) instanceof StationBlockEntity be)) return List.of("no station block entity");
        check(fails, be.start() == null, "empty station started a job");
        be.load(new ItemStack(Items.IRON_INGOT, 1));
        check(fails, be.start() == null, "half the ingredients started a job");
        be.load(new ItemStack(Items.IRON_INGOT, 1));
        be.load(new ItemStack(Items.OAK_PLANKS, 1));
        check(fails, StationRecipes.forStation("station.wright_bench").stream().anyMatch(r -> r.satisfiedBy(be.inv)), "ingredients not satisfied after loading");
        // with no seat the operator recipe is skipped, so the plain recipe must run
        check(fails, be.start() != null && be.busy(), "no job started with full ingredients");
        check(fails, be.start() == null, "a busy station started a second job");
        int guard = 0;
        while (be.busy() && guard++ < 100) be.tick();
        check(fails, !be.busy() && be.lastOutput.is(Items.IRON_NUGGET) && be.lastOutput.getCount() == 9, "job did not finish with 9 nuggets in 100 ticks");
        check(fails, be.inv.stream().allMatch(ItemStack::isEmpty), "ingredients not consumed");

        // operator gate: with no fed seat the gated recipe alone must not start
        StationRecipes.setForTest(Map.of(Identifier.parse("test:gated"), gated));
        be.load(new ItemStack(Items.IRON_INGOT, 2));
        be.load(new ItemStack(Items.OAK_PLANKS, 1));
        check(fails, be.start() == null, "operator recipe ran without an operator");
        level.setBlock(at.east(), DiaithirCrafting.OPERATOR_SEAT.get().defaultBlockState(), 3);
        if (level.getBlockEntity(at.east()) instanceof net.diaithir.crafting.foundry.OperatorSeatBlockEntity seat) {
            try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                seat.food.view().insert(net.neoforged.neoforge.transfer.item.ItemResource.of(Items.BREAD), 1, tx);
                tx.commit();
            }
            check(fails, be.start() != null && be.busy(), "operator recipe did not run with a fed seat");
        } else {
            fails.add("no seat block entity");
        }
        StationRecipes.setForTest(Map.of());
        return fails;
    }
}
