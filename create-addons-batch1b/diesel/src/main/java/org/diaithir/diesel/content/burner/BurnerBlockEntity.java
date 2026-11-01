package org.diaithir.diesel.content.burner;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.diaithir.diesel.Fuels;
import org.diaithir.diesel.registries.CDGBlockEntityTypes;

import java.util.List;

public class BurnerBlockEntity extends KineticBlockEntity {
    public static final int CAPACITY = 100 * 81;
    public SmartFluidTankBehaviour tank;
    public float heat = -1;
    public int redstoneOutput;
    public int redstonePower;
    public float valveState = 0.2f;
    private int tick;
    private int ignitionTries;
    private boolean ignited;

    public BurnerBlockEntity(BlockPos pos, BlockState state) {
        super(CDGBlockEntityTypes.BURNER.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        tank = SmartFluidTankBehaviour.single(this, CAPACITY);
        behaviours.add(tank);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide()) return;
        tick = (tick + 1) & 0xFFFF;
        valveState = Mth.clamp(valveState + getSpeed() / 5000f, 0, 1);
        float valve = Math.max(valveState, redstonePower * 4);
        FluidStack fluid = tank.getPrimaryHandler().getFluid();
        Fuels.Fuel fuel = fluid.isEmpty() ? null : Fuels.of(fluid.getFluid());
        float mult = fuel == null ? 0 : fuel.burnerMultiplier();
        if (valveState == 0 && redstonePower == 0 || mult == 0) {
            heat = -1;
            if (ignited) level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3f, 0.9f);
            ignited = false;
            ignitionTries = 0;
        } else {
            heat = (valve + 1) * mult;
            if (tick % 5 == 0 && !ignited) {
                level.playSound(null, worldPosition, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.5f, 1f);
                if (++ignitionTries > 4) ignited = true;
            }
            if (ignited && (int) (tick % Math.max(1, 10 / valve)) == 0) {
                fluid.decrement(81);
                if (fluid.isEmpty()) tank.getPrimaryHandler().setFluid(FluidStack.EMPTY);
                tank.getPrimaryHandler().markDirty();
                sendData();
                setChanged();
            }
        }
        BlazeBurnerBlock.HeatLevel hl = ignited ? heatLevel(heat) : BlazeBurnerBlock.HeatLevel.NONE;
        BlockState st = getBlockState();
        if (st.getValue(BurnerBlock.HEAT_LEVEL) != hl || st.getValue(BurnerBlock.LIT) != (hl != BlazeBurnerBlock.HeatLevel.NONE)) {
            level.setBlockAndUpdate(worldPosition, st.setValue(BurnerBlock.HEAT_LEVEL, hl).setValue(BurnerBlock.LIT, hl != BlazeBurnerBlock.HeatLevel.NONE));
            notifyUpdate();
        }
        int out = (int) Mth.clamp(heat * 6, 0, 15);
        if (out != redstoneOutput) {
            redstoneOutput = out;
            setChanged();
        }
    }

    public static BlazeBurnerBlock.HeatLevel heatLevel(float heat) {
        if (heat >= 1.8f) return BlazeBurnerBlock.HeatLevel.SEETHING;
        if (heat >= 1.4f) return BlazeBurnerBlock.HeatLevel.KINDLED;
        if (heat >= 1.2f) return BlazeBurnerBlock.HeatLevel.FADING;
        if (heat >= 1f) return BlazeBurnerBlock.HeatLevel.SMOULDERING;
        return BlazeBurnerBlock.HeatLevel.NONE;
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putFloat("ValveState", valveState);
        view.putInt("RedstonePower", redstonePower);
        view.putInt("RedstoneOutput", redstoneOutput);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        valveState = view.getFloatOr("ValveState", 0.2f);
        redstonePower = view.getIntOr("RedstonePower", 0);
        redstoneOutput = view.getIntOr("RedstoneOutput", 0);
    }
}
