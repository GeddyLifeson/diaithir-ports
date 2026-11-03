package net.diaithir.yards;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

/** Run with -Ddiaithir.yardtest=true: builds Rocket in a workshop, checks crane need, tier lock, stats and the rollout, then stops the server. */
public final class YardSelfTest {
    private static boolean done;
    private static int warm;

    private YardSelfTest() {}

    public static void tick(MinecraftServer server) {
        if (done || !Boolean.getBoolean("diaithir.yardtest") || warm++ < 20) return;
        done = true;
        List<String> f = new ArrayList<>();
        try {
            run(server, f);
        } catch (Throwable t) {
            f.add("exception " + t);
        }
        LogUtils.getLogger().info(f.isEmpty() ? "Yards self test PASS" : "Yards self test FAIL: " + String.join("; ", f));
        server.halt(false);
    }

    /** Every ingredient and result of the bench recipes must be a real item. */
    private static void recipes(List<String> f) throws java.io.IOException {
        var ids = new java.util.ArrayList<String>();
        for (YardData.Named p : YardData.PARTS) ids.add(p.id());
        for (YardData.Controller c : YardData.CONTROLLERS) ids.add(c.id());
        for (YardData.Crane c : YardData.CRANES) ids.add(c.id());
        com.google.gson.Gson g = new com.google.gson.Gson();
        int n = 0;
        for (String id : ids) {
            try (var in = YardSelfTest.class.getResourceAsStream("/data/diaithircarts/diaithir_stations/station_recipe/" + id + ".json")) {
                if (in == null) { f.add("recipe missing " + id); continue; }
                com.google.gson.JsonObject o = g.fromJson(new java.io.InputStreamReader(in), com.google.gson.JsonObject.class);
                n++;
                for (var e : o.getAsJsonArray("ingredients")) {
                    String item = e.getAsJsonObject().get("item").getAsString();
                    if (!BuiltInRegistries.ITEM.containsKey(Identifier.parse(item))) f.add(id + " ingredient " + item + " is not an item");
                }
                String res = o.getAsJsonObject("result").get("id").getAsString();
                if (!BuiltInRegistries.ITEM.containsKey(Identifier.parse(res))) f.add(id + " result " + res + " is not an item");
            }
        }
        if (n != ids.size()) f.add("recipes " + n + "/" + ids.size());
    }

    private static void run(MinecraftServer server, List<String> f) throws java.io.IOException {
        recipes(f);
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        BlockPos at = new BlockPos(level.getRespawnData().pos().getX(), 230, level.getRespawnData().pos().getZ());
        level.getChunk(at);
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-9, -1, -9), at.offset(9, 4, 9))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
        level.setBlock(at, BuiltInRegistries.BLOCK.getValue(Identifier.parse("diaithircarts:rail_workshop")).defaultBlockState(), 2);
        YardBlockEntity be = (YardBlockEntity) level.getBlockEntity(at);
        if (be == null) { f.add("no block entity"); return; }
        if (be.select("yard_loco_gp7") == null) f.add("workshop accepted a diesel (assembly line) design");
        if (be.select("yard_loco_rocket") != null) { f.add("rocket not accepted"); return; }
        PartDef bogie = YardRegistry.part("diaithircarts:bogie_xs").orElseThrow();
        PartDef boiler = YardRegistry.part("diaithircarts:boiler_xs").orElseThrow();
        PartDef cab = YardRegistry.part("diaithircarts:cab_xs").orElseThrow();
        PartDef coupler = YardRegistry.part("diaithircarts:coupler_xs").orElseThrow();
        for (int i = 0; i < 2; i++) if (be.install(bogie) != BuildState.InstallResult.OK) f.add("bogie " + i);
        for (int i = 0; i < 200; i++) be.tick();
        if (be.build().stageIndex() != 1) f.add("frame stage did not finish, at " + be.build().stageIndex());
        if (be.install(cab) != BuildState.InstallResult.WRONG_STAGE) f.add("cab accepted too early");
        if (be.install(boiler) != BuildState.InstallResult.NEEDS_CRANE) f.add("heavy boiler placed without a crane");
        level.setBlock(at.offset(3, 0, 0), BuiltInRegistries.BLOCK.getValue(Identifier.parse("diaithircarts:crane_treadwheel")).defaultBlockState(), 2);
        if (be.cranes()[0] < 2000) f.add("crane not seen: lift " + be.cranes()[0]);
        if (be.install(boiler) != BuildState.InstallResult.OK) f.add("boiler with crane");
        for (int i = 0; i < 300; i++) be.tick();
        if (be.install(cab) != BuildState.InstallResult.OK) f.add("cab");
        for (int i = 0; i < 100; i++) be.tick();
        if (be.install(coupler) != BuildState.InstallResult.OK) f.add("coupler");
        for (int i = 0; i < 100; i++) be.tick();
        if (be.active()) f.add("still building");
        if (be.lastRollout.isEmpty() || !BuiltInRegistries.ITEM.getKey(be.lastRollout.getItem()).toString().equals("diaithircarts:loco_rocket")) f.add("no rollout");
    }
}
