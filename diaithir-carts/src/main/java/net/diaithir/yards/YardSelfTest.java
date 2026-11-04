package net.diaithir.yards;

import net.diaithir.carts.CartLogic;
import net.diaithir.carts.DiaithirCarts;
import net.diaithir.carts.Gauge;
import net.diaithir.carts.GaugeData;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
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
        level.setBlock(at.offset(3, 0, 0), BuiltInRegistries.BLOCK.getValue(Identifier.parse("diaithircarts:crane_treadwheel")).defaultBlockState(), 2);
        if (be.cranes()[0] < 2000) f.add("crane not seen: lift " + be.cranes()[0]);
        YardDef def = YardRegistry.yard("yard_loco_rocket").orElseThrow();
        PartDef early = YardRegistry.part("diaithircarts:" + def.stages().get(1).slots().get(0).kind()).orElseThrow();
        if (be.install(early) != BuildState.InstallResult.WRONG_STAGE) f.add("a later stage's part was accepted too early");
        for (YardDef.Stage st : def.stages()) {   // every stage of the spec-driven chain, each part its own kind
            for (YardDef.Slot sl : st.slots()) for (int i = 0; i < sl.count(); i++) {
                PartDef pd = YardRegistry.part("diaithircarts:" + sl.kind()).orElseThrow();
                if (be.install(pd) != BuildState.InstallResult.OK) f.add(st.id() + " part " + sl.kind() + " " + i);
            }
            for (int i = 0; i < 400; i++) be.tick();
        }
        if (be.active()) f.add("still building");
        if (be.lastRollout.isEmpty() || !BuiltInRegistries.ITEM.getKey(be.lastRollout.getItem()).toString().equals("diaithircarts:loco_rocket")) f.add("no rollout");
        // rolling stock becomes the matching existing cart entity
        BlockPos rail = at.offset(6, 0, -6);
        level.setBlock(rail, Blocks.RAIL.defaultBlockState(), 2);
        cart(level, rail, "wag_boxcar", net.minecraft.world.entity.EntityType.CHEST_MINECART, f);
        cart(level, rail, "wag_tank", net.minecraft.world.entity.EntityType.HOPPER_MINECART, f);
        cart(level, rail, "loco_gp7", net.minecraft.world.entity.EntityType.FURNACE_MINECART, f);
        cart(level, rail, "pas_dining", net.minecraft.world.entity.EntityType.MINECART, f);
        gauge(level, rail.offset(6, 0, 0), f);
        cargo(level, rail.offset(12, 0, 0), f);
        // blueprint build by a manned crane from a depot chest, no hand installs
        level.setBlock(at.offset(0, 0, 1), Blocks.CHEST.defaultBlockState(), 2);
        net.minecraft.world.Container depot = (net.minecraft.world.Container) level.getBlockEntity(at.offset(0, 0, 1));
        depot.setItem(0, new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("diaithircarts:axle_wagon_light_xs")), 4));
        depot.setItem(1, new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("diaithircarts:body_general_xs")), 1));
        if (be.select("yard_wag_wagonway") != null) { f.add("wagonway not selectable"); return; }
        for (int i = 0; i < 400; i++) be.tick();
        if (!be.active() || be.build().stageIndex() != 0) f.add("built with nobody at the crane");
        var villager = net.minecraft.world.entity.EntityType.VILLAGER.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        villager.setPos(at.getX() + 2.5, at.getY(), at.getZ() + 2.5);
        level.addFreshEntity(villager);
        for (int i = 0; i < 2000 && be.active(); i++) be.tick();
        if (be.active() || !BuiltInRegistries.ITEM.getKey(be.lastRollout.getItem()).toString().equals("diaithircarts:wag_wagonway")) f.add("operator did not finish the blueprint build");
        villager.discard();
    }

    private static void cart(ServerLevel level, BlockPos rail, String id, net.minecraft.world.entity.EntityType<?> want, List<String> f) {
        var c = RollingStockItem.spawn(level, rail, new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("diaithircarts:" + id))));
        if (c == null || c.getType() != want) f.add(id + " spawned " + (c == null ? "nothing" : c.getType().toString()));
        else if (c.getCustomName() == null) f.add(id + " has no name");
        if (c != null) c.discard();
    }

    /** Standard and narrow vehicles on standard, narrow and dual rail: right gauge runs, wrong gauge is capped at 0, dual takes both, narrow takes curves better. */
    private static void gauge(ServerLevel level, BlockPos p, List<String> f) {
        var std = DiaithirCarts.STEEL_RAIL.get().defaultBlockState();
        var nar = DiaithirCarts.NARROW_RAIL.get().defaultBlockState();
        var dual = DiaithirCarts.DUAL_RAIL.get().defaultBlockState();
        level.setBlock(p, std, 2); level.setBlock(p.offset(2, 0, 0), nar, 2); level.setBlock(p.offset(4, 0, 0), dual, 2);
        var stdCart = spawnAt(level, p, "loco_gp7"); var narCart = spawnAt(level, p.offset(2, 0, 0), "nwag_boxcar");
        if (stdCart == null || narCart == null) { f.add("gauge test carts did not spawn"); return; }
        if (GaugeData.of(stdCart) != Gauge.STANDARD || GaugeData.of(narCart) != Gauge.NARROW) f.add("vehicle gauge not stored");
        if (CartLogic.cap(stdCart, 0.4) < 0.89) f.add("standard vehicle slow on standard rail");
        if (CartLogic.cap(narCart, 0.4) < 0.49) f.add("narrow vehicle slow on narrow rail");
        stdCart.setPos(p.getX() + 2.5, p.getY() + 0.0625, p.getZ() + 0.5);
        if (CartLogic.cap(stdCart, 0.4) != 0) f.add("standard vehicle ran on narrow rail");
        narCart.setPos(p.getX() + 0.5, p.getY() + 0.0625, p.getZ() + 0.5);
        if (CartLogic.cap(narCart, 0.4) != 0) f.add("narrow vehicle ran on standard rail");
        stdCart.setPos(p.getX() + 4.5, p.getY() + 0.0625, p.getZ() + 0.5); narCart.setPos(p.getX() + 4.5, p.getY() + 0.0625, p.getZ() + 0.5);
        if (CartLogic.cap(stdCart, 0.4) == 0 || CartLogic.cap(narCart, 0.4) == 0) f.add("dual-gauge rail refused a vehicle");
        if (CartLogic.slotLimit(narCart) != 10) f.add("narrow freight cart takes " + CartLogic.slotLimit(narCart) + " slots, want 10");
        if (!((net.minecraft.world.Container) narCart).canPlaceItem(9, new ItemStack(net.minecraft.world.item.Items.COAL)) || ((net.minecraft.world.Container) narCart).canPlaceItem(10, new ItemStack(net.minecraft.world.item.Items.COAL))) f.add("payload limit not applied to narrow cart slots");
        var steam = spawnAt(level, p, "loco_mallard");
        if (steam == null || net.diaithir.carts.Autonomy.crewRequired(steam) != 2 || net.diaithir.carts.Autonomy.crewRequired(stdCart) != 1) f.add("crew counts not stored");
        if (steam != null) steam.discard();
        var rocket = spawnAt(level, p, "loco_rocket");
        if (rocket instanceof net.minecraft.world.entity.vehicle.minecart.MinecartFurnace fc) {
            if (net.diaithir.carts.MagicFuel.tick(rocket)) f.add("unfitted locomotive lit by magic");
            rocket.setData(net.diaithir.carts.MagicFuel.FITTED, true);
            net.diaithir.carts.MagicFuel.charge(rocket, 10_000_000L);
            boolean fired = net.diaithir.carts.MagicFuel.tick(rocket);
            long left = rocket.getData(GaugeData.JOULES);
            if (!fired || left != 10_000_000L - net.diaithir.carts.MagicFuel.fillCost(rocket) || left < 5_000_000L) f.add("magic fill failed: fired " + fired + " bank " + left);
            rocket.discard();
        } else f.add("loco_rocket is not a furnace cart");
        stdCart.discard(); narCart.discard();
    }

    /** Rail wagons as cargo carriers: bays by wagon type and gauge, loading, a container that keeps its own cargo, saving, aiming, and the client's view. */
    private static void cargo(ServerLevel level, BlockPos p, List<String> f) {
        level.setBlock(p, Blocks.RAIL.defaultBlockState(), 2);
        var flat = spawnAt(level, p, "wag_flatcar"); var box = spawnAt(level, p, "wag_boxcar"); var nflat = spawnAt(level, p, "nwag_flatcar");
        if (flat == null || box == null || nflat == null) { f.add("cargo test carts did not spawn"); return; }
        var cf = flat.getCapability(net.diaithir.freight.Freight.CARRIER, null);
        var cb = box.getCapability(net.diaithir.freight.Freight.CARRIER, null);
        var cn = nflat.getCapability(net.diaithir.freight.Freight.CARRIER, null);
        if (cf == null || cb == null || cn == null) { f.add("wagon is not a cargo carrier"); return; }
        if (cf.bay().w != 4 || cf.bay().l != 7 || cb.bay().w != 2 || cn.bay().w != 2) f.add("bay sizes: flat " + cf.bay().w + "x" + cf.bay().l + ", box " + cb.bay().w + ", narrow flat " + cn.bay().w);
        if (spawnAt(level, p, "pas_dining").getCapability(net.diaithir.freight.Freight.CARRIER, null) != null) f.add("a passenger car carries freight");
        // a pallet with iron inside, and a container holding a pallet
        net.minecraft.core.NonNullList<ItemStack> iron = net.minecraft.core.NonNullList.withSize(18, ItemStack.EMPTY);
        iron.set(0, new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 64));
        ItemStack pallet = new ItemStack(net.diaithir.freight.Freight.LOAD_ITEMS.get("pallet").get());
        pallet.set(net.minecraft.core.component.DataComponents.CONTAINER, net.minecraft.world.item.component.ItemContainerContents.fromItems(iron));
        ItemStack box20 = new ItemStack(net.diaithir.freight.Freight.CONTAINER_ITEMS.get("container_20").get());
        box20.set(net.diaithir.freight.Freight.BAY.get(), new net.diaithir.freight.BayData(List.of(new net.diaithir.freight.BayData.Entry("pallet", 0, 0, 0, false, net.minecraft.world.item.component.ItemContainerContents.fromItems(iron)))));
        var r = net.diaithir.freight.Carriers.put(cf, box20.copy(), 1, 3);
        if (r != net.diaithir.freight.CargoBay.Result.OK) f.add("container refused by a flat wagon: " + r);
        if (net.diaithir.freight.Carriers.put(cb, box20.copy(), 0, 0) == net.diaithir.freight.CargoBay.Result.OK) f.add("a box wagon took a container");
        if (net.diaithir.freight.Carriers.put(cn, box20.copy(), 0, 0) == net.diaithir.freight.CargoBay.Result.OK) f.add("a narrow flat wagon took a container");
        if (net.diaithir.freight.Carriers.put(cb, pallet.copy(), 0, 0) != net.diaithir.freight.CargoBay.Result.OK) f.add("box wagon refused a pallet");
        if (net.diaithir.freight.Carriers.put(cn, pallet.copy(), 0, 0) != net.diaithir.freight.CargoBay.Result.OK) f.add("narrow wagon refused a pallet");
        // saved loads come back, container interior included
        var tree = flat.getData(net.diaithir.carts.RailCargo.LOADS);
        var enc = net.diaithir.freight.BayTree.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, tree).result();
        var back = enc.flatMap(n -> net.diaithir.freight.BayTree.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, n).result());
        if (back.isEmpty()) f.add("loads did not survive a save");
        else {
            var bay = back.get().toBay(4, 4, 7, 70_000);
            var placed = bay.loads();
            if (placed.size() != 1 || placed.get(0).inside == null || placed.get(0).inside.loads().size() != 1) f.add("container interior lost on save");
            else if (!(placed.get(0).inside.loads().get(0).data instanceof List<?> l) || ((ItemStack) l.get(0)).getCount() != 64) f.add("pallet contents lost on save");
        }
        if (!flat.getData(net.diaithir.carts.RailCargo.VIEW).contains("container_20,0,0,0")) f.add("client view is " + flat.getData(net.diaithir.carts.RailCargo.VIEW));
        // aiming at the middle of the wagon picks the middle column
        int[] col = cf.columnAt(flat.position());
        if (col == null || col[0] != 2 || col[1] != 3) f.add("middle column is " + (col == null ? "off the deck" : col[0] + "," + col[1]));
        if (cf.columnAt(flat.position().add(3, 0, 3)) != null) f.add("a point far from the wagon hit its deck");
        // the container comes off with its cargo intact
        ItemStack out = net.diaithir.freight.Carriers.takeTop(cf, 1, 3);
        var kept = out.get(net.diaithir.freight.Freight.BAY.get());
        if (out.isEmpty() || kept == null || kept.loads().size() != 1) f.add("container came off without its cargo");
        if (!cf.bay().loads().isEmpty()) f.add("wagon still holds the container");
        flat.discard(); box.discard(); nflat.discard();
    }

    private static net.minecraft.world.entity.vehicle.minecart.AbstractMinecart spawnAt(ServerLevel level, BlockPos p, String id) {
        return RollingStockItem.spawn(level, p, new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("diaithircarts:" + id))));
    }
}
