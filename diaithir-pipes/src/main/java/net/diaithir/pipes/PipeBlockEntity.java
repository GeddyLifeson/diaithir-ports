package net.diaithir.pipes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Per-pipe state: face modes, redstone mode, distribution, filter and link. Does the moving for its extract faces. */
public final class PipeBlockEntity extends BlockEntity {
    private final FaceMode[] modes = new FaceMode[6];
    private RedstoneMode redstone = RedstoneMode.ALWAYS;
    private Distribution distribution = Distribution.ROUND_ROBIN;
    private final ItemFilter filter = new ItemFilter();
    private BlockPos link;
    private int cursor;
    private int cooldown;

    public PipeBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirPipes.PIPE_BE.get(), pos, state);
        for (int i = 0; i < 6; i++) modes[i] = FaceMode.BOTH;
    }

    PipeBlock2 pipe() {
        return (PipeBlock2) getBlockState().getBlock();
    }

    public FaceMode mode(Direction d) {
        return modes[d.ordinal()];
    }

    public void setMode(Direction d, FaceMode m) {
        modes[d.ordinal()] = m;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public RedstoneMode redstone() {
        return redstone;
    }

    public void cycleRedstone() {
        redstone = redstone.next();
        setChanged();
    }

    public Distribution distribution() {
        return distribution;
    }

    public void cycleDistribution() {
        distribution = distribution.next();
        setChanged();
    }

    public ItemFilter filter() {
        return filter;
    }

    public BlockPos link() {
        return link;
    }

    public void setLink(BlockPos p) {
        link = p;
        setChanged();
    }

    public void serverTick(ServerLevel level) {
        if (++cooldown < Config.TICKS_PER_RUN.get()) return;
        cooldown = 0;
        run(level);
    }

    /** Runs every extract face once. */
    void run(ServerLevel level) {
        PipeBlock2 p = pipe();
        boolean powered = level.hasNeighborSignal(worldPosition);
        if (!redstone.allows(powered)) return;
        long budget = p.tier().perRun(p.kind());
        for (Direction d : Direction.values()) {
            if (budget <= 0) return;
            if (!mode(d).extracts()) continue;
            BlockPos src = worldPosition.relative(d);
            if (!level.isLoaded(src) || level.getBlockState(src).getBlock() instanceof PipeBlock2) continue;
            budget -= pushFrom(level, p.kind(), d, budget);
        }
    }

    private record Target(BlockPos pos, Direction side, int distance) {
    }

    /** Walks the connected pipes of the same kind and collects every insert-capable endpoint, with its pipe distance. */
    private List<Target> targets(ServerLevel level, PipeKind kind, Direction origin) {
        List<Target> out = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        List<BlockPos> frontier = new ArrayList<>();
        frontier.add(worldPosition);
        seen.add(worldPosition);
        int depth = 0;
        int limit = Config.MAX_NETWORK_PIPES.get();
        while (!frontier.isEmpty() && seen.size() <= limit) {
            List<BlockPos> next = new ArrayList<>();
            for (BlockPos cur : frontier) {
                if (!(level.getBlockEntity(cur) instanceof PipeBlockEntity pe)) continue;
                for (Direction d : Direction.values()) {
                    FaceMode m = pe.mode(d);
                    if (m == FaceMode.DISCONNECTED) continue;
                    BlockPos np = cur.relative(d);
                    if (!level.isLoaded(np)) continue;
                    if (level.getBlockState(np).getBlock() instanceof PipeBlock2 other) {
                        if (other.kind() == kind && seen.add(np)) next.add(np);
                    } else if (m.inserts() && !(cur.equals(worldPosition) && d == origin)) {
                        out.add(new Target(np, d.getOpposite(), depth));
                    }
                }
                if (pe.link != null && level.isLoaded(pe.link)) {
                    if (level.getBlockEntity(pe.link) instanceof PipeBlockEntity other && other.pipe().kind() == kind && seen.add(pe.link)) next.add(pe.link);
                }
            }
            frontier = next;
            depth++;
        }
        return out;
    }

    private long pushFrom(ServerLevel level, PipeKind kind, Direction face, long budget) {
        BlockPos srcPos = worldPosition.relative(face);
        List<Target> ts = targets(level, kind, face);
        if (ts.isEmpty()) return 0;
        int[] dist = new int[ts.size()];
        for (int i = 0; i < dist.length; i++) dist[i] = ts.get(i).distance();
        List<Integer> order = Distribution.order(distribution, dist, cursor);
        long moved = 0;
        int served = -1;
        for (int i : order) {
            if (moved >= budget) break;
            Target t = ts.get(i);
            long m = moveOne(level, kind, srcPos, face.getOpposite(), t, budget - moved);
            if (m > 0) {
                moved += m;
                served = i;
            }
        }
        if (served >= 0) cursor = served + 1;
        return moved;
    }

    private long moveOne(ServerLevel level, PipeKind kind, BlockPos srcPos, Direction srcSide, Target t, long budget) {
        switch (kind) {
            case ITEM -> {
                ResourceHandler<ItemResource> s = level.getCapability(Capabilities.Item.BLOCK, srcPos, srcSide);
                ResourceHandler<ItemResource> d = level.getCapability(Capabilities.Item.BLOCK, t.pos(), t.side());
                return s == null || d == null ? 0 : PipeIo.moveItems(s, d, budget, filter::allows);
            }
            case FLUID -> {
                ResourceHandler<FluidResource> s = level.getCapability(Capabilities.Fluid.BLOCK, srcPos, srcSide);
                ResourceHandler<FluidResource> d = level.getCapability(Capabilities.Fluid.BLOCK, t.pos(), t.side());
                return s == null || d == null ? 0 : PipeIo.moveFluid(s, d, budget, r -> true);
            }
            default -> {
                EnergyHandler s = level.getCapability(Capabilities.Energy.BLOCK, srcPos, srcSide);
                EnergyHandler d = level.getCapability(Capabilities.Energy.BLOCK, t.pos(), t.side());
                return s == null || d == null ? 0 : PipeIo.moveEnergy(s, d, budget);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        int[] m = new int[6];
        for (int i = 0; i < 6; i++) m[i] = modes[i].ordinal();
        out.putIntArray("modes", m);
        out.putInt("redstone", redstone.ordinal());
        out.putInt("distribution", distribution.ordinal());
        out.putBoolean("blacklist", filter.blacklist());
        out.store("filter", ItemResource.CODEC.listOf(), new ArrayList<>(filter.items()));
        if (link != null) out.store("link", BlockPos.CODEC, link);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.getIntArray("modes").ifPresent(m -> {
            for (int i = 0; i < 6 && i < m.length; i++) modes[i] = FaceMode.byOrdinal(m[i]);
        });
        redstone = RedstoneMode.byOrdinal(in.getIntOr("redstone", 0));
        distribution = Distribution.byOrdinal(in.getIntOr("distribution", 0));
        filter.setBlacklist(in.getBooleanOr("blacklist", false));
        filter.items().clear();
        in.read("filter", ItemResource.CODEC.listOf()).ifPresent(filter.items()::addAll);
        link = in.read("link", BlockPos.CODEC).orElse(null);
    }
}
