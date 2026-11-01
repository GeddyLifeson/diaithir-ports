package org.diaithir.diesel.content.pumpjack;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import org.diaithir.diesel.registries.CDGBlockEntityTypes;
import org.diaithir.diesel.registries.CDGFluids;
import org.diaithir.diesel.world.OilReserves;

import java.util.List;

public class PumpjackBlockEntity extends KineticBlockEntity {
    public static final int CAPACITY = 4000 * 81;
    /** mB drawn per second at 64 RPM. */
    public static final int RATE = 200;
    public SmartFluidTankBehaviour tank;
    private int timer;

    public PumpjackBlockEntity(BlockPos pos, BlockState state) {
        super(CDGBlockEntityTypes.PUMPJACK.get(), pos, state);
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
        if (!(level instanceof ServerLevel sl) || Math.abs(getSpeed()) < 8) return;
        if (++timer < 20) return;
        timer = 0;
        FluidStack held = tank.getPrimaryHandler().getFluid();
        int space = CAPACITY - held.getAmount();
        if (space <= 0 || (!held.isEmpty() && held.getFluid() != CDGFluids.ENTRIES.get("crude_oil").still)) return;
        int want = Math.min(space / 81, (int) (RATE * Math.abs(getSpeed()) / 64f));
        int got = OilReserves.get(sl).draw(ChunkPos.containing(worldPosition), want);
        if (got <= 0) return;
        if (held.isEmpty()) tank.getPrimaryHandler().setFluid(new FluidStack(CDGFluids.ENTRIES.get("crude_oil").still, got * 81));
        else held.increment(got * 81);
        tank.getPrimaryHandler().markDirty();
        sendData();
    }
}
