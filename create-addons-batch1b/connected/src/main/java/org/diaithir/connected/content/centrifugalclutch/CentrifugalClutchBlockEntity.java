package org.diaithir.connected.content.centrifugalclutch;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.kinetics.RotationPropagator;
import com.zurrtum.create.content.kinetics.transmission.SplitShaftBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.TickPriority;
import org.diaithir.connected.registries.CCBlocks;

import java.util.List;

import static org.diaithir.connected.content.CCBlockStateProperties.UNCOUPLED;

/** Couples only while the shaft turns at or above (positive value) or at or below (negative value) a threshold speed. */
public class CentrifugalClutchBlockEntity extends SplitShaftBlockEntity {
    public static final int DEFAULT_SPEED = 64;
    public static final int MAX_SPEED = 256;
    public ServerScrollValueBehaviour speedThreshold;
    public boolean reattachNextTick = false;

    public CentrifugalClutchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        speedThreshold = new ServerScrollValueBehaviour(this).between(-MAX_SPEED, MAX_SPEED);
        speedThreshold.setValue(DEFAULT_SPEED);
        speedThreshold.withCallback(i -> this.onKineticUpdate());
        behaviours.add(speedThreshold);
    }

    @Override
    public void initialize() {
        onKineticUpdate();
        super.initialize();
    }

    private void onKineticUpdate() {
        boolean coupled = !getBlockState().getValue(UNCOUPLED);
        float abs = Mth.abs(getSpeed());
        int threshold = speedThreshold.getValue();
        boolean reached = abs > 0 && (threshold < 0 ? abs <= Mth.abs(threshold) : abs >= Mth.abs(threshold));
        if (coupled != reached && !isOverStressed() && level != null) {
            level.setBlockAndUpdate(getBlockPos(), getBlockState().cycle(UNCOUPLED));
            level.scheduleTick(getBlockPos(), CCBlocks.CENTRIFUGAL_CLUTCH.get(), 0, TickPriority.EXTREMELY_HIGH);
            reattachNextTick = true;
        }
    }

    @Override
    public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
        super.updateFromNetwork(maxStress, currentStress, networkSize);
        onKineticUpdate();
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        onKineticUpdate();
        super.onSpeedChanged(previousSpeed);
    }

    @Override
    public void tick() {
        super.tick();
        if (reattachNextTick && level != null) {
            reattachNextTick = false;
            RotationPropagator.handleAdded(level, getBlockPos(), this);
        }
    }

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (face == getBlockState().getValue(CentrifugalClutchBlock.FACING) && getBlockState().getValue(UNCOUPLED)) return 0;
        return 1;
    }
}
