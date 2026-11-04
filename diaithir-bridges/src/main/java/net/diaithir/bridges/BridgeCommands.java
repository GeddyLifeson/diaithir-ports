package net.diaithir.bridges;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import net.diaithir.units.fuel.Fuels;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** /diaithirbridges selftest (owners only): converters, reservoir and the cell guard, through the same capabilities other mods use. */
final class BridgeCommands {
    private BridgeCommands() {}

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("diaithirbridges")
                .then(Commands.literal("selftest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    List<String> fails = selftest(c.getSource().getServer());
                    String line = fails.isEmpty() ? "Bridges self test PASS" : "Bridges self test FAIL: " + String.join("; ", fails);
                    c.getSource().sendSuccess(() -> Component.literal(line), false);
                    return fails.isEmpty() ? 1 : 0;
                })));
    }

    private static int insert(net.neoforged.neoforge.transfer.energy.EnergyHandler h, int n) {
        try (Transaction tx = Transaction.openRoot()) {
            int r = h.insert(n, tx);
            tx.commit();
            return r;
        }
    }

    private static int extract(net.neoforged.neoforge.transfer.energy.EnergyHandler h, int n) {
        try (Transaction tx = Transaction.openRoot()) {
            int r = h.extract(n, tx);
            tx.commit();
            return r;
        }
    }

    static List<String> selftest(MinecraftServer server) {
        List<String> f = new ArrayList<>();
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) return List.of("no overworld");
        BlockPos at = new BlockPos(level.getRespawnData().pos().getX(), 240, level.getRespawnData().pos().getZ());
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-3, -1, -3), at.offset(3, 3, 3))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
        try {
            // ---- EU converter: front north, back south
            BlockPos eu = at;
            level.setBlock(eu, DiaithirBridges.EU_CONVERTER.get().defaultBlockState(), 3);
            var front = level.getCapability(Capabilities.Energy.BLOCK, eu, Direction.NORTH);
            var back = level.getCapability(Capabilities.Energy.BLOCK, eu, Direction.SOUTH);
            if (front == null || back == null) return List.of("EU converter has no energy capability");
            int got = insert(back, 1000);
            if (got != 1000) f.add("EU in accepted " + got);
            if (!(level.getBlockEntity(eu) instanceof ConverterEntity be) || be.stored() != Config.get(Config.EU_TO_FE) * 10) f.add("EU to FE stored wrong");
            if (insert(front, 100) != 0) f.add("front face accepted input");
            if (extract(back, 100) != 0) f.add("back face gave output");
            int out = extract(front, 100000);
            if (out != Config.get(Config.EU_TO_FE) * 10) f.add("EU to FE out " + out);
            // flip to FE to EU
            BlockState st = level.getBlockState(eu).setValue(ConverterBlock.REVERSE, true);
            level.setBlock(eu, st, 3);
            level.invalidateCapabilities(eu);
            back = level.getCapability(Capabilities.Energy.BLOCK, eu, Direction.SOUTH);
            front = level.getCapability(Capabilities.Energy.BLOCK, eu, Direction.NORTH);
            insert(back, 1000);
            out = extract(front, 100000);
            if (out != Config.get(Config.FE_TO_EU) * 10) f.add("FE to EU out " + out);

            // ---- air converter: FE side only (PneumaticCraft is a soft dependency)
            BlockPos air = at.east(2);
            level.setBlock(air, DiaithirBridges.AIR_CONVERTER.get().defaultBlockState(), 3);
            if (level.getCapability(Capabilities.Energy.BLOCK, air, Direction.NORTH) != null) f.add("air converter FE_TO_AIR front should have no FE");
            var airIn = level.getCapability(Capabilities.Energy.BLOCK, air, Direction.SOUTH);
            if (airIn == null || insert(airIn, 1000) != 1000) f.add("air converter takes no FE");
            else if (!(level.getBlockEntity(air) instanceof ConverterEntity ae) || ae.stored() != Config.get(Config.FE_TO_AIR) * 10) f.add("FE to air stored wrong");
            else {
                ae.serverTick(); // no neighbour air handler: must not throw, buffer stays
                if (ae.stored() != Config.get(Config.FE_TO_AIR) * 10) f.add("air side moved without a handler");
            }

            // ---- reservoir
            BlockPos res = at.west(2);
            level.setBlock(res, DiaithirBridges.RESERVOIR.get().defaultBlockState(), 3);
            var tank = level.getCapability(Capabilities.Fluid.BLOCK, res, Direction.UP);
            if (tank == null) f.add("reservoir has no fluid capability");
            else {
                try (Transaction tx = Transaction.openRoot()) {
                    if (tank.insert(FluidResource.of(Fluids.WATER), 1000, tx) != 0) f.add("reservoir took water");
                    if (tank.insert(FluidResource.of(Fluids.LAVA), 1000, tx) != 1000) f.add("reservoir refused lava");
                    tx.commit();
                }
                FakePlayer p = FakePlayerFactory.get(level, new GameProfile(UUID.nameUUIDFromBytes("br-test".getBytes()), "br_test"));
                p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
                boolean filled = level.getBlockEntity(res) instanceof ReservoirEntity r && FluidUtil.interactWithFluidHandler(p, InteractionHand.MAIN_HAND, res, r.tank(), null);
                if (!filled || !p.getMainHandItem().is(Items.LAVA_BUCKET)) f.add("bucket did not fill from the reservoir: " + p.getMainHandItem());
                else {
                    boolean emptied = FluidUtil.interactWithFluidHandler(p, InteractionHand.MAIN_HAND, res, ((ReservoirEntity) level.getBlockEntity(res)).tank(), null);
                    if (!emptied || !p.getMainHandItem().is(Items.BUCKET)) f.add("bucket did not empty into the reservoir: " + p.getMainHandItem());
                }
            }

            // ---- carrier tank (fuel bridge): FuelStore, engine feed, one fuel at a time, comparator, fluid port
            BlockPos ct = at.south(2);
            level.setBlock(ct, DiaithirBridges.CARRIER_TANK.get().defaultBlockState(), 3);
            var fs = level.getCapability(Ports.FUEL, ct, Direction.UP);
            if (fs == null) f.add("carrier tank has no fuel store capability");
            else {
                var diesel = Fuels.get("diesel");
                var petrol = Fuels.get("petrol");
                var mana = Fuels.get("mana");
                if (fs.insert(diesel, 1000) != 1000) f.add("tank refused diesel");
                if (fs.insert(petrol, 10) != 0) f.add("tank mixed petrol into diesel");
                if (fs.insert(diesel, 1e9) != CarrierTankEntity.capacityNow() - 1000) f.add("tank capacity wrong");
                fs.extract(1e9);
                fs.insert(diesel, 1000);
                var tankBe = (CarrierTankEntity) level.getBlockEntity(ct);
                if (tankBe.canFeed(Fuels.engine("petrol_engine"))) f.add("petrol engine accepted diesel");
                if (!tankBe.canFeed(Fuels.engine("diesel_engine"))) f.add("diesel engine refused diesel");
                double shaft = tankBe.feed(Fuels.engine("diesel_engine"), 35_000, 10);
                double burnt = 1000 - fs.quantity();
                double expect = 35_000 * 10 / 0.35 / diesel.joulesPerUnit();
                if (Math.abs(shaft - 350_000) > 1 || Math.abs(burnt - expect) > 1e-9) f.add("engine feed gave " + shaft + " J for " + burnt + " L, expected 350000 J for " + expect);
                if (tankBe.feed(Fuels.engine("petrol_engine"), 1000, 1) != 0) f.add("petrol engine drew diesel");
                if (tankBe.signal() != Ports.level(fs.quantity() / CarrierTankEntity.capacityNow())) f.add("tank signal wrong");
                fs.extract(1e9);
                if (tankBe.signal() != 0) f.add("empty tank signal not 0");
                if (fs.insert(mana, 100) != 100) f.add("tank refused mana after emptying");
                double mShaft = tankBe.feed(Fuels.engine("arcane_motor"), 600, 10);
                if (Math.abs(mShaft - 6000) > 1e-6 || fs.quantity() != 0) f.add("arcane feed gave " + mShaft + " J, left " + fs.quantity());
                fs.insert(diesel, CarrierTankEntity.capacityNow());
                if (tankBe.signal() != 15) f.add("full tank signal not 15");
                fs.extract(1e9);
                var fp = level.getCapability(Capabilities.Fluid.BLOCK, ct, Direction.UP);
                if (fp == null) f.add("carrier tank has no fluid port");
                else try (Transaction tx = Transaction.openRoot()) {
                    if (fp.insert(FluidResource.of(Fluids.WATER), 1000, tx) != 0) f.add("tank fluid port took water");
                    tx.commit();
                }
            }
            if (Ports.level(0) != 0 || Ports.level(0.0001) != 1 || Ports.level(0.5) != 8 || Ports.level(1) != 15) f.add("Ports.level wrong");

            // ---- magic carrier: reagent joules to quanta, pushes into a neighbouring carrier tank
            BlockPos mc = at.south(2).east();
            level.setBlock(mc, DiaithirBridges.MAGIC_CARRIER.get().defaultBlockState(), 3);
            var mcBe = (MagicCarrierEntity) level.getBlockEntity(mc);
            double q = mcBe.quantaFromJoules(16_000);
            if (Math.abs(q - 16_000 * Config.get(Config.MAGIC_YIELD_PCT) / 100.0 / mcBe.carrier().joulesPerUnit()) > 1e-9 || q <= 0) f.add("reagent to quanta wrong: " + q);
            if (mcBe.canPlaceItem(0, new ItemStack(Items.STONE))) f.add("magic carrier took a non-reagent");
            var magicStore = level.getCapability(Ports.FUEL, mc, Direction.UP);
            if (magicStore == null) f.add("magic carrier has no fuel store");
            else {
                String first = mcBe.carrier().id();
                if (magicStore.insert(mcBe.carrier(), 500) != 500) f.add("magic carrier store refused its own carrier");
                if (mcBe.cycle()) f.add("carrier changed while holding quanta");
                mcBe.serverTick();
                var tankStore = level.getCapability(Ports.FUEL, ct, Direction.UP);
                if (tankStore == null || tankStore.quantity() != 100 || tankStore.fuel() != mcBe.carrier()) f.add("quanta did not reach the tank: " + (tankStore == null ? "no store" : tankStore.quantity()));
                if (Math.abs(magicStore.quantity() - 400) > 1e-9) f.add("magic store did not lose what it pushed: " + magicStore.quantity());
                for (int i = 0; i < 10; i++) mcBe.serverTick();
                magicStore.extract(1e9);
                if (!mcBe.cycle() || mcBe.carrier().id().equals(first)) f.add("carrier did not change when empty");
            }

            // ---- locomotive charger: takes FE on any face, signal port; no trains lane in this test, so it must idle and keep its energy
            BlockPos lc = at.north(2);
            level.setBlock(lc, DiaithirBridges.LOCO_CHARGER.get().defaultBlockState(), 3);
            var lcIn = level.getCapability(Capabilities.Energy.BLOCK, lc, Direction.EAST);
            if (lcIn == null || insert(lcIn, 5000) != 5000) f.add("loco charger takes no FE");
            else if (level.getBlockEntity(lc) instanceof LocoChargerEntity le) {
                if (extract(lcIn, 100) != 0) f.add("loco charger gave FE out");
                if (le.signal() <= 0) f.add("loco charger signal 0 with energy");
                if (CartMagic.available()) f.add("CartMagic available without the trains lane in this test");
                else if (le.serverTick() != 0 || le.energy().getAmountAsInt() != 5000) f.add("loco charger spent FE with no trains lane");
            }

            // ---- cell guard on a stack carrying a stand-in component (ae2 itself is not needed)
            ItemStack plain = new ItemStack(Items.STONE);
            if (!CellGuard.isEmpty(plain)) f.add("plain stack counted as full");
            ItemStack named = new ItemStack(Items.STONE);
            named.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("x"));
            var custom = Set.of(net.minecraft.resources.Identifier.parse("minecraft:custom_name"));
            if (!CellGuard.hasContents(named, custom)) f.add("guard missed a configured component");
            if (CellGuard.hasContents(plain, custom)) f.add("guard false positive");
            if (!net.minecraft.core.registries.BuiltInRegistries.RECIPE_SERIALIZER.containsKey(net.minecraft.resources.Identifier.parse("diaithirbridges:empty_cell_convert"))) f.add("cell recipe serializer missing");
        } catch (RuntimeException e) {
            f.add("exception " + e);
        } finally {
            for (BlockPos p : BlockPos.betweenClosed(at.offset(-3, -1, -3), at.offset(3, 3, 3))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
        }
        return f;
    }
}
