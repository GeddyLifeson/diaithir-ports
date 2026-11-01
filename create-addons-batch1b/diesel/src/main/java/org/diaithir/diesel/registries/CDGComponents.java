package org.diaithir.diesel.registries;

import com.mojang.serialization.Codec;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.diaithir.diesel.CreateDieselGenerators;

public class CDGComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CreateDieselGenerators.MODID);

    /** Fluid held by a canister item; carried across place and break by the block entity's implicit components. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FluidStack>> FLUID_CONTENTS = COMPONENTS.registerComponentType("fluid_contents",
        b -> b.persistent(FluidStack.CODEC).networkSynchronized(FluidStack.PACKET_CODEC));
}
