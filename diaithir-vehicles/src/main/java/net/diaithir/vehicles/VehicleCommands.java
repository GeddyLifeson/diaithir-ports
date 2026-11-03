package net.diaithir.vehicles;

import com.mojang.brigadier.CommandDispatcher;
import net.diaithir.units.fuel.Engine;
import net.diaithir.units.fuel.Fuels;
import net.diaithir.units.fuel.FuelStore;
import net.diaithir.units.fuel.SimpleStore;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/** /diaithirvehicles selftest (owners): a Carrier Tank high above spawn, a jerry can, and the burn maths on a vehicle-style store. */
final class VehicleCommands {
    private VehicleCommands() {}

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("diaithirvehicles").then(Commands.literal("trackstest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
            String r = TrackTest.run(c.getSource().getLevel());
            c.getSource().sendSuccess(() -> Component.literal(r), false);
            return r.startsWith("PASS") ? 1 : 0;
        })).then(Commands.literal("selftest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
            String r = run(c.getSource().getLevel());
            c.getSource().sendSuccess(() -> Component.literal(r), false);
            return r.startsWith("PASS") ? 1 : 0;
        })));
    }

    static String run(ServerLevel level) {
        StringBuilder bad = new StringBuilder();
        int n = 0;
        BlockPos pos = level.getRespawnData().pos().above(60);
        level.getChunkAt(pos);
        var tankBlock = BuiltInRegistries.BLOCK.getValue(Identifier.parse("diaithirbridges:carrier_tank"));
        level.setBlockAndUpdate(pos, tankBlock.defaultBlockState());
        FuelStore s = FuelLookup.block(level, pos, Direction.UP);
        n++; if (s == null) return "FAIL no Ports.FUEL store on the Carrier Tank";
        n++; if (s.insert(Fuels.get("diesel"), 500) != 500) bad.append(" fill");
        n++; if (s.insert(Fuels.get("petrol"), 10) != 0) bad.append(" mix");
        ItemStack can = new ItemStack(DiaithirVehicles.JERRY_CAN.get());
        FuelStore cs = new JerryCan.CanStore(can);
        n++; if (FuelStore.transfer(s, cs, 100) != 20) bad.append(" tank->can");
        n++; if (cs.fuel() != Fuels.get("diesel") || cs.quantity() != 20) bad.append(" can content");
        n++; if (cs.insert(Fuels.get("coal"), 1) != 0) bad.append(" can took a solid");
        n++; if (FuelStore.transfer(cs, s, 100) != 20 || s.quantity() != 500) bad.append(" can->tank");
        // vehicle-style store burnt through the shared engine maths
        SimpleStore veh = new SimpleStore(100);
        veh.insert(Fuels.get("diesel"), 50);
        Engine diesel = Fuels.engine("diesel_engine");
        n++; if (!FuelBurn.canFeed(veh, diesel) || FuelBurn.canFeed(veh, Fuels.engine("petrol_engine"))) bad.append(" engine accepts");
        double j = FuelBurn.feed(veh, diesel, 100_000, 10);
        n++; if (Math.abs(j - 1_000_000) > 1 || veh.quantity() >= 50) bad.append(" feed ").append(j);
        level.removeBlock(pos, false);
        return bad.length() == 0 ? "PASS " + n + " checks" : "FAIL" + bad;
    }
}
