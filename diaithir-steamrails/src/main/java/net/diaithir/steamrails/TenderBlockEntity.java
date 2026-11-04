package net.diaithir.steamrails;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedFluidInventoryBehaviour;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;

public final class TenderBlockEntity extends SmartBlockEntity {
    public static final String KEY = "Tender";

    /** Pipes and tank carts fill water through this; the buffer drains into the tender next tick. */
    public final class Intake extends FluidStacksResourceHandler {
        Intake() {
            super(1, 4000);
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return resource.getFluid() == Fluids.WATER && super.isValid(index, resource);
        }
    }

    public TenderState state = new TenderState();
    public final Intake intake = new Intake();

    /** What Create pipes, tanks and spouts fill. */
    public final SteamIntake createIntake = new SteamIntake(this::setChanged);

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(new CachedFluidInventoryBehaviour<>(this, be -> be.createIntake));
    }

    public TenderBlockEntity(BlockPos pos, BlockState blockState) {
        super(SteamBlocks.TENDER_BE.get(), pos, blockState);
    }

    /** Called by the capability users every so often. */
    public void drainIntake() {
        int a = intake.getAmountAsInt(0);
        if (a > 0) {
            int n = state.addWater(a);
            intake.set(0, FluidResource.EMPTY, a - n);
            setChanged();
        }
        int mb = createIntake.millibuckets();
        if (mb > 0) {
            createIntake.take(state.addWater(mb));
            setChanged();
        }
    }

    /** Create saves only this for a block on a carriage, so the numbers must be in it. */
    @Override
    public void writeSafe(ValueOutput out) {
        super.writeSafe(out);
        out.store(KEY, TenderState.CODEC, state);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.store(KEY, TenderState.CODEC, state);
        intake.serialize(out.child("intake"));
        createIntake.save(out.child("createIntake"));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        state = in.read(KEY, TenderState.CODEC).orElseGet(TenderState::new);
        in.child("intake").ifPresent(intake::deserialize);
        in.child("createIntake").ifPresent(createIntake::load);
    }

    /** Server tick, run through the ticker of the block. */
    public static void tick(Level level, BlockPos pos, BlockState st, TenderBlockEntity be) {
        if (level.isClientSide()) return;
        be.tick();
        if ((level.getGameTime() + pos.asLong()) % 10 == 0) be.drainIntake();
    }

    public ItemStack[] fuel() {
        return state.fuel;
    }
}
