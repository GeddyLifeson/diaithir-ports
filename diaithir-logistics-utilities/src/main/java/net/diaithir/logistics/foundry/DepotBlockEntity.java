package net.diaithir.logistics.foundry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * The depot. Stock is every inventory touching it. Each run it lands couriers that have arrived, then sends free couriers
 * (up to couriersPerDepot) to the nearest unclaimed ghosts in reach whose item the stock can supply. Couriers are tracked as jobs, not entities.
 */
public final class DepotBlockEntity extends BlockEntity {
    /** One courier trip. done = the block was built (or the item returned), the courier is flying home until freeAt. */
    public record Job(long ghost, ItemResource item, long arriveAt, long freeAt, boolean done, boolean demolish) {
        Job(long ghost, ItemResource item, long arriveAt, long freeAt, boolean done) {
            this(ghost, item, arriveAt, freeAt, done, false);
        }

        static final Codec<Job> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.LONG.fieldOf("ghost").forGetter(Job::ghost),
                ItemResource.CODEC.fieldOf("item").forGetter(Job::item),
                Codec.LONG.fieldOf("arriveAt").forGetter(Job::arriveAt),
                Codec.LONG.fieldOf("freeAt").forGetter(Job::freeAt),
                Codec.BOOL.fieldOf("done").forGetter(Job::done),
                Codec.BOOL.optionalFieldOf("demolish", false).forGetter(Job::demolish)).apply(i, Job::new));
    }

    private static final int MAX_CHECKS = 64;

    private final List<Job> jobs = new ArrayList<>();
    /** Blocks marked for demolition with the plan wand. A courier breaks one and brings the drops to stock (deconstruction plans). */
    private final java.util.LinkedHashSet<Long> marks = new java.util.LinkedHashSet<>();
    /** Carried by demolition jobs in place of a real item; never inserted anywhere. */
    private static final ItemResource NONE = ItemResource.of(Items.STICK);

    public boolean mark(BlockPos p) {
        boolean added = marks.add(p.asLong());
        if (added) setChanged();
        return added;
    }

    public int marked() {
        return marks.size();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) Sites.addDepot(level, worldPosition);
    }

    @Override
    public void setRemoved() {
        if (level != null) Sites.removeDepot(level, worldPosition);
        super.setRemoved();
    }

    private void giveBackStack(ServerLevel level, net.minecraft.world.item.ItemStack st) {
        ItemResource r = ItemResource.of(st);
        int left = st.getCount();
        for (ResourceHandler<ItemResource> h : stock(level)) {
            if (left <= 0) break;
            try (Transaction tx = Transaction.openRoot()) {
                int in = h.insert(r, left, tx);
                if (in > 0) {
                    tx.commit();
                    left -= in;
                }
            }
        }
        if (left > 0) level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, st.copyWithCount(left)));
    }

    private boolean demolishable(ServerLevel level, BlockPos p) {
        BlockState st = level.getBlockState(p);
        return !st.isAir() && !(st.getBlock() instanceof DepotBlock) && !(st.getBlock() instanceof GhostBlock)
                && st.getDestroySpeed(level, p) >= 0 && st.getFluidState().isEmpty();
    }

    public DepotBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirLogistics.DEPOT_BE.get(), pos, state);
    }

    public int couriersOut() {
        return jobs.size();
    }

    public List<Job> jobs() {
        return jobs;
    }

    private List<ResourceHandler<ItemResource>> stock(ServerLevel level) {
        List<ResourceHandler<ItemResource>> out = new ArrayList<>();
        for (Direction d : Direction.values()) {
            BlockPos np = worldPosition.relative(d);
            if (!level.isLoaded(np) || level.getBlockState(np).getBlock() instanceof DepotBlock) continue;
            ResourceHandler<ItemResource> h = level.getCapability(Capabilities.Item.BLOCK, np, d.getOpposite());
            if (h != null) out.add(h);
        }
        return out;
    }

    public long stockCount(ServerLevel level) {
        long n = 0;
        for (ResourceHandler<ItemResource> h : stock(level)) for (int i = 0, s = h.size(); i < s; i++) n += h.getAmountAsLong(i);
        return n;
    }

    private boolean take(List<ResourceHandler<ItemResource>> stock, ItemResource r) {
        try (Transaction tx = Transaction.openRoot()) {
            for (ResourceHandler<ItemResource> h : stock) {
                if (h.extract(r, 1, tx) == 1) {
                    tx.commit();
                    return true;
                }
            }
        }
        return false;
    }

    private void giveBack(ServerLevel level, ItemResource r) {
        try (Transaction tx = Transaction.openRoot()) {
            for (ResourceHandler<ItemResource> h : stock(level)) {
                if (h.insert(r, 1, tx) == 1) {
                    tx.commit();
                    return;
                }
            }
        }
        level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, r.toStack(1)));
    }

    private static double trip(BlockPos a, BlockPos b) {
        return Math.sqrt(a.distSqr(b)) / Config.COURIER_SPEED.get();
    }

    /** One run at game time now. Public so the self test can drive time. */
    public void work(ServerLevel level, long now) {
        // 1. couriers that reached their ghost, and couriers that are home again
        boolean changed = false;
        for (int i = 0; i < jobs.size(); i++) {
            Job j = jobs.get(i);
            if (!j.done() && now >= j.arriveAt()) {
                BlockPos gp = BlockPos.of(j.ghost());
                if (!level.isLoaded(gp)) continue;
                if (j.demolish()) {
                    if (demolishable(level, gp)) {
                        BlockState st = level.getBlockState(gp);
                        for (net.minecraft.world.item.ItemStack d : net.minecraft.world.level.block.Block.getDrops(st, level, gp, level.getBlockEntity(gp), null, net.minecraft.world.item.ItemStack.EMPTY)) giveBackStack(level, d);
                        level.destroyBlock(gp, false);
                    }
                } else if (level.getBlockEntity(gp) instanceof GhostBlockEntity g && level.getBlockState(gp).getBlock() instanceof GhostBlock) {
                    BlockState target = g.target();
                    level.setBlock(gp, target, 3);
                    level.playSound(null, gp, target.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.8F);
                } else {
                    giveBack(level, j.item());
                }
                jobs.set(i, new Job(j.ghost(), j.item(), j.arriveAt(), j.freeAt(), true, j.demolish()));
                changed = true;
            }
        }
        for (Iterator<Job> it = jobs.iterator(); it.hasNext(); ) {
            Job j = it.next();
            if (j.done() && now >= j.freeAt()) {
                it.remove();
                changed = true;
            }
        }
        // 2. send free couriers
        int free = Config.COURIERS_PER_DEPOT.get() - jobs.size();
        if (free > 0) {
            List<ResourceHandler<ItemResource>> stock = stock(level);
            if (!stock.isEmpty()) {
                double r2 = Math.pow(Config.SERVICE_RADIUS.get(), 2);
                List<BlockPos> near = new ArrayList<>();
                for (BlockPos p : Sites.ghosts(level)) if (p.distSqr(worldPosition) <= r2) near.add(p);
                near.sort(Comparator.comparingDouble(p -> p.distSqr(worldPosition)));
                int checks = 0;
                for (BlockPos gp : near) {
                    if (free <= 0 || checks++ >= MAX_CHECKS) break;
                    if (!level.isLoaded(gp) || !(level.getBlockEntity(gp) instanceof GhostBlockEntity g) || g.claimed(now)) continue;
                    Item item = g.target().getBlock().asItem();
                    if (item == Items.AIR) continue;
                    ItemResource r = ItemResource.of(item);
                    if (!take(stock, r)) continue;
                    long leg = (long) Math.ceil(trip(worldPosition, gp)) + 4;
                    g.claim(now + leg * 2 + Config.CLAIM_TIMEOUT.get());
                    jobs.add(new Job(gp.asLong(), r, now + leg, now + leg * 2, false));
                    free--;
                    changed = true;
                }
            }
        }
        // 2b. demolition marks go to free couriers too; no stock is needed, the drops come back to stock
        free = Config.COURIERS_PER_DEPOT.get() - jobs.size();
        for (Iterator<Long> it = marks.iterator(); it.hasNext() && free > 0; ) {
            BlockPos mp = BlockPos.of(it.next());
            if (mp.distSqr(worldPosition) > Math.pow(Config.SERVICE_RADIUS.get(), 2) || !level.isLoaded(mp) || !demolishable(level, mp)) {
                if (level.isLoaded(mp) && !demolishable(level, mp)) { it.remove(); changed = true; }
                continue;
            }
            it.remove();
            long leg = (long) Math.ceil(trip(worldPosition, mp)) + 4;
            jobs.add(new Job(mp.asLong(), NONE, now + leg, now + leg * 2, false, true));
            free--;
            changed = true;
        }
        // 3. a trail of sparks along each courier's path
        for (Job j : jobs) {
            long leg = Math.max(1, j.freeAt() - j.arriveAt());
            double t = Math.min(1.0, Math.max(0.0, (now - (j.arriveAt() - leg)) / (2.0 * leg)));
            double f = t <= 0.5 ? t * 2 : (1 - t) * 2;
            BlockPos gp = BlockPos.of(j.ghost());
            level.sendParticles(ParticleTypes.END_ROD,
                    worldPosition.getX() + 0.5 + (gp.getX() - worldPosition.getX()) * f,
                    worldPosition.getY() + 1.0 + (gp.getY() - worldPosition.getY()) * f,
                    worldPosition.getZ() + 0.5 + (gp.getZ() - worldPosition.getZ()) * f, 1, 0, 0, 0, 0);
        }
        if (changed) setChanged();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel sl) dropCargo(sl);
    }

    /** Called when the depot is broken: couriers in flight drop what they carry. */
    void dropCargo(ServerLevel level) {
        for (Job j : jobs) {
            if (!j.done() && !j.demolish()) {
                level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, j.item().toStack(1)));
                BlockPos gp = BlockPos.of(j.ghost());
                if (level.isLoaded(gp) && level.getBlockEntity(gp) instanceof GhostBlockEntity g) g.release();
            }
        }
        jobs.clear();
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.store("jobs", Job.CODEC.listOf(), new ArrayList<>(jobs));
        out.store("marks", Codec.LONG.listOf(), new ArrayList<>(marks));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        jobs.clear();
        in.read("jobs", Job.CODEC.listOf()).ifPresent(jobs::addAll);
        marks.clear();
        in.read("marks", Codec.LONG.listOf()).ifPresent(marks::addAll);
    }
}
