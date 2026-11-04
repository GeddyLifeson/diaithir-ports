package net.diaithir.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A node with a channel, a priority and a filter on each of its six faces, plus up to six laser links. A node pushes from its own faces' inventories into the faces of linked nodes that share a channel, highest priority first. */
public final class LaserNodeBlockEntity extends BlockEntity {
    public static final int CHANNELS = 16;
    public static final int MAX_PRIORITY = 9;

    private final int[] channel = new int[6];
    private final int[] priority = new int[6];
    private final FilterData[] filters = new FilterData[6];
    private final List<BlockPos> links = new ArrayList<>();
    private int cooldown;

    public LaserNodeBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirRouting.LASER_BE.get(), pos, state);
        Arrays.fill(filters, FilterData.EMPTY);
    }

    public List<BlockPos> links() {
        return links;
    }

    public void cycleChannel(Direction side) {
        channel[side.ordinal()] = (channel[side.ordinal()] + 1) % CHANNELS;
        setChanged();
    }

    public void cyclePriority(Direction side) {
        priority[side.ordinal()] = (priority[side.ordinal()] + 1) % (MAX_PRIORITY + 1);
        setChanged();
    }

    public void setFilter(Direction side, FilterData f) {
        filters[side.ordinal()] = f;
        setChanged();
    }

    public String describeSide(Direction side) {
        int i = side.ordinal();
        return side.getName() + ": channel " + channel[i] + ", priority " + priority[i] + ", " + filters[i].describe();
    }

    /** Joins this node to another if it is in range and unobstructed. Only the first node of a pair draws the beam. */
    public boolean connect(ServerLevel level, BlockPos other) {
        if (other.equals(worldPosition) || links.contains(other) || links.size() >= 6) return links.contains(other);
        if (other.distSqr(worldPosition) > (double) Config.LASER_RANGE.get() * Config.LASER_RANGE.get()) return false;
        if (!(level.getBlockEntity(other) instanceof LaserNodeBlockEntity)) return false;
        if (!lineOfSight(level, worldPosition, other)) return false;
        links.add(other);
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }

    static boolean lineOfSight(ServerLevel level, BlockPos a, BlockPos b) {
        Vec3 from = Vec3.atCenterOf(a);
        Vec3 to = Vec3.atCenterOf(b);
        var hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, net.minecraft.world.phys.shapes.CollisionContext.empty()));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(b) || hit.getBlockPos().equals(a);
    }

    public void disconnectAll(ServerLevel level) {
        for (BlockPos p : links) {
            if (level.getBlockEntity(p) instanceof LaserNodeBlockEntity o && o.links.remove(worldPosition)) {
                o.setChanged();
                level.sendBlockUpdated(p, o.getBlockState(), o.getBlockState(), 3);
            }
        }
        links.clear();
    }

    public void serverTick(ServerLevel level) {
        if (links.isEmpty() || ++cooldown < Config.OPERATION_INTERVAL.get()) return;
        cooldown = 0;
        links.removeIf(p -> !(level.isLoaded(p) && level.getBlockEntity(p) instanceof LaserNodeBlockEntity) || !lineOfSight(level, worldPosition, p));
        for (Direction side : Direction.values()) {
            BlockPos srcPos = worldPosition.relative(side);
            if (!level.isLoaded(srcPos) || level.getBlockEntity(srcPos) == null || level.getBlockEntity(srcPos) instanceof LaserNodeBlockEntity) continue;
            ResourceHandler<ItemResource> si = Transfers.items(level, srcPos, side.getOpposite());
            ResourceHandler<FluidResource> sf = Transfers.fluids(level, srcPos, side.getOpposite());
            EnergyHandler se = Transfers.energy(level, srcPos, side.getOpposite());
            if (si == null && sf == null && se == null) continue;
            int itemBudget = Config.ITEMS_PER_OPERATION.get();
            int fluidBudget = Config.FLUID_PER_OPERATION.get();
            int energyBudget = Config.ENERGY_PER_OPERATION.get();
            FilterData srcFilter = filters[side.ordinal()];
            for (Target t : targets(level, side)) {
                BlockPos dstPos = t.node.worldPosition.relative(t.side);
                FilterData dstFilter = t.node.filters[t.side.ordinal()];
                if (si != null && itemBudget > 0) {
                    ResourceHandler<ItemResource> di = Transfers.items(level, dstPos, t.side.getOpposite());
                    if (di != null) itemBudget -= Transfers.moveItems(si, di, itemBudget, s -> srcFilter.allows(s) && dstFilter.allows(s));
                }
                if (sf != null && fluidBudget > 0) {
                    ResourceHandler<FluidResource> df = Transfers.fluids(level, dstPos, t.side.getOpposite());
                    if (df != null) fluidBudget -= Transfers.moveFluid(sf, df, fluidBudget);
                }
                if (se != null && energyBudget > 0) {
                    EnergyHandler de = Transfers.energy(level, dstPos, t.side.getOpposite());
                    if (de != null) energyBudget -= Transfers.moveEnergy(se, de, energyBudget);
                }
            }
        }
    }

    private record Target(LaserNodeBlockEntity node, Direction side, int prio) {
    }

    private List<Target> targets(ServerLevel level, Direction from) {
        List<Target> out = new ArrayList<>();
        int ch = channel[from.ordinal()];
        for (BlockPos p : links) {
            if (!(level.getBlockEntity(p) instanceof LaserNodeBlockEntity n)) continue;
            for (Direction d : Direction.values()) {
                if (n.channel[d.ordinal()] == ch) out.add(new Target(n, d, n.priority[d.ordinal()]));
            }
        }
        out.sort((a, b) -> Integer.compare(b.prio, a.prio));
        return out;
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        List<Integer> ch = new ArrayList<>();
        List<Integer> pr = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            ch.add(channel[i]);
            pr.add(priority[i]);
        }
        out.store("channels", com.mojang.serialization.Codec.INT.listOf(), ch);
        out.store("priorities", com.mojang.serialization.Codec.INT.listOf(), pr);
        out.store("filters", FilterData.CODEC.listOf(), List.of(filters));
        out.store("links", BlockPos.CODEC.listOf(), new ArrayList<>(links));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        List<Integer> ch = in.read("channels", com.mojang.serialization.Codec.INT.listOf()).orElse(List.of());
        List<Integer> pr = in.read("priorities", com.mojang.serialization.Codec.INT.listOf()).orElse(List.of());
        List<FilterData> fl = in.read("filters", FilterData.CODEC.listOf()).orElse(List.of());
        for (int i = 0; i < 6; i++) {
            channel[i] = i < ch.size() ? Math.floorMod(ch.get(i), CHANNELS) : 0;
            priority[i] = i < pr.size() ? Math.max(0, Math.min(MAX_PRIORITY, pr.get(i))) : 0;
            filters[i] = i < fl.size() ? fl.get(i) : FilterData.EMPTY;
        }
        links.clear();
        in.read("links", BlockPos.CODEC.listOf()).ifPresent(links::addAll);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
