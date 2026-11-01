package net.diaithir.bridges;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;

/** One-fluid tank, 16 buckets, that only takes fluids in the tag diaithirbridges:reservoir (lava and every c:molten_* metal). */
public final class ReservoirEntity extends BlockEntity {
    static final TagKey<Fluid> ACCEPTS = TagKey.create(Registries.FLUID, Identifier.parse("diaithirbridges:reservoir"));
    static final int CAPACITY = 16_000;

    private final Tank tank = new Tank();

    ReservoirEntity(BlockPos pos, BlockState state) { super(DiaithirBridges.RESERVOIR_ENTITY.get(), pos, state); }

    private final class Tank extends FluidStacksResourceHandler {
        Tank() { super(1, CAPACITY); }
        @Override public boolean isValid(int index, FluidResource resource) { return resource.typeHolder().is(ACCEPTS); }
        @Override protected void onContentsChanged(int index, FluidStack previous) { setChanged(); }
    }

    FluidStacksResourceHandler tank() { return tank; }

    String describe() {
        FluidStack s = tank.copyToList().get(0);
        return s.isEmpty() ? "Empty, takes molten metal and lava (16000 mB)" : s.getHoverName().getString() + " " + s.getAmount() + " / " + CAPACITY + " mB";
    }

    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        tank.serialize(out.child("tank"));
    }

    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.child("tank").ifPresent(tank::deserialize);
    }
}
