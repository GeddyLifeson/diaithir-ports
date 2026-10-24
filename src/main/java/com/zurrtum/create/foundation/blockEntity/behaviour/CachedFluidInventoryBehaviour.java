package com.zurrtum.create.foundation.blockEntity.behaviour;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public class CachedFluidInventoryBehaviour<T extends SmartBlockEntity> extends BlockEntityBehaviour<T> {
    public static final BehaviourType<CachedFluidInventoryBehaviour<?>> TYPE = new BehaviourType<>();
    private final Function<T, @Nullable FluidInventory> factory;
    private @Nullable FluidInventory cached;

    public CachedFluidInventoryBehaviour(T be, Function<T, @Nullable FluidInventory> factory) {
        super(be);
        this.factory = factory;
    }

    public static @Nullable <T extends SmartBlockEntity> FluidInventory get(T be, @Nullable Direction side) {
        CachedFluidInventoryBehaviour<?> behaviour = be.getBehaviour(TYPE);
        return behaviour == null ? null : behaviour.get(side);
    }

    @Nullable
    public FluidInventory get(@Nullable Direction side) {
        if (cached == null) {
            cached = factory.apply(blockEntity);
        }
        return cached;
    }

    public void reset() {
        cached = null;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
