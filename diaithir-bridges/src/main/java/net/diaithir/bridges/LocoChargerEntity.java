package net.diaithir.bridges;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

/**
 * Locomotive charger: takes Forge Energy on any face and pours it, at 1 FE = 1 J, into the magic joule bank of every fitted locomotive standing on
 * the block above (a rail on top of it). Big locomotives depend on this because items alone only fire small engines.
 */
public final class LocoChargerEntity extends BlockEntity {
    private final Buf energy = new Buf();
    private long delivered;

    LocoChargerEntity(BlockPos pos, BlockState state) { super(DiaithirBridges.LOCO_CHARGER_ENTITY.get(), pos, state); }

    private final class Buf extends SimpleEnergyHandler {
        Buf() { super(Config.get(Config.LOCO_BUFFER), Config.get(Config.LOCO_RATE), 0); }
        @Override protected void onEnergyChanged(int previous) { setChanged(); }
        void drain(int n) { set(Math.max(0, energy - n)); }
    }

    EnergyHandler energy() { return energy; }

    int signal() { return Ports.level((double) energy.getAmountAsInt() / Math.max(1, energy.getCapacityAsInt())); }

    /** Pays up to the per-tick rate into each fitted locomotive above; returns joules moved. */
    int serverTick() {
        if (level == null || energy.getAmountAsInt() <= 0 || !CartMagic.available()) return 0;
        int moved = 0;
        for (AbstractMinecart cart : level.getEntitiesOfClass(AbstractMinecart.class, new AABB(worldPosition.above()))) {
            int give = Math.min(energy.getAmountAsInt(), Config.get(Config.LOCO_RATE));
            if (give <= 0) break;
            if (CartMagic.fitted(cart) && CartMagic.charge(cart, give)) { energy.drain(give); moved += give; delivered += give; }
        }
        return moved;
    }

    Component status() {
        return Component.literal(energy.getAmountAsInt() + " / " + energy.getCapacityAsInt() + " FE, " + delivered + " J delivered"
                + (CartMagic.available() ? "" : " (trains lane not installed)"));
    }

    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        energy.serialize(out.child("energy"));
        out.putLong("delivered", delivered);
    }

    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.child("energy").ifPresent(energy::deserialize);
        delivered = in.getLongOr("delivered", 0);
    }
}
