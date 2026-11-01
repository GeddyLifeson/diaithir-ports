package org.diaithir.diesel.content.canister;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.diaithir.diesel.registries.CDGBlockEntityTypes;
import org.diaithir.diesel.registries.CDGComponents;

import java.util.List;

/** A portable fuel can that is also a placed tank: pipes fill and drain it, and it keeps its contents when broken. */
public class CanisterBlockEntity extends SmartBlockEntity {
    public static final int CAPACITY = 4000 * 81;
    public SmartFluidTankBehaviour tank;

    public CanisterBlockEntity(BlockPos pos, BlockState state) {
        super(CDGBlockEntityTypes.CANISTER.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, CAPACITY);
        behaviours.add(tank);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        FluidStack f = components.get(CDGComponents.FLUID_CONTENTS.get());
        if (f != null && !f.isEmpty()) tank.getPrimaryHandler().setFluid(f.copy());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        FluidStack f = tank.getPrimaryHandler().getFluid();
        if (!f.isEmpty()) builder.set(CDGComponents.FLUID_CONTENTS.get(), f.copy());
    }
}
