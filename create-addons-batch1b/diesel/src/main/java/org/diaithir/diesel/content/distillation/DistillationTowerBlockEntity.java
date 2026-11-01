package org.diaithir.diesel.content.distillation;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.diaithir.diesel.registries.CDGBlockEntityTypes;

import java.util.List;

/**
 * The bottom segment takes crude oil and, when the block under it is a lit burner, splits it. The first segment above takes diesel,
 * the second gasoline; pipes draw each fraction off its own segment. A tower of fewer than three segments does nothing.
 */
public class DistillationTowerBlockEntity extends SmartBlockEntity {
    public static final int CAPACITY = 4000 * 81;
    /** Per cycle: crude in, then each fraction out, in mB. */
    public static final int CRUDE_IN = 100;
    public static final List<Fraction> FRACTIONS = List.of(new Fraction("diesel", 50), new Fraction("gasoline", 25));
    private static final int CYCLE = 40;

    public record Fraction(String fluid, int mb) {
    }

    public SmartFluidTankBehaviour tank;
    private int cooldown;

    public DistillationTowerBlockEntity(BlockPos pos, BlockState state) {
        super(CDGBlockEntityTypes.DISTILLATION_TOWER.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, CAPACITY);
        behaviours.add(tank);
    }

    private boolean isTower(BlockPos p) {
        return level.getBlockState(p).getBlock() == getBlockState().getBlock();
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide() || isTower(worldPosition.below())) return;
        if (--cooldown > 0) return;
        cooldown = CYCLE;
        BlazeBurnerBlock.HeatLevel heat = BasinBlockEntity.getHeatLevelOf(level.getBlockState(worldPosition.below()));
        if (heat == BlazeBurnerBlock.HeatLevel.NONE) return;
        FluidStack in = tank.getPrimaryHandler().getFluid();
        int need = CRUDE_IN * 81;
        if (in.isEmpty() || in.getAmount() < need || !BuiltInRegistries.FLUID.getKey(in.getFluid()).getPath().equals("crude_oil")) return;
        List<DistillationTowerBlockEntity> segs = new java.util.ArrayList<>();
        BlockPos at = worldPosition;
        for (int i = 0; i <= FRACTIONS.size(); i++, at = at.above()) {
            if (!isTower(at) || !(level.getBlockEntity(at) instanceof DistillationTowerBlockEntity s)) return;
            segs.add(s);
        }
        for (int i = 0; i < FRACTIONS.size(); i++) {
            Fraction f = FRACTIONS.get(i);
            Fluid fluid = BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("createdieselgenerators", f.fluid()));
            FluidStack out = segs.get(i + 1).tank.getPrimaryHandler().getFluid();
            int add = f.mb() * 81;
            if (!out.isEmpty() && (out.getFluid() != fluid || out.getAmount() + add > CAPACITY)) return;
            if (out.isEmpty() && add > CAPACITY) return;
        }
        in.decrement(need);
        if (in.isEmpty()) tank.getPrimaryHandler().setFluid(FluidStack.EMPTY);
        tank.getPrimaryHandler().markDirty();
        for (int i = 0; i < FRACTIONS.size(); i++) {
            Fraction f = FRACTIONS.get(i);
            Fluid fluid = BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("createdieselgenerators", f.fluid()));
            var h = segs.get(i + 1).tank.getPrimaryHandler();
            FluidStack out = h.getFluid();
            if (out.isEmpty()) h.setFluid(new FluidStack(fluid, f.mb() * 81));
            else out.increment(f.mb() * 81);
            h.markDirty();
        }
    }
}
