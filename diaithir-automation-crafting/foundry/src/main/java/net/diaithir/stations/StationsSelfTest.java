package net.diaithir.stations;

import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
import net.diaithir.crafting.foundry.DiaithirCrafting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
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

        // the jar's own recipes: packs 2 to 7 at the research bench, the Operator Seat at the assembler (loaded before any test hook replaces them)
        check(fails, StationRecipes.forStation("research.bench").size() == 6, "research bench has " + StationRecipes.forStation("research.bench").size() + " recipes, expected 6");
        check(fails, StationRecipes.forStation("machine.assembler").stream().anyMatch(r -> r.result().item().value() == DiaithirCrafting.OPERATOR_SEAT_ITEM.get()), "operator seat is not an assembler recipe");

        StationRecipe nail = parse(level, "{\"station\":\"station.wright_bench\",\"stage\":\"part\",\"time_s\":1,"
                + "\"ingredients\":[{\"item\":\"minecraft:iron_ingot\",\"count\":2},{\"tag\":\"minecraft:planks\"}],\"result\":{\"id\":\"minecraft:iron_nugget\",\"count\":9}}");
        check(fails, nail != null, "good recipe did not parse");
        check(fails, parse(level, "{\"station\":\"s\",\"stage\":\"magic\",\"ingredients\":[{\"item\":\"minecraft:stick\"}],\"result\":{\"id\":\"minecraft:stick\"}}") == null, "bad stage parsed");
        check(fails, parse(level, "{\"station\":\"s\",\"stage\":\"part\",\"ingredients\":[{\"item\":\"minecraft:stick\",\"tag\":\"minecraft:planks\"}],\"result\":{\"id\":\"minecraft:stick\"}}") == null, "item plus tag parsed");
        if (nail == null) return fails;

        StationRecipe gated = new StationRecipe("station.wright_bench", "assembly", nail.ingredients(), new ItemStackTemplate(Items.DIAMOND), 1, true, "none", 0, 0);
        StationRecipes.setForTest(Map.of(Identifier.parse("test:nail"), nail, Identifier.parse("test:gated"), gated));
        check(fails, StationRecipes.forStation("station.wright_bench").size() == 2 && StationRecipes.forStation("station.smeltery").isEmpty(), "station lookup wrong");

        level.setBlock(at, DiaithirStations.BLOCK_BY_NAME.get("foundry_wright_bench").get().defaultBlockState(), 3);
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
        // stations and support blocks registered
        check(fails, List.of("machine.assembler", "altar.basic", "station.kitchen", "machine.reclaimer", "research.bench").stream().allMatch(DiaithirStations.STATIONS::containsValue)
                && DiaithirStations.BLOCK_LIST.size() == 10, "a station is missing");

        // smeltery pour: a recipe with basins=2 needs two touching Cast Basins
        StationRecipe pour = new StationRecipe("station.smeltery", "part", List.of(new StationRecipe.Ing(java.util.Optional.of(Identifier.parse("minecraft:iron_ingot")), java.util.Optional.empty(), 1)),
                new ItemStackTemplate(Items.IRON_NUGGET, 9), 1, false, "none", 0, 2);
        StationRecipes.setForTest(Map.of(Identifier.parse("test:pour"), pour));
        BlockPos sm = at.above(2);
        level.setBlock(sm, DiaithirStations.BLOCK_BY_NAME.get("foundry_smeltery").get().defaultBlockState(), 3);
        StationBlockEntity smBe = (StationBlockEntity) level.getBlockEntity(sm);
        smBe.load(new ItemStack(Items.IRON_INGOT, 1));
        level.setBlock(sm.north(), DiaithirStations.CAST_BASIN.get().defaultBlockState(), 3);
        check(fails, smBe.start() == null, "pour ran with one basin of two");
        level.setBlock(sm.south(), DiaithirStations.CAST_BASIN.get().defaultBlockState(), 3);
        check(fails, smBe.start() != null, "pour did not run with two basins");

        // steam power: a 40 kW, 2 s job costs 80 kJ from a boiler; coal burns 80 s at 50 kW = 4 MJ per item
        StationRecipe forge = new StationRecipe("station.steam_forge", "part", pour.ingredients(), new ItemStackTemplate(Items.IRON_NUGGET, 9), 2, false, "steam", 40000, 0);
        StationRecipes.setForTest(Map.of(Identifier.parse("test:forge"), forge));
        BlockPos sf = at.above(2).east(3);
        level.setBlock(sf, DiaithirStations.BLOCK_BY_NAME.get("foundry_steam_forge").get().defaultBlockState(), 3);
        StationBlockEntity sfBe = (StationBlockEntity) level.getBlockEntity(sf);
        sfBe.load(new ItemStack(Items.IRON_INGOT, 2));
        check(fails, sfBe.start() == null, "steam job ran with no boiler");
        BlockPos bp = sf.east();
        level.setBlock(bp, DiaithirStations.BOILER.get().defaultBlockState(), 3);
        BoilerBlockEntity boiler = (BoilerBlockEntity) level.getBlockEntity(bp);
        check(fails, !level.getBlockState(bp).getValue(BoilerBlock.LIT), "empty boiler is lit");
        check(fails, boiler.feed(new ItemStack(Items.STONE, 4)) == 0, "boiler took a non-fuel item");
        check(fails, boiler.feed(new ItemStack(Items.COAL, 2)) == 2, "boiler did not take coal");
        check(fails, level.getBlockState(bp).getValue(BoilerBlock.LIT), "fuelled boiler is not lit");
        check(fails, Math.abs(Boilers.available(level, sf) - 2 * 4_000_000.0) < 1, "boiler potential is not 8 MJ for two coal: " + Boilers.available(level, sf));
        check(fails, sfBe.start() != null, "steam job did not run with a fuelled boiler");
        check(fails, Math.abs(Boilers.available(level, sf) - (8_000_000.0 - 80_000.0)) < 1, "steam job did not cost 80 kJ");
        check(fails, !Boilers.draw(level, sf, 9_000_000.0) && Math.abs(Boilers.available(level, sf) - 7_920_000.0) < 1, "an unaffordable draw took energy");

        StationRecipes.setForTest(Map.of());
        return fails;
    }
}
