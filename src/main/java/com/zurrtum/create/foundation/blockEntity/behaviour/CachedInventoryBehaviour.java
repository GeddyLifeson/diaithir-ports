package com.zurrtum.create.foundation.blockEntity.behaviour;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public class CachedInventoryBehaviour<T extends SmartBlockEntity> extends BlockEntityBehaviour<T> {
    public static final BehaviourType<CachedInventoryBehaviour<?>> TYPE = new BehaviourType<>();
    private final Function<T, @Nullable Container> factory;
    private @Nullable Container cached;

    public CachedInventoryBehaviour(T be, Function<T, @Nullable Container> factory) {
        super(be);
        this.factory = factory;
    }

    public static @Nullable <T extends SmartBlockEntity> Container get(T be, @Nullable Direction side) {
        CachedInventoryBehaviour<?> behaviour = be.getBehaviour(TYPE);
        return behaviour == null ? null : behaviour.get(side);
    }

    @Nullable
    public Container get(@Nullable Direction side) {
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
