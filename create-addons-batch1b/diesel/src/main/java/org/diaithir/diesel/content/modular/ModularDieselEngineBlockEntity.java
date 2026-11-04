package org.diaithir.diesel.content.modular;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.zurrtum.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollOptionBehaviour;
import com.zurrtum.create.foundation.fluid.FluidTank;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.diaithir.diesel.Fuels;
import org.diaithir.diesel.content.EngineUpgrade;
import org.diaithir.diesel.registries.CDGBlockEntityTypes;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ModularDieselEngineBlockEntity extends GeneratingKineticBlockEntity implements IMultiBlockEntityContainer.Fluid {
    public static final int UNITS_PER_MB = 81;
    public static final int CAPACITY = 1000 * UNITS_PER_MB;

    public ServerScrollOptionBehaviour<RotationDirection> movementDirection;
    public EngineUpgrade upgrade = EngineUpgrade.NONE;
    protected final FluidTank tank = new FluidTank(CAPACITY);
    protected @Nullable BlockPos controller;
    protected @Nullable BlockPos lastKnownPos;
    protected int length = 1;
    protected boolean updateConnectivity;
    private int analogSignal;
    private float lastSpeed, lastCapacity, fuelDebt;

    public ModularDieselEngineBlockEntity(BlockPos pos, BlockState state) {
        super(CDGBlockEntityTypes.MODULAR_DIESEL_ENGINE.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        movementDirection = new ServerScrollOptionBehaviour<>(RotationDirection.class, this);
        movementDirection.withCallback(v -> {
            ModularDieselEngineBlockEntity c = getControllerBE();
            if (c != null) c.reActivateSource = true;
        });
        behaviours.add(movementDirection);
        behaviours.add(new com.zurrtum.create.foundation.blockEntity.behaviour.CachedFluidInventoryBehaviour<ModularDieselEngineBlockEntity>(this, b -> b.sharedTank()));
        super.addBehaviours(behaviours);
    }

    /** What pipes see: the controller's tank, or nothing on a segment whose pipe is shut. */
    public @Nullable FluidInventory sharedTank() {
        if (!getBlockState().getValue(ModularDieselEngineBlock.PIPE)) return null;
        ModularDieselEngineBlockEntity c = getControllerBE();
        return c == null ? null : c.tank;
    }

    public void setAnalogSignal(int s) {
        ModularDieselEngineBlockEntity c = getControllerBE();
        if (c != null && c.analogSignal != s) {
            c.analogSignal = s;
            c.reActivateSource = true;
        }
    }

    private float throttle() {
        return (15 - analogSignal) / 15f;
    }

    private Fuels.@Nullable Per props() {
        FluidStack f = tank.getFluid();
        if (f.isEmpty()) return null;
        Fuels.Fuel fuel = Fuels.of(f.getFluid());
        return fuel == null ? null : fuel.modular();
    }

    @Override
    public float getGeneratedSpeed() {
        if (!isController()) return 0;
        Fuels.Per p = props();
        float t = throttle();
        if (p == null || t == 0) return 0;
        return convertToDirection((movementDirection.getValue() == 1 ? -1 : 1) * p.speed() * upgrade.multiplier * t, getBlockState().getValue(ModularDieselEngineBlock.HORIZONTAL_FACING));
    }

    @Override
    public float calculateAddedStressCapacity() {
        Fuels.Per p = props();
        float speed = Math.abs(getGeneratedSpeed());
        if (p == null || speed == 0) return 0;
        lastCapacityProvided = p.strength() * length * upgrade.multiplier / speed;
        return lastCapacityProvided;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;
        if (lastKnownPos == null) lastKnownPos = worldPosition;
        if (updateConnectivity) updateConnectivity();
        if (level.isClientSide() || !isController()) return;
        float speed = getGeneratedSpeed();
        float cap = speed == 0 ? 0 : calculateAddedStressCapacity();
        if (speed != lastSpeed || cap != lastCapacity) {
            lastSpeed = speed;
            lastCapacity = cap;
            reActivateSource = true;
        }
        Fuels.Per p = props();
        boolean burning = p != null && speed != 0 && !isOverStressed();
        setLit(burning);
        if (burning) {
            fuelDebt += length * p.burn() * UNITS_PER_MB * (0.25f + throttle() * 0.75f);
            while (fuelDebt >= 1f) {
                FluidStack f = tank.getFluid();
                int drained = Math.min(f.getAmount(), (int) fuelDebt);
                if (drained <= 0) break;
                f.decrement(drained);
                if (f.isEmpty()) tank.setFluid(FluidStack.EMPTY);
                tank.markDirty();
                fuelDebt -= drained;
            }
        }
    }

    private void setLit(boolean lit) {
        for (int i = 0; i < length; i++) {
            BlockPos at = worldPosition.relative(getMainConnectionAxis() == Direction.Axis.X ? Direction.EAST : Direction.SOUTH, i);
            BlockState st = level.getBlockState(at);
            if (st.getBlock() instanceof ModularDieselEngineBlock && st.getValue(ModularDieselEngineBlock.LIT) != lit)
                level.setBlock(at, st.setValue(ModularDieselEngineBlock.LIT, lit), 3);
        }
    }

    public void updateConnectivity() {
        updateConnectivity = false;
        if (level == null || level.isClientSide() || !isController()) return;
        ConnectivityHandler.formMulti(this);
    }

    // multiblock plumbing
    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity & IMultiBlockEntityContainer> @Nullable T getControllerBE() {
        if (isController() || !hasLevel()) return (T) this;
        BlockEntity be = level.getBlockEntity(controller);
        return be instanceof ModularDieselEngineBlockEntity m ? (T) m : null;
    }

    @Override
    public boolean isController() {
        return controller == null || controller.equals(worldPosition);
    }

    @Override
    public void setController(BlockPos pos) {
        if (level.isClientSide() || pos.equals(controller)) return;
        controller = pos;
        reActivateSource = true;
        setChanged();
        sendData();
    }

    @Override
    public void removeController(boolean keepContents) {
        if (level.isClientSide()) return;
        updateConnectivity = true;
        controller = null;
        length = 1;
        reActivateSource = true;
        setChanged();
        sendData();
    }

    @Override
    public @Nullable BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        reActivateSource = true;
        setChanged();
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return getBlockState().getValue(ModularDieselEngineBlock.HORIZONTAL_FACING).getAxis();
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        return 21;
    }

    @Override
    public int getMaxWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return length;
    }

    @Override
    public void setHeight(int height) {
        length = height;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public void setWidth(int width) {
    }

    @Override
    public boolean hasTank() {
        return true;
    }

    @Override
    public int getTankSize(int t) {
        return 1;
    }

    @Override
    public void setTankSize(int t, int blocks) {
        tank.setCapacity(CAPACITY);
    }

    @Override
    public FluidTank getTank(int t) {
        return tank;
    }

    @Override
    public FluidStack getFluid(int t) {
        return tank.getFluid().copy();
    }

    @Override
    public boolean matches(FluidStack a, FluidStack b) {
        return a.getFluid() == b.getFluid();
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (updateConnectivity) view.putBoolean("Uninitialized", true);
        if (lastKnownPos != null) view.store("LastKnownPos", BlockPos.CODEC, lastKnownPos);
        if (!isController()) view.store("Controller", BlockPos.CODEC, controller);
        if (isController()) {
            view.putString("Upgrade", upgrade.id);
            view.putInt("Length", length);
            view.putInt("AnalogSignal", analogSignal);
            tank.write(view);
        }
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        BlockPos before = controller;
        updateConnectivity = view.getBooleanOr("Uninitialized", false);
        lastKnownPos = view.read("LastKnownPos", BlockPos.CODEC).orElse(null);
        controller = view.read("Controller", BlockPos.CODEC).orElse(null);
        if (isController()) {
            upgrade = EngineUpgrade.byId(view.getStringOr("Upgrade", "none"));
            length = view.getIntOr("Length", 1);
            analogSignal = view.getIntOr("AnalogSignal", 0);
            tank.setCapacity(CAPACITY);
            tank.read(view);
            fuelDebt = 0;
        }
        if (clientPacket && (!Objects.equals(before, controller)) && hasLevel())
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
    }
}
