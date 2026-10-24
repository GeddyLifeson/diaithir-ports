package com.zurrtum.create.foundation.blockEntity.behaviour;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import org.jspecify.annotations.Nullable;

import java.util.function.BiFunction;

public class CachedDirectionInventoryBehaviour<T extends SmartBlockEntity> extends BlockEntityBehaviour<T> {
    public static final BehaviourType<CachedDirectionInventoryBehaviour<?>> TYPE = new BehaviourType<>();
    private final BiFunction<T, @Nullable Direction, @Nullable Container> factory;
    @Nullable Container[] sides = new Container[7];

    public CachedDirectionInventoryBehaviour(T be, BiFunction<T, Direction, @Nullable Container> factory) {
        super(be);
        this.factory = factory;
    }

    public static @Nullable <T extends SmartBlockEntity> Container get(T be, @Nullable Direction side) {
        CachedDirectionInventoryBehaviour<?> behaviour = be.getBehaviour(TYPE);
        return behaviour == null ? null : behaviour.get(side);
    }

    @Nullable
    public Container get(@Nullable Direction side) {
        int i = side == null ? 6 : side.get3DDataValue();
        Container inv = sides[i];
        if (inv == null) {
            inv = sides[i] = factory.apply(blockEntity, side);
        }
        return inv;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
