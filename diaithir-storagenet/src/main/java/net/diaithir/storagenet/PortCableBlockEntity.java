package net.diaithir.storagenet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Holds the filter of an import, export or filter cable and does the moving for the first two. */
public final class PortCableBlockEntity extends BlockEntity {
    private final Filter filter = new Filter();

    public PortCableBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirStorageNet.PORT_BE.get(), pos, state);
    }

    public Filter filter() {
        return filter;
    }

    private CableKind kind() {
        return getBlockState().getBlock() instanceof PortCableBlock p ? p.kind() : CableKind.CABLE;
    }

    /** One run: moves up to itemsPerRun items between the network and the inventories touching this cable. Returns how many moved. */
    public int work(ServerLevel level) {
        CableKind k = kind();
        if (k != CableKind.IMPORT && k != CableKind.EXPORT) return 0;
        Network net = Networks.find(level, worldPosition);
        if (net == null) return 0;
        int budget = Config.ITEMS_PER_OPERATION.get();
        int moved = 0;
        for (Direction d : Direction.values()) {
            if (budget <= 0) break;
            BlockPos np = worldPosition.relative(d);
            if (!level.isLoaded(np) || level.getBlockState(np).getBlock() instanceof NetBlock) continue;
            ResourceHandler<ItemResource> h = level.getCapability(Capabilities.Item.BLOCK, np, d.getOpposite());
            if (h == null) continue;
            int m = k == CableKind.IMPORT ? importFrom(h, net, budget) : exportTo(h, net, budget);
            budget -= m;
            moved += m;
        }
        return moved;
    }

    private int importFrom(ResourceHandler<ItemResource> h, Network net, int budget) {
        int moved = 0;
        for (int i = 0, n = h.size(); i < n && budget - moved > 0; i++) {
            ItemResource r = h.getResource(i);
            long have = h.getAmountAsLong(i);
            if (r.isEmpty() || have <= 0 || !filter.allows(r)) continue;
            moved += transfer(h, net, r, (int) Math.min(have, budget - moved), true);
        }
        return moved;
    }

    private int exportTo(ResourceHandler<ItemResource> h, Network net, int budget) {
        List<ItemResource> wanted = new ArrayList<>();
        if (!filter.blacklist() && !filter.isEmpty()) {
            wanted.addAll(filter.items());
        } else {
            for (Map.Entry<ItemResource, Long> e : net.contents().entrySet()) if (filter.allows(e.getKey())) wanted.add(e.getKey());
        }
        int moved = 0;
        for (ItemResource r : wanted) {
            if (budget - moved <= 0) break;
            moved += transfer(h, net, r, budget - moved, false);
        }
        return moved;
    }

    /** Moves in two steps so a short destination cannot lose items: first learn how much fits, then move exactly that. */
    private static int transfer(ResourceHandler<ItemResource> chest, Network net, ItemResource r, int amount, boolean toNetwork) {
        int fit;
        try (Transaction tx = Transaction.openRoot()) {
            int out = toNetwork ? chest.extract(r, amount, tx) : net.extract(r, amount, tx);
            fit = out <= 0 ? 0 : (toNetwork ? net.insert(r, out, tx) : chest.insert(r, out, tx));
        }
        if (fit <= 0) return 0;
        try (Transaction tx = Transaction.openRoot()) {
            int out = toNetwork ? chest.extract(r, fit, tx) : net.extract(r, fit, tx);
            int in = out <= 0 ? 0 : (toNetwork ? net.insert(r, out, tx) : chest.insert(r, out, tx));
            if (out == fit && in == fit) {
                tx.commit();
                return fit;
            }
        }
        return 0;
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putBoolean("blacklist", filter.blacklist());
        out.store("items", ItemResource.CODEC.listOf(), new ArrayList<>(filter.items()));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        filter.setBlacklist(in.getBooleanOr("blacklist", false));
        filter.items().clear();
        in.read("items", ItemResource.CODEC.listOf()).ifPresent(filter.items()::addAll);
    }
}
