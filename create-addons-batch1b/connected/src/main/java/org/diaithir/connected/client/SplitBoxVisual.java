package org.diaithir.connected.client;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.AbstractInstance;
import com.zurrtum.create.client.flywheel.lib.instance.FlatLit;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.kinetics.transmission.SplitShaftBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/** Shaft stubs on the four side faces of a gearbox, each turning at the face's own speed modifier. */
public class SplitBoxVisual<T extends SplitShaftBlockEntity> extends KineticBlockEntityVisual<T> {
    public interface Modifier {
        float of(BlockState state, Direction face, Direction source);
    }

    protected final EnumMap<Direction, RotatingInstance> keys = new EnumMap<>(Direction.class);
    protected @Nullable Direction sourceFacing;
    private final Modifier modifier;

    public SplitBoxVisual(VisualizationContext context, T blockEntity, float partialTick, Modifier modifier) {
        super(context, blockEntity, partialTick);
        this.modifier = modifier;
        Direction.Axis boxAxis = blockState.getValue(BlockStateProperties.AXIS);
        updateSourceFacing();
        var instancer = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.chunkPartial(AllPartialModels.SHAFT_HALF));
        for (Direction direction : Iterate.directions) {
            Direction.Axis axis = direction.getAxis();
            if (boxAxis == axis) continue;
            RotatingInstance instance = instancer.createInstance();
            instance.setup(blockEntity, axis, speed(direction)).setPosition(getVisualPosition())
                .rotateToFace(Direction.SOUTH, direction).setChanged();
            keys.put(direction, instance);
        }
    }

    private float speed(Direction direction) {
        float speed = blockEntity.getSpeed();
        if (speed != 0 && sourceFacing != null) speed *= modifier.of(blockState, direction, sourceFacing);
        return speed;
    }

    protected void updateSourceFacing() {
        if (blockEntity.hasSource()) {
            BlockPos source = blockEntity.source.subtract(pos);
            sourceFacing = Direction.getApproximateNearest(source.getX(), source.getY(), source.getZ());
        } else {
            sourceFacing = null;
        }
    }

    @Override
    public void update(float pt) {
        updateSourceFacing();
        for (Map.Entry<Direction, RotatingInstance> key : keys.entrySet()) {
            Direction direction = key.getKey();
            key.getValue().setup(blockEntity, direction.getAxis(), speed(direction)).setChanged();
        }
    }

    @Override
    public void updateLight(float partialTick) {
        relight(keys.values().toArray(FlatLit[]::new));
    }

    @Override
    protected void _delete() {
        keys.values().forEach(AbstractInstance::delete);
        keys.clear();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        keys.values().forEach(consumer);
    }
}
