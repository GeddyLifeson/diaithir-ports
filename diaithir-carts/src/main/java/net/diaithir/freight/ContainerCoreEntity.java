package net.diaithir.freight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import org.jspecify.annotations.Nullable;

/** Type and orientation of a deployed container, plus the tank of a tank container. The cargo itself is the blocks standing inside; nothing is stored twice. */
public final class ContainerCoreEntity extends BlockEntity {
    private String typeId = "container_20";
    private Direction facing = Direction.NORTH;
    private @Nullable Tank tank;

    final class Tank extends FluidStacksResourceHandler {
        Tank(int litres) { super(1, litres * 1000); }

        @Override protected void onContentsChanged(int index, FluidStack previous) { setChanged(); }
    }

    public ContainerCoreEntity(BlockPos pos, BlockState state) { super(Freight.CORE_BE.get(), pos, state); }

    public void init(LoadType type, Direction facing) {
        this.typeId = type.id();
        this.facing = facing;
        this.tank = type.fluidL() > 0 ? new Tank(type.fluidL()) : null;
        setChanged();
    }

    public LoadType type() { return LoadType.byId(typeId); }
    public Direction facing() { return facing; }
    public @Nullable ResourceHandler<FluidResource> tank() { return tank; }
    public long fluidMb() { return tank == null ? 0 : tank.getAmountAsLong(0); }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("type", typeId);
        out.putInt("facing", facing.get2DDataValue());
        if (tank != null) tank.serialize(out.child("tank"));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        typeId = in.getStringOr("type", "container_20");
        facing = Direction.from2DDataValue(in.getIntOr("facing", 0));
        LoadType t = type();
        tank = t != null && t.fluidL() > 0 ? new Tank(t.fluidL()) : null;
        if (tank != null) in.child("tank").ifPresent(tank::deserialize);
    }
}
