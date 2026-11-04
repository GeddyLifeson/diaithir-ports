package net.diaithir.storagenet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Holds a wireless transmitter's frequency (0 to 99) and keeps it registered in {@link Links} while loaded. */
public final class WirelessBlockEntity extends BlockEntity {
    public static final int FREQUENCIES = 100;
    private int frequency;

    public WirelessBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirStorageNet.WIRELESS_BE.get(), pos, state);
    }

    public int frequency() {
        return frequency;
    }

    public void setFrequency(int f) {
        frequency = Math.floorMod(f, FREQUENCIES);
        setChanged();
        if (level != null) Networks.markDirtyEverywhere();
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (!level.isClientSide()) Links.register(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) Links.register(this);
    }

    @Override
    public void setRemoved() {
        Links.unregister(this);
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putInt("frequency", frequency);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        frequency = Math.floorMod(in.getIntOr("frequency", 0), FREQUENCIES);
    }
}
