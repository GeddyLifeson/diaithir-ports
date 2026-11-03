// SPDX-License-Identifier: GPL-3.0-only
// Folded from MrCrayfish's Vehicle Mod (Unofficial 1.20.1 port by bay4lly; original by MrCrayfish, GPL). Rewritten for NeoForge 26.1.2; art is not used.
package net.diaithir.vehicles.crayfish;

import net.diaithir.vehicles.FuelLookup;
import net.diaithir.units.fuel.FuelStore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/** The pump head. Its tank is whatever fuel store sits directly below it (the fuel tank block); one player holds the hose at a time and loses it by walking too far. */
public class GasPumpEntity extends BlockEntity {
    private UUID fueler;

    public GasPumpEntity(BlockPos pos, BlockState state) {
        super(CrayfishVehicles.GAS_PUMP_BE.get(), pos, state);
    }

    public FuelStore tank() {
        return level == null ? null : FuelLookup.block(level, worldPosition.below(), Direction.UP);
    }

    public UUID fueler() {
        return fueler;
    }

    public void take(Player p) {
        fueler = p.getUUID();
    }

    public void release() {
        fueler = null;
    }

    /** Moves up to the configured litres from the tank into {@code target}; returns litres moved. */
    public double refuel(FuelStore target) {
        FuelStore t = tank();
        return t == null ? 0 : FuelStore.transfer(t, target, Config.PUMP_LITRES_PER_USE.get());
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GasPumpEntity be) {
        if (be.fueler == null || !(level instanceof ServerLevel sl)) return;
        ServerPlayer p = sl.getServer().getPlayerList().getPlayer(be.fueler);
        if (p == null || !p.isAlive() || p.level() != level || p.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > Math.pow(Config.MAX_HOSE_DISTANCE.get(), 2)) {
            if (p != null && p.isAlive()) level.playSound(null, p.blockPosition(), SoundEvents.ITEM_BREAK.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            be.fueler = null;
        }
    }
}
