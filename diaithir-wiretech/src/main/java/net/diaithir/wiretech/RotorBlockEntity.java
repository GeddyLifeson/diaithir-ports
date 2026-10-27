package net.diaithir.wiretech;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Torque source. Crank: torque for a short time after each turn. Windmill: needs a mostly clear 5 by 5 frame in front of its
 * face, and gives more torque higher up and in rain. Water wheel: torque from each side touching flowing water.
 * Dynamos read the torque of the rotors next to them.
 */
public final class RotorBlockEntity extends BlockEntity {
    public final RotorKind kind;
    private int torque;
    private int crankLeft;
    private int tickCount;

    public RotorBlockEntity(BlockPos pos, BlockState state) {
        super(Machines.ROTOR_BE.get(), pos, state);
        this.kind = ((RotorBlock) state.getBlock()).kind;
    }

    public int torque() {
        return torque;
    }

    public void crank() {
        crankLeft = Config.CRANK_TICKS.get();
        torque = Config.CRANK_TORQUE.get();
        setChanged();
    }

    public void serverTick(ServerLevel level) {
        switch (kind) {
            case HAND_CRANK -> {
                if (crankLeft > 0) {
                    crankLeft--;
                    if (crankLeft == 0) torque = 0;
                }
            }
            case WINDMILL -> {
                if (tickCount % 20 == 0) recompute(level);
            }
            case WATER_WHEEL -> {
                if (tickCount % 10 == 0) recompute(level);
            }
        }
        tickCount++;
    }

    /** Fraction of the 24 frame cells around a windmill that are open. */
    public double frameOpenness(ServerLevel level) {
        Direction right = getBlockState().getValue(RotorBlock.FACING).getClockWise();
        int open = 0;
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                if (i == 0 && j == 0) continue;
                BlockPos p = worldPosition.relative(right, i).above(j);
                BlockState st = level.getBlockState(p);
                if (st.getCollisionShape(level, p).isEmpty() && st.getFluidState().isEmpty()) open++;
            }
        }
        return open / 24.0;
    }

    public int flowingSides(ServerLevel level) {
        int n = 0;
        for (Direction d : Direction.values()) {
            FluidState fs = level.getFluidState(worldPosition.relative(d));
            if (fs.is(FluidTags.WATER) && !fs.isSource()) n++;
        }
        return n;
    }

    /** Works out torque now. Windmills and water wheels do this on their own every second or so. */
    public void recompute(ServerLevel level) {
        int next = 0;
        if (kind == RotorKind.WINDMILL) {
            double open = frameOpenness(level);
            if (open >= 0.75) {
                double height = Mth.clamp(0.5 + (worldPosition.getY() - 62) / 64.0, 0.5, 2.0);
                double weather = level.isRaining() ? 1.3 : 1.0;
                next = Math.max(1, (int) Math.round(Config.WINDMILL_TORQUE.get() * open * height * weather));
            }
        } else if (kind == RotorKind.WATER_WHEEL) {
            next = flowingSides(level) * Config.WATERWHEEL_TORQUE.get();
        }
        torque = next;
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putInt("crank_left", crankLeft);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        crankLeft = in.getIntOr("crank_left", 0);
        if (kind == RotorKind.HAND_CRANK) torque = crankLeft > 0 ? Config.CRANK_TORQUE.get() : 0;
    }
}
