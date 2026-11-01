package net.diaithir.wiretech;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** A plain FE buffer that its owner can fill and drain directly, and push into neighbouring blocks. */
public final class FeBuffer extends SimpleEnergyHandler {
    private final Runnable onChange;

    public FeBuffer(int capacity, int maxInsert, int maxExtract, Runnable onChange) {
        super(capacity, maxInsert, maxExtract);
        this.onChange = onChange;
    }

    public int amount() {
        return getAmountAsInt();
    }

    public int room() {
        return capacity - energy;
    }

    public int capacity() {
        return capacity;
    }

    /** Owner side: add FE, returns how much fitted. */
    public int give(int n) {
        int add = Math.max(0, Math.min(n, capacity - energy));
        if (add > 0) set(energy + add);
        return add;
    }

    /** Owner side: remove FE, returns how much was there. */
    public int take(int n) {
        int sub = Math.max(0, Math.min(n, energy));
        if (sub > 0) set(energy - sub);
        return sub;
    }

    @Override
    protected void onEnergyChanged(int previousAmount) {
        onChange.run();
    }

    /** Pushes up to max FE into the six neighbours that accept it. Returns what was pushed. */
    public int pushToNeighbours(ServerLevel level, BlockPos pos, int max, boolean skipConnectors) {
        int pushed = 0;
        for (Direction d : Direction.values()) {
            int avail = Math.min(max - pushed, energy);
            if (avail <= 0) break;
            BlockPos np = pos.relative(d);
            BlockEntity be = level.getBlockEntity(np);
            if (be == null || (skipConnectors && be instanceof ConnectorBlockEntity)) continue;
            EnergyHandler h = level.getCapability(Capabilities.Energy.BLOCK, np, d.getOpposite());
            if (h == null) continue;
            try (Transaction tx = Transaction.openRoot()) {
                int n = h.insert(avail, tx);
                if (n > 0) {
                    tx.commit();
                    take(n);
                    pushed += n;
                }
            }
        }
        return pushed;
    }
}
