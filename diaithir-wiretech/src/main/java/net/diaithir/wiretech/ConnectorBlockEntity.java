package net.diaithir.wiretech;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the wires of one connector and a small FE buffer. Every tick it feeds blocks next to it, then shares FE along its wires
 * with any connector that holds less, losing a share per block of wire. FE therefore flows from generators towards machines
 * with no global network to keep in sync.
 */
public final class ConnectorBlockEntity extends BlockEntity {
    public static final int MAX_LINKS = 6;

    public final WireType wire;
    public final FeBuffer energy;
    private final List<BlockPos> links = new ArrayList<>();

    public ConnectorBlockEntity(BlockPos pos, BlockState state) {
        super(Machines.CONNECTOR_BE.get(), pos, state);
        this.wire = ((ConnectorBlock) state.getBlock()).wire;
        this.energy = new FeBuffer(wire.throughput * 4, wire.throughput, 0, this::setChanged);
    }

    public List<BlockPos> links() {
        return links;
    }

    public @Nullable EnergyHandler energyFor(net.minecraft.core.@Nullable Direction side) {
        return energy;
    }

    /** Where the wires of this connector are tied: a little way out from the block it sits on. */
    public Vec3 attach() {
        return attachOf(worldPosition, getBlockState());
    }

    public static Vec3 attachOf(BlockPos pos, BlockState state) {
        Vec3 c = Vec3.atCenterOf(pos);
        if (state.getBlock() instanceof ConnectorBlock) {
            net.minecraft.core.Direction f = state.getValue(ConnectorBlock.FACING);
            return c.add(new Vec3(f.getStepX(), f.getStepY(), f.getStepZ()).scale(0.3));
        }
        return c;
    }

    // ---- wiring --------------------------------------------------------------------------------------------------------------------

    /** Joins two connectors with a wire of the given type. Returns null on success or the reason it failed. */
    public static @Nullable String connect(ServerLevel level, BlockPos a, BlockPos b, WireType type) {
        if (a.equals(b)) return "A wire needs two different connectors.";
        if (!(level.getBlockEntity(a) instanceof ConnectorBlockEntity ca) || !(level.getBlockEntity(b) instanceof ConnectorBlockEntity cb)) {
            return "That is not a wire connector.";
        }
        if (ca.wire != type || cb.wire != type) return "A " + type.id + " wire only fits " + type.id + " connectors.";
        if (ca.links.contains(b)) return "Those connectors are already wired together.";
        if (ca.links.size() >= MAX_LINKS || cb.links.size() >= MAX_LINKS) return "A connector holds at most " + MAX_LINKS + " wires.";
        double len = ca.attach().distanceTo(cb.attach());
        if (len > type.maxRange()) return String.format("Too far: %.1f blocks, this wire reaches %.0f.", len, type.maxRange());
        ca.addLink(b);
        cb.addLink(a);
        return null;
    }

    private void addLink(BlockPos other) {
        links.add(other.immutable());
        changed();
    }

    private void removeLink(BlockPos other) {
        links.remove(other);
        changed();
    }

    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel sl)) return;
        for (BlockPos other : new ArrayList<>(links)) {
            if (sl.getBlockEntity(other) instanceof ConnectorBlockEntity peer) peer.removeLink(pos);
            ItemStack coil = new ItemStack(Machines.COILS.get(wire).get());
            Block.popResource(sl, pos, coil);
        }
        links.clear();
    }

    // ---- ticking -------------------------------------------------------------------------------------------------------------------

    public void serverTick(ServerLevel level) {
        if (energy.amount() > 0) energy.pushToNeighbours(level, worldPosition, wire.throughput, true);
        if (links.isEmpty() || energy.amount() <= 0) return;
        Vec3 here = attach();
        for (BlockPos other : new ArrayList<>(links)) {
            if (!level.isLoaded(other)) continue;
            if (!(level.getBlockEntity(other) instanceof ConnectorBlockEntity peer)) {
                removeLink(other); // the far end is gone
                Block.popResource(level, worldPosition, new ItemStack(Machines.COILS.get(wire).get()));
                continue;
            }
            int mine = energy.amount(), theirs = peer.energy.amount();
            if (mine <= theirs) continue;
            double eff = wire.efficiency(here.distanceTo(peer.attach()));
            int sent = Math.min(wire.throughput, (mine - theirs) / 2);
            int delivered = Math.min((int) Math.floor(sent * eff), peer.energy.room());
            if (delivered <= 0) continue;
            sent = (int) Math.min(sent, Math.ceil(delivered / eff));
            energy.take(sent);
            peer.energy.give(delivered);
        }
    }

    // ---- save, load, client sync -----------------------------------------------------------------------------------------------

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        energy.serialize(out.child("fe"));
        ValueOutput.ValueOutputList list = out.childrenList("links");
        for (BlockPos p : links) list.addChild().putIntArray("p", new int[] {p.getX(), p.getY(), p.getZ()});
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.child("fe").ifPresent(energy::deserialize);
        links.clear();
        for (ValueInput c : in.childrenListOrEmpty("links")) {
            c.getIntArray("p").ifPresent(a -> {
                if (a.length == 3) links.add(new BlockPos(a[0], a[1], a[2]));
            });
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
