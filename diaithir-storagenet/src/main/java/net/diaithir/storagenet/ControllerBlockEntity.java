package net.diaithir.storagenet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The master block. Holds the last walk of its network and re-walks only when marked dirty (or when part of it was unloaded). */
public final class ControllerBlockEntity extends BlockEntity {
    private @Nullable Network network;
    private boolean dirty = true;

    public ControllerBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirStorageNet.CONTROLLER_BE.get(), pos, state);
    }

    @Override
    public void setLevel(net.minecraft.world.level.Level level) {
        super.setLevel(level);
        // onLoad is deferred to the next level tick, so register as soon as the block entity has a level
        if (!level.isClientSide()) {
            Networks.register(this);
            dirty = true;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            Networks.register(this);
            dirty = true;
        }
    }

    @Override
    public void setRemoved() {
        Networks.unregister(this);
        super.setRemoved();
    }

    void markDirty() {
        dirty = true;
    }

    /** The current network, walking again first if something changed. Null on the client. */
    public @Nullable Network network() {
        if (!(level instanceof ServerLevel sl)) return null;
        if (dirty || network == null || network.incomplete()) {
            network = scan(sl);
            dirty = false;
        }
        return network;
    }

    private Network scan(ServerLevel level) {
        int maxBlocks = Config.MAX_NETWORK_BLOCKS.get();
        int maxStores = Config.MAX_STORES.get();
        Set<Long> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Map<Long, Network.Store> candidates = new LinkedHashMap<>();
        Set<Long> endpoints = new HashSet<>();
        boolean incomplete = false;
        boolean truncated = false;
        int terminals = 0;
        seen.add(worldPosition.asLong());
        queue.add(worldPosition);
        while (!queue.isEmpty()) {
            BlockPos p = queue.poll();
            BlockState st = level.getBlockState(p);
            boolean io = st.getBlock() instanceof PortCableBlock pb && (pb.kind() == CableKind.IMPORT || pb.kind() == CableKind.EXPORT);
            Filter filter = null;
            if (st.getBlock() instanceof PortCableBlock pb && pb.kind() == CableKind.FILTER && level.getBlockEntity(p) instanceof PortCableBlockEntity pe) filter = pe.filter();
            if (st.getBlock() instanceof TerminalBlock) terminals++;
            for (Direction d : Direction.values()) {
                BlockPos np = p.relative(d);
                if (!level.isLoaded(np)) {
                    incomplete = true;
                    continue;
                }
                BlockState ns = level.getBlockState(np);
                if (ns.getBlock() instanceof ControllerBlock) continue; // one controller per network
                if (ns.getBlock() instanceof NetBlock) {
                    if (seen.contains(np.asLong())) continue;
                    if (seen.size() >= maxBlocks) {
                        truncated = true;
                        continue;
                    }
                    seen.add(np.asLong());
                    queue.add(np);
                } else if (level.getCapability(Capabilities.Item.BLOCK, np, d.getOpposite()) != null) {
                    if (io) {
                        endpoints.add(np.asLong());
                    } else {
                        Network.Store s = candidates.computeIfAbsent(np.asLong(), k -> new Network.Store(np, d.getOpposite(), new java.util.ArrayList<>()));
                        if (filter != null && !s.filters().contains(filter)) s.filters().add(filter);
                    }
                }
            }
        }
        List<Network.Store> stores = Network.newStoreList();
        Set<Long> taken = new HashSet<>();
        for (Network.Store s : candidates.values()) {
            if (endpoints.contains(s.pos().asLong())) continue;
            if (stores.size() >= maxStores) {
                truncated = true;
                break;
            }
            // the two halves of a double chest expose the same inventory: count it once
            BlockState bs = level.getBlockState(s.pos());
            if (bs.getBlock() instanceof ChestBlock && bs.hasProperty(ChestBlock.TYPE) && bs.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                BlockPos other = s.pos().relative(ChestBlock.getConnectedDirection(bs));
                if (taken.contains(other.asLong())) continue;
            }
            taken.add(s.pos().asLong());
            stores.add(s);
        }
        return new Network(level, seen, stores, incomplete, truncated, terminals);
    }
}
