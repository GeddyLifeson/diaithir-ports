package net.diaithir.vehicles;

import com.mojang.brigadier.CommandDispatcher;
import net.diaithir.vehicles.fuel.FuelCaps;
import net.diaithir.vehicles.fuel.FuelStore;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/** /diaithirvehicles selftest (owners): tank block, capabilities, jerry can and fluid view, at a spot high above spawn. */
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
        level.setBlockAndUpdate(pos, DiaithirVehicles.FUEL_TANK.get().defaultBlockState());
        FuelStore s = level.getCapability(FuelCaps.BLOCK, pos, null);
        n++; if (s == null) return "FAIL no fuel capability on the tank block";
        n++; if (s.capacityL() != FuelTankEntity.CAPACITY_L) bad.append(" capacity");
        n++; if (s.fill("c:diesel", 5000, false) != 5000) bad.append(" fill");
        n++; if (s.fill("c:gasoline", 100, false) != 0) bad.append(" mix");
        n++; if (Math.abs(s.energyJoules() - 5000 * 38.6e6) > 1) bad.append(" energy");
        ItemStack can = new ItemStack(DiaithirVehicles.JERRY_CAN.get());
        FuelStore cs = can.getCapability(FuelCaps.ITEM, null);
        n++; if (cs == null) return "FAIL no capability on the jerry can";
        n++; if (s.transferTo(cs, 100) != 20) bad.append(" tank->can");
        n++; if (!"c:diesel".equals(cs.fuelId()) || cs.amountL() != 20) bad.append(" can content");
        n++; if (cs.transferTo(s, 100) != 20 || s.amountL() != 5000) bad.append(" can->tank");
        n++; if (!(level.getBlockEntity(pos) instanceof FuelTankEntity be) || be.fluid.getAmountAsLong(0) != 5000) bad.append(" fluid view amount");
        level.removeBlock(pos, false);
        return bad.length() == 0 ? "PASS " + n + " checks" : "FAIL" + bad;
    }
}
