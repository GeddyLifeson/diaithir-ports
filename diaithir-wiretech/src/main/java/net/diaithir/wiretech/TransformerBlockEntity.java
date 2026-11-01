package net.diaithir.wiretech;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

public final class TransformerBlockEntity extends BlockEntity {
    public static final double EFFICIENCY = 0.95;

    public final WireType out;
    public final FeBuffer energy;

    public TransformerBlockEntity(BlockPos pos, BlockState state) {
        super(Machines.TRANSFORMER_BE.get(), pos, state);
        this.out = ((TransformerBlock) state.getBlock()).out;
        this.energy = new FeBuffer(out.throughput * 4, out.throughput, 0, this::setChanged);
    }

    /** Everything but the front accepts FE. */
    public @Nullable EnergyHandler energyFor(@Nullable Direction side) {
        return side == getBlockState().getValue(TransformerBlock.FACING) ? null : energy;
    }

    public void serverTick(ServerLevel level) {
        if (energy.amount() <= 0) return;
        Direction front = getBlockState().getValue(TransformerBlock.FACING);
        BlockPos np = worldPosition.relative(front);
        EnergyHandler h = level.getCapability(Capabilities.Energy.BLOCK, np, front.getOpposite());
        if (h == null) return;
        int send = Math.min(out.throughput, energy.amount());
        try (Transaction tx = Transaction.openRoot()) {
            int taken = h.insert((int) Math.floor(send * EFFICIENCY), tx);
            if (taken > 0) {
                tx.commit();
                energy.take((int) Math.ceil(taken / EFFICIENCY));
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput o) {
        super.saveAdditional(o);
        energy.serialize(o.child("fe"));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.child("fe").ifPresent(energy::deserialize);
    }
}
