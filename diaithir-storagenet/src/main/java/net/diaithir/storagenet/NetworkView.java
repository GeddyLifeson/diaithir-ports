package net.diaithir.storagenet;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Read-only view of everything a storage network holds, one entry per distinct item, exposed on the controller so any reader (the wiretech
 * circuit reader, comparators, other mods) can count what is in the network. It cannot be inserted into or extracted from; terminals and port cables do that.
 */
final class NetworkView implements ResourceHandler<ItemResource> {
    private final List<Map.Entry<ItemResource, Long>> entries;

    NetworkView(Network network) {
        this.entries = network == null ? List.of() : new ArrayList<>(network.contents().entrySet());
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public ItemResource getResource(int index) {
        return entries.get(index).getKey();
    }

    @Override
    public long getAmountAsLong(int index) {
        return entries.get(index).getValue();
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
