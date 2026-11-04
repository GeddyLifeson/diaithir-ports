package net.diaithir.logistics.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Remembers the block a ghost will become and until when a courier has claimed it. */
public final class GhostBlockEntity extends BlockEntity {
    private BlockState target = Blocks.STONE.defaultBlockState();
    private long claimedUntil = -1;

    public GhostBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirLogistics.GHOST_BE.get(), pos, state);
    }

    public BlockState target() {
        return target;
    }

    public void setTarget(BlockState t) {
        target = t;
        setChanged();
    }

    public boolean claimed(long now) {
        return claimedUntil > now;
    }

    public void claim(long until) {
        claimedUntil = until;
        setChanged();
    }

    public void release() {
        claimedUntil = -1;
        setChanged();
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (!level.isClientSide()) Sites.addGhost(level, worldPosition);
    }

    @Override
    public void setRemoved() {
        if (level != null) Sites.removeGhost(level, worldPosition);
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.store("target", BlockState.CODEC, target);
        out.putLong("claimedUntil", claimedUntil);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.read("target", BlockState.CODEC).ifPresent(s -> target = s);
        claimedUntil = in.getLongOr("claimedUntil", -1);
    }
}
