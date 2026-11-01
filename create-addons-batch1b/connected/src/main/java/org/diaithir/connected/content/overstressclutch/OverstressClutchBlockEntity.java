package org.diaithir.connected.content.overstressclutch;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.kinetics.RotationPropagator;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.transmission.SplitShaftBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.ticks.TickPriority;
import org.diaithir.connected.content.overstressclutch.OverstressClutchBlock.ClutchState;
import org.diaithir.connected.registries.CCBlocks;

import java.util.List;

import static org.diaithir.connected.content.overstressclutch.OverstressClutchBlock.POWERED;
import static org.diaithir.connected.content.overstressclutch.OverstressClutchBlock.STATE;

/** Cuts the shaft after the network has stayed overstressed for the configured delay (ticks). */
public class OverstressClutchBlockEntity extends SplitShaftBlockEntity {
    public int delay;
    public ServerScrollValueBehaviour maxDelay;

    public OverstressClutchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        maxDelay = new ServerScrollValueBehaviour(this).between(1, 20 * 60 * 5);
        maxDelay.withCallback(max -> {
            delay = Mth.clamp(delay, 0, max);
            sendData();
        });
        maxDelay.setValue(20);
        behaviours.add(maxDelay);
    }

    @Override
    public void initialize() {
        onKineticUpdate();
        super.initialize();
    }

    public void onKineticUpdate() {
        BlockState state = getBlockState();
        if (state.getValue(STATE) == ClutchState.UNCOUPLED && state.getValue(POWERED)) {
            resetClutch();
            return;
        }
        if (level == null) return;
        if (IRotate.StressImpact.isEnabled() && !state.getValue(POWERED) && isOverStressed() && state.getValue(STATE) == ClutchState.COUPLED) {
            level.setBlock(getBlockPos(), state.setValue(STATE, ClutchState.UNCOUPLING), 2 | 16);
            delay = maxDelay.getValue() - 1;
            sendData();
            return;
        }
        if (!isOverStressed() && state.getValue(STATE) == ClutchState.UNCOUPLING) {
            level.setBlock(getBlockPos(), state.setValue(STATE, ClutchState.COUPLED), 2 | 16);
        }
    }

    @Override
    public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
        super.updateFromNetwork(maxStress, currentStress, networkSize);
        onKineticUpdate();
    }

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (hasSource() && face != getSourceFacing() && getBlockState().getValue(STATE) == ClutchState.UNCOUPLED) return 0;
        return 1;
    }

    public void resetClutch() {
        if (getBlockState().getValue(STATE) == ClutchState.UNCOUPLED && !isOverStressed() && level != null) {
            level.setBlock(getBlockPos(), getBlockState().setValue(STATE, ClutchState.COUPLED), 3);
            RotationPropagator.handleRemoved(level, getBlockPos(), this);
            RotationPropagator.handleAdded(level, getBlockPos(), this);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (getBlockState().getValue(STATE) == ClutchState.UNCOUPLING && level != null && !level.isClientSide()) {
            level.scheduleTick(getBlockPos(), CCBlocks.OVERSTRESS_CLUTCH.get(), 0, TickPriority.EXTREMELY_HIGH);
        }
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        delay = view.getIntOr("Delay", 0);
        super.read(view, clientPacket);
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        view.putInt("Delay", delay);
        super.write(view, clientPacket);
    }
}
