package net.diaithir.steamrails;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * A firebox riding a train carriage. It ticks the same {@link BoilerState} a boiler in the world does, but its numbers live in the block entity
 * data of the moving contraption (so they come back when the carriage is taken apart). Each tick it reports thrust to {@link TrainPower}.
 */
public final class BoilerMovement extends MovementBehaviour {
    static final String KEY = BoilerBlockEntity.KEY;

    /** Everything kept between ticks for one moving firebox. */
    static final class Run {
        BoilerState st;
        @Nullable Layout layout;
        long layoutAt = -1000;
        double lastSpeed;
        int sinceSave;
        boolean steamBogey;
        float lastThrust;
        float lastLoad;
    }

    @Override
    public boolean mustTickWhileDisabled() {
        return true;
    }

    static RegistryOps<Tag> ops(Level level) {
        return level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
    }

    static @Nullable Run run(MovementContext ctx) {
        if (ctx.temporaryData instanceof Run r) return r;
        Run r = new Run();
        CompoundTag nbt = ctx.blockEntityData;
        r.st = nbt == null ? new BoilerState() : nbt.read(KEY, BoilerState.CODEC, ops(ctx.world)).orElseGet(BoilerState::new);
        ctx.temporaryData = r;
        return r;
    }

    static void save(MovementContext ctx, Run r) {
        if (ctx.blockEntityData != null) ctx.blockEntityData.store(KEY, BoilerState.CODEC, ops(ctx.world), r.st);
        r.sinceSave = 0;
    }

    public static Function<BlockPos, BlockState> view(Contraption c) {
        Map<BlockPos, StructureBlockInfo> blocks = c.getBlocks();
        return p -> {
            StructureBlockInfo i = blocks.get(p);
            return i == null ? Blocks.AIR.defaultBlockState() : i.state();
        };
    }

    static boolean onSteamBogey(@Nullable Carriage c) {
        if (c == null) return false;
        return isSteam(c.leadingBogey()) || isSteam(c.trailingBogey());
    }

    private static boolean isSteam(@Nullable CarriageBogey b) {
        return b != null && b.getStyle().id.getNamespace().equals(SteamRails.ID);
    }

    @Override
    public void tick(MovementContext ctx) {
        if (!(ctx.world instanceof ServerLevel level)) return;
        Run run = run(ctx);
        if (run == null) return;
        Contraption c = ctx.contraption;
        long t = level.getGameTime();
        if (run.layout == null || t - run.layoutAt >= 20) {
            run.layout = Layout.scan(view(c), ctx.localPos);
            run.layoutAt = t;
        }
        Layout l = run.layout;
        if (l == null || !l.master.equals(ctx.localPos)) return;

        AbstractContraptionEntity ent = c.entity;
        Carriage carriage = ent instanceof CarriageContraptionEntity cce ? cce.getCarriage() : null;
        Train train = carriage == null ? null : carriage.train;
        run.steamBogey = train != null && onSteamBogey(carriage);
        ToIntFunction<ItemStack> burn = Hand.burn(level);

        if (train != null && t % 10 == 0) feed(level, train, run.st, l, burn);

        double speed = train == null ? 0 : Math.abs(train.speed);
        float load = 0.0004F * l.cylinders;
        int eff = Math.min(l.cylinders, (l.size + 1) / 2);
        if (run.steamBogey && train != null) {
            double top = Math.max(0.01, AllConfigs.server().trains.poweredTrainTopSpeed.getF() / 20.0);
            double want = Math.abs(train.targetSpeed);
            load += (float) (eff * (0.002 + SteamRails.CYLINDER_STEAM * Math.min(1.0, speed / top)
                    + (want > speed + 0.005 ? SteamRails.CYLINDER_STEAM * 0.5 : 0)));
        }
        run.lastLoad = load;
        run.st.tick(l, burn, load);
        if (run.steamBogey && train != null) {
            double th = TrainPower.thrust(run.st.pressure, l.cylinders, l.size);
            run.lastThrust = (float) th;
            TrainPower.report(train.id, th);
        } else {
            run.lastThrust = 0;
        }

        if (run.st.burning() && t % 3 == 0 && ent != null) {
            for (BlockPos s : l.stacks) {
                Vec3 g = ent.toGlobalVector(Vec3.atCenterOf(s).add(0, 0.6, 0), 1F);
                Vec3 m = train == null ? Vec3.ZERO : Vec3.ZERO;
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, g.x, g.y, g.z, 0, m.x, 0.08, m.z, 1.0);
            }
        }
        if (run.st.venting && t % 8 == 0 && ent != null) {
            Vec3 g = ent.toGlobalVector(Vec3.atCenterOf(l.master.above()), 1F);
            level.sendParticles(ParticleTypes.CLOUD, g.x, g.y, g.z, 4, 0.2, 0.1, 0.2, 0.05);
        }
        // pull out of a station: one blast from each whistle
        if (train != null && ent != null && run.lastSpeed < 0.005 && speed >= 0.02 && !l.whistlePos.isEmpty()) {
            boolean ok = run.st.pressure >= WhistleBlock.MIN_PRESSURE;
            for (BlockPos w : l.whistlePos) {
                Vec3 g = ent.toGlobalVector(Vec3.atCenterOf(w), 1F);
                if (ok) run.st.pressure = Math.max(0F, run.st.pressure - WhistleBlock.COST / Math.max(1, l.size));
                WhistleBlock.sound(level, g.x, g.y, g.z, ok, run.st.pressure / l.rating);
            }
        }
        run.lastSpeed = speed;
        if (++run.sinceSave >= 20) save(ctx, run);
    }

    /** Draws water and fuel from every tender of the train, on any carriage. */
    static void feed(ServerLevel level, Train train, BoilerState b, Layout l, ToIntFunction<ItemStack> burn) {
        if (b.water > l.waterCapacity() * 0.8F && b.fuelCount() >= 8) return;
        RegistryOps<Tag> ops = ops(level);
        for (Carriage car : train.carriages) {
            Carriage.DimensionalCarriageEntity d = car.getDimensionalIfPresent(level.dimension());
            CarriageContraptionEntity e = d == null ? null : d.entity.get();
            if (e == null || e.getContraption() == null) continue;
            for (StructureBlockInfo info : e.getContraption().getBlocks().values()) {
                if (!(info.state().getBlock() instanceof TenderBlock) || info.nbt() == null) continue;
                TenderState ts = info.nbt().read(TenderBlockEntity.KEY, TenderState.CODEC, ops).orElse(null);
                if (ts == null) continue;
                if (ts.feed(b, l, burn)) info.nbt().store(TenderBlockEntity.KEY, TenderState.CODEC, ops, ts);
            }
        }
    }

    @Override
    public void stopMoving(MovementContext ctx) {
        if (ctx.temporaryData instanceof Run r) save(ctx, r);
    }

    @Override
    public void writeExtraData(MovementContext ctx) {
        if (ctx.temporaryData instanceof Run r) save(ctx, r);
    }
}
