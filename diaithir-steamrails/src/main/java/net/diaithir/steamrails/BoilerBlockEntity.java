package net.diaithir.steamrails;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedFluidInventoryBehaviour;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import org.jspecify.annotations.Nullable;

import java.util.function.ToIntFunction;

/** The brain of a boiler standing in the world: ticks {@link BoilerState}, draws from tenders, smokes. A boiler riding a train is ticked by {@link BoilerMovement} instead. */
public final class BoilerBlockEntity extends SmartBlockEntity {
    public static final String KEY = "Boiler";

    public final class Intake extends FluidStacksResourceHandler {
        Intake() {
            super(1, 4000);
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return resource.getFluid() == Fluids.WATER && super.isValid(index, resource);
        }
    }

    public BoilerState state = new BoilerState();
    public final Intake intake = new Intake();

    /** What Create pipes, tanks and spouts fill. */
    public final SteamIntake createIntake = new SteamIntake(this::setChanged);

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(new CachedFluidInventoryBehaviour<>(this, be -> be.createIntake));
    }
    private @Nullable Layout layout;
    private long layoutAt = -1000;
    private float extraLoad;

    public BoilerBlockEntity(BlockPos pos, BlockState blockState) {
        super(SteamBlocks.BOILER_BE.get(), pos, blockState);
    }

    /** The active firebox of the boiler that contains {@code any}, or null. */
    public static @Nullable BoilerBlockEntity masterOf(Level level, BlockPos any) {
        Layout l = Layout.scan(level::getBlockState, any);
        if (l == null) return null;
        return level.getBlockEntity(l.master) instanceof BoilerBlockEntity b ? b : null;
    }

    public @Nullable Layout layout(Level level) {
        if (layout == null || level.getGameTime() - layoutAt >= 20) {
            layout = Layout.scan(level::getBlockState, worldPosition);
            layoutAt = level.getGameTime();
        }
        return layout;
    }

    public void invalidateLayout() {
        layout = null;
    }

    public void changed() {
        setChanged();
    }

    /**
     * Takes steam for a whistle or a test. Refused below {@code minPressure}. The draw is applied on the next tick.
     */
    public boolean draw(float steam, float minPressure) {
        if (state.pressure < minPressure) return false;
        extraLoad += steam;
        return true;
    }

    /** Create saves only this for a block on a carriage, so the numbers must be in it. */
    @Override
    public void writeSafe(ValueOutput out) {
        super.writeSafe(out);
        out.store(KEY, BoilerState.CODEC, state);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.store(KEY, BoilerState.CODEC, state);
        intake.serialize(out.child("intake"));
        createIntake.save(out.child("createIntake"));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        state = in.read(KEY, BoilerState.CODEC).orElseGet(BoilerState::new);
        in.child("intake").ifPresent(intake::deserialize);
        in.child("createIntake").ifPresent(createIntake::load);
    }

    public void serverTick(ServerLevel level) {
        tick();
        Layout l = layout(level);
        if (l == null) return;
        BlockState bs = getBlockState();
        if (!l.master.equals(worldPosition)) {
            if (bs.getValue(BoilerBlock.LIT)) level.setBlock(worldPosition, bs.setValue(BoilerBlock.LIT, false), 3);
            return;
        }
        long t = level.getGameTime();
        ToIntFunction<net.minecraft.world.item.ItemStack> burn = Hand.burn(level);
        if (t % 10 == 0) {
            int a = intake.getAmountAsInt(0);
            if (a > 0) {
                int n = state.addWater(a, l.waterCapacity());
                intake.set(0, FluidResource.EMPTY, a - n);
            }
            int mb = createIntake.millibuckets();
            if (mb > 0) createIntake.take(state.addWater(mb, l.waterCapacity()));
            for (BlockPos tp : l.tenderPos) {
                if (level.getBlockEntity(tp) instanceof TenderBlockEntity tb && tb.state.feed(state, l, burn)) tb.setChanged();
            }
        }
        float load = extraLoad + 0.0004F * l.cylinders;
        extraLoad = 0;
        state.tick(l, burn, load);
        boolean lit = state.burning();
        if (bs.getValue(BoilerBlock.LIT) != lit) level.setBlock(worldPosition, bs.setValue(BoilerBlock.LIT, lit), 3);
        if (lit && t % 3 == 0) {
            for (BlockPos s : l.stacks) {
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, s.getX() + 0.5, s.getY() + 1.0, s.getZ() + 0.5, 0, 0.0, 0.08, 0.0, 1.0);
            }
        }
        if (state.venting && t % 8 == 0) {
            BlockPos top = l.master.above();
            level.sendParticles(ParticleTypes.CLOUD, top.getX() + 0.5, top.getY() + 0.5, top.getZ() + 0.5, 4, 0.2, 0.1, 0.2, 0.05);
            level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.4F);
        }
        if (t % 20 == 0) setChanged();
    }
}
