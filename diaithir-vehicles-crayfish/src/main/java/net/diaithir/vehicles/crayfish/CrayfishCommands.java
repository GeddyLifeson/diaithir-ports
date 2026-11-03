// SPDX-License-Identifier: GPL-3.0-only
// Folded from MrCrayfish's Vehicle Mod (Unofficial 1.20.1 port by bay4lly; original by MrCrayfish, GPL). Rewritten for NeoForge 26.1.2; art is not used.
package net.diaithir.vehicles.crayfish;

import com.mojang.brigadier.CommandDispatcher;
import net.diaithir.vehicles.DiaithirVehicles;
import net.diaithir.vehicles.fuel.FuelCaps;
import net.diaithir.vehicles.fuel.FuelStore;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;

/** /diaithircrayfish selftest (owners) and /diaithircrayfish trailer (owners, spawns a tank trailer at your feet). */
final class CrayfishCommands {
    private CrayfishCommands() {}

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("diaithircrayfish")
                .then(Commands.literal("trailer").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    TankTrailer t = CrayfishVehicles.TANK_TRAILER.get().create(c.getSource().getLevel(), EntitySpawnReason.COMMAND);
                    t.setPos(c.getSource().getPosition());
                    c.getSource().getLevel().addFreshEntity(t);
                    return 1;
                }))
                .then(Commands.literal("selftest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    String r = run(c.getSource().getLevel());
                    c.getSource().sendSuccess(() -> Component.literal(r), false);
                    return r.startsWith("PASS") ? 1 : 0;
                })));
    }

    static String run(ServerLevel level) {
        StringBuilder bad = new StringBuilder();
        int n = 0;
        BlockPos base = level.getRespawnData().pos().above(70);
        level.getChunkAt(base);
        // pump on a fuel tank
        level.setBlockAndUpdate(base, DiaithirVehicles.FUEL_TANK.get().defaultBlockState());
        level.setBlockAndUpdate(base.above(), CrayfishVehicles.GAS_PUMP.get().defaultBlockState());
        n++; if (!(level.getBlockEntity(base.above()) instanceof GasPumpEntity pump)) return "FAIL no pump block entity";
        FuelStore tank = pump.tank();
        n++; if (tank == null) return "FAIL pump sees no tank below";
        tank.fill("c:diesel", 3000, false);
        // trailer
        TankTrailer t = CrayfishVehicles.TANK_TRAILER.get().create(level, EntitySpawnReason.COMMAND);
        t.setPos(base.getX() + 0.5, base.getY() + 3, base.getZ() + 10.5);
        level.addFreshEntity(t);
        n++; if (t.getCapability(FuelCaps.ENTITY, null) != t.tank) bad.append(" trailer fuel cap");
        n++; if (pump.refuel(t.getCapability(FuelCaps.ENTITY, null)) != Config.PUMP_LITRES_PER_USE.get() || t.tank.amountL() != 50 || tank.amountL() != 2950) bad.append(" pump refuel");
        // hitch: an armour stand stands in for the tow vehicle
        ArmorStand tow = new ArmorStand(net.minecraft.world.entity.EntityType.ARMOR_STAND, level);
        tow.setPos(base.getX() + 0.5, base.getY() + 3, base.getZ() + 0.5);
        level.addFreshEntity(tow);
        t.setPulling(tow);
        for (int i = 0; i < 5; i++) t.follow(TowPoint.of(tow));
        double dist = Math.hypot(t.getX() - tow.getX(), t.getZ() - tow.getZ());
        n++; if (Math.abs(dist - 2.0) > 0.05) bad.append(" hitch distance ").append(dist);
        n++; if (Math.abs(Math.abs(Math.toRadians(t.getYRot())) - Math.PI) > 0.01) bad.append(" hitch yaw ").append(t.getYRot());
        // crash and repair
        float before = t.health.health();
        n++; if (!t.hurtServer(level, level.damageSources().generic(), 30F) || t.health.health() != before - 30F) bad.append(" damage");
        n++; if (!t.health.repair() || t.health.health() != before - 25F) bad.append(" repair");
        n++; if (VehicleHealth.fallDamage(12, -2) != 6F) bad.append(" fall");
        t.hurtServer(level, level.damageSources().generic(), 500F);
        n++; if (t.isAlive()) bad.append(" not destroyed");
        tow.discard();
        t.discard();
        level.removeBlock(base.above(), false);
        level.removeBlock(base, false);
        return bad.length() == 0 ? "PASS " + n + " checks" : "FAIL" + bad;
    }
}
