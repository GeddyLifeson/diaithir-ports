package net.diaithir.storagenet;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Read-only view of everything a storage network holds, one entry per distinct item, exposed on the controller so any reader (the wiretech
 * circuit reader, comparators, other mods) can count what is in the network. It cannot be inserted into or extracted from; terminals and port cables do that.
 */
final class NetworkView implements ResourceHandler<ItemResource> {
    private static final int REFRESH_TICKS = 10;
    private final Supplier<Network> network;
    private final LongSupplier clock;
    private List<Map.Entry<ItemResource, Long>> entries = List.of();
    private long stamp = Long.MIN_VALUE / 2;

    /** One stable handler per controller, so cached capability lookups stay valid; it rebuilds its snapshot when read and older than half a second. */
    NetworkView(Supplier<Network> network, LongSupplier clock) {
        this.network = network;
        this.clock = clock;
    }

    private List<Map.Entry<ItemResource, Long>> entries() {
        long now = clock.getAsLong();
        if (now - stamp >= REFRESH_TICKS || now < stamp) {
            Network n = network.get();
            entries = n == null ? List.of() : new ArrayList<>(n.contents().entrySet());
            stamp = now;
        }
        return entries;
    }

    @Override
    public int size() {
        return entries().size();
    }

    @Override
    public ItemResource getResource(int index) {
        List<Map.Entry<ItemResource, Long>> e = entries();
        return index < e.size() ? e.get(index).getKey() : ItemResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index) {
        List<Map.Entry<ItemResource, Long>> e = entries();
        return index < e.size() ? e.get(index).getValue() : 0;
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return resource.equals(getResource(index)) ? getAmountAsLong(index) : 0;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return false;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return 0;
    }
}
