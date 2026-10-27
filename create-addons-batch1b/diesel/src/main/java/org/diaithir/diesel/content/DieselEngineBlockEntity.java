package org.diaithir.diesel.content;

import com.zurrtum.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollOptionBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.diaithir.diesel.Fuels;
import org.diaithir.diesel.registries.CDGBlockEntityTypes;
import org.jspecify.annotations.Nullable;

import java.util.List;

/** Burns fuel from its tank and generates rotation; analog redstone throttles it (15 stops it). */
public class DieselEngineBlockEntity extends GeneratingKineticBlockEntity {
    /** One bucket is 81000 units in the Create port; the tank holds 1000 mB. */
    public static final int UNITS_PER_MB = 81;
    public static final int CAPACITY = 1000 * UNITS_PER_MB;

    public ServerScrollOptionBehaviour<RotationDirection> movementDirection;
    public SmartFluidTankBehaviour tank;
    private int analogSignal;
    private float lastSpeed, lastCapacity;
    private float fuelDebt;
    private boolean signalChanged;
    public EngineUpgrade upgrade = EngineUpgrade.NONE;

    public DieselEngineBlockEntity(BlockPos pos, BlockState state) {
        super(CDGBlockEntityTypes.DIESEL_ENGINE.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        movementDirection = new ServerScrollOptionBehaviour<>(RotationDirection.class, this);
        movementDirection.withCallback(v -> reActivateSource = true);
        tank = SmartFluidTankBehaviour.single(this, CAPACITY);
        behaviours.add(movementDirection);
        behaviours.add(tank);
    }

    public void setAnalogSignal(int signal) {
        if (signal != analogSignal) {
            analogSignal = signal;
            signalChanged = true;
        }
    }

    public Fuels.@Nullable Fuel fuel() {
        FluidStack fluid = tank.getPrimaryHandler().getFluid();
        return fluid.isEmpty() ? null : Fuels.of(fluid.getFluid());
    }

    public float throttle() {
        return (15 - analogSignal) / 15f;
    }

    private Fuels.@Nullable Per props() {
        Fuels.Fuel fuel = fuel();
        return fuel == null ? null : fuel.normal();
    }

    @Override
    public float calculateAddedStressCapacity() {
        Fuels.Per p = props();
        float speed = Math.abs(getGeneratedSpeed());
        if (p == null || speed == 0) return 0;
        lastCapacityProvided = p.strength() * upgrade.multiplier / Math.max(0.01f, speed);
        return lastCapacityProvided;
    }

    @Override
    public float getGeneratedSpeed() {
        Fuels.Per p = props();
        float throttle = throttle();
        if (p == null || throttle == 0) return 0;
        return convertToDirection((movementDirection.getValue() == 1 ? -1 : 1) * p.speed() * upgrade.multiplier * throttle, getBlockState().getValue(DieselEngineBlock.FACING));
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide()) return;
        if (signalChanged) {
            signalChanged = false;
            reActivateSource = true;
        }
        float speed = getGeneratedSpeed();
        float capacity = speed == 0 ? 0 : calculateAddedStressCapacity();
        if (speed != lastSpeed || capacity != lastCapacity) {
            lastSpeed = speed;
            lastCapacity = capacity;
            reActivateSource = true;
        }
        Fuels.Per p = props();
        boolean burning = p != null && speed != 0 && !isOverStressed() && !tank.getPrimaryHandler().getFluid().isEmpty();
        if (getBlockState().getValue(DieselEngineBlock.LIT) != burning)
            level.setBlock(worldPosition, getBlockState().setValue(DieselEngineBlock.LIT, burning), 3);
        if (p != null && speed != 0 && !isOverStressed()) {
            float throttle = throttle();
            fuelDebt += p.burn() * UNITS_PER_MB * (0.25f + throttle * 0.75f);
            while (fuelDebt >= 1f) {
                FluidStack fluid = tank.getPrimaryHandler().getFluid();
                int drained = Math.min(fluid.getAmount(), (int) fuelDebt);
                if (drained <= 0) break;
                fluid.decrement(drained);
                if (fluid.isEmpty()) tank.getPrimaryHandler().setFluid(FluidStack.EMPTY);
                tank.getPrimaryHandler().markDirty();
                fuelDebt -= drained;
            }
        }
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putInt("AnalogSignal", analogSignal);
        view.putString("Upgrade", upgrade.id);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        analogSignal = view.getIntOr("AnalogSignal", 0);
        upgrade = EngineUpgrade.byId(view.getStringOr("Upgrade", "none"));
        fuelDebt = 0;
    }
}
