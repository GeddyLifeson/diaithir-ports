package net.diaithir.wiretech;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.jspecify.annotations.Nullable;

public final class DynamoBlockEntity extends BlockEntity {
    public static final int CAPACITY = 4000, MAX_OUT = 1024;

    public final FeBuffer energy = new FeBuffer(CAPACITY, 0, MAX_OUT, this::setChanged);
    private int lastTorque;

    public DynamoBlockEntity(BlockPos pos, BlockState state) {
        super(Machines.DYNAMO_BE.get(), pos, state);
    }

    public @Nullable EnergyHandler energyFor(@Nullable Direction side) {
        return energy;
    }

    public int torque() {
        return lastTorque;
    }

    public int adjacentTorque(ServerLevel level) {
        int sum = 0;
        for (Direction d : Direction.values()) if (level.getBlockEntity(worldPosition.relative(d)) instanceof RotorBlockEntity r) sum += r.torque();
        return sum;
    }

    public void serverTick(ServerLevel level) {
        lastTorque = adjacentTorque(level);
        if (lastTorque > 0) energy.give(lastTorque * Config.FE_PER_TORQUE.get());
        if (energy.amount() > 0) energy.pushToNeighbours(level, worldPosition, MAX_OUT, false);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        energy.serialize(out.child("fe"));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.child("fe").ifPresent(energy::deserialize);
    }
}
