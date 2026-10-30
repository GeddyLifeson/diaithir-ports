package net.diaithir.storagenet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The result of one walk from a controller: the member blocks and the inventories used as storage. Item moves go through the NeoForge transfer API. */
public final class Network {
    /** One storage inventory: the block, the face the network touches it from, and the filters of cables touching it. */
    public record Store(BlockPos pos, Direction side, List<Filter> filters) {
        boolean allows(ItemResource r) {
            for (Filter f : filters) if (!f.allows(r)) return false;
            return true;
        }

        boolean wants(ItemResource r) {
            for (Filter f : filters) if (f.wants(r)) return true;
            return false;
        }
    }

    private final ServerLevel level;
    private final Set<Long> members;
    private final List<Store> stores;
    private final boolean incomplete;
    private final boolean truncated;
    private final int terminals;

    Network(ServerLevel level, Set<Long> members, List<Store> stores, boolean incomplete, boolean truncated, int terminals) {
        this.level = level;
        this.members = members;
        this.stores = stores;
        this.incomplete = incomplete;
        this.truncated = truncated;
        this.terminals = terminals;
    }

    public Set<Long> members() {
        return members;
    }

    public List<Store> stores() {
        return stores;
    }

    /** Part of the network was in an unloaded chunk when it was walked. */
    public boolean incomplete() {
        return incomplete;
    }

    /** The walk hit the configured block cap. */
    public boolean truncated() {
        return truncated;
    }

    public int terminals() {
        return terminals;
    }

    private @Nullable ResourceHandler<ItemResource> handler(Store s) {
        if (!level.isLoaded(s.pos())) return null;
        return level.getCapability(Capabilities.Item.BLOCK, s.pos(), s.side());
    }

    /** Everything stored, merged by item and components. */
    public Map<ItemResource, Long> contents() {
        Map<ItemResource, Long> out = new LinkedHashMap<>();
        for (Store s : stores) {
            ResourceHandler<ItemResource> h = handler(s);
            if (h == null) continue;
            for (int i = 0, n = h.size(); i < n; i++) {
                ItemResource r = h.getResource(i);
                if (r.isEmpty()) continue;
                long a = h.getAmountAsLong(i);
                if (a > 0) out.merge(r, a, Long::sum);
            }
        }
        return out;
    }

    public long count(ItemResource r) {
        long total = 0;
        for (Store s : stores) {
            ResourceHandler<ItemResource> h = handler(s);
            if (h == null) continue;
            for (int i = 0, n = h.size(); i < n; i++) if (h.getAmountAsLong(i) > 0 && r.equals(h.getResource(i))) total += h.getAmountAsLong(i);
        }
        return total;
    }

    /** Inserts into storage in the given transaction. Inventories that name the item in a whitelist go first, then ones already holding it. */
    public int insert(ItemResource r, int amount, TransactionContext tx) {
        if (r.isEmpty() || amount <= 0) return 0;
        int left = amount;
        for (int pass = 0; pass < 3 && left > 0; pass++) {
            for (Store s : stores) {
                if (left <= 0) break;
                if (!s.allows(r)) continue;
                boolean wanted = s.wants(r);
                ResourceHandler<ItemResource> h = handler(s);
                if (h == null) continue;
                boolean take = switch (pass) {
                    case 0 -> wanted;
                    case 1 -> !wanted && ResourceHandlerUtil.contains(h, r);
                    default -> !wanted && !ResourceHandlerUtil.contains(h, r);
                };
                if (take) left -= h.insert(r, left, tx);
            }
        }
        return amount - left;
    }

    public int extract(ItemResource r, int amount, TransactionContext tx) {
        if (r.isEmpty() || amount <= 0) return 0;
        int got = 0;
        for (Store s : stores) {
            if (got >= amount) break;
            ResourceHandler<ItemResource> h = handler(s);
            if (h == null) continue;
            got += h.extract(r, amount - got, tx);
        }
        return got;
    }

    /** Puts the stack into the network and returns what did not fit. */
    public ItemStack insertStack(ItemStack stack) {
        if (stack.isEmpty()) return stack;
        ItemResource r = ItemResource.of(stack);
        int in;
        try (Transaction tx = Transaction.openRoot()) {
            in = insert(r, stack.getCount(), tx);
            tx.commit();
        }
        ItemStack rest = stack.copy();
        rest.shrink(in);
        return rest;
    }

    /** Takes up to amount of the item out of the network. */
    public ItemStack extractStack(ItemResource r, int amount) {
        int got;
        try (Transaction tx = Transaction.openRoot()) {
            got = extract(r, amount, tx);
            tx.commit();
        }
        return got <= 0 ? ItemStack.EMPTY : r.toStack(got);
    }

    public String summary() {
        return members.size() + " network blocks, " + stores.size() + " inventories, " + terminals + " terminals"
                + (truncated ? ", size cap reached" : "") + (incomplete ? ", part unloaded" : "");
    }

    static List<Store> newStoreList() {
        return new ArrayList<>();
    }
}
