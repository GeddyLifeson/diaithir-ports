package net.diaithir.routing.foundry;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.Predicate;

/** The inventory behind a sieve, showing only what passes the filter. Slots that hold other items look empty and refuse everything. */
final class FilteredView implements ResourceHandler<ItemResource> {
    private final ResourceHandler<ItemResource> inner;
    private final Predicate<ItemResource> pass;

    FilteredView(ResourceHandler<ItemResource> inner, Predicate<ItemResource> pass) {
        this.inner = inner;
        this.pass = pass;
    }

    @Override public int size() { return inner.size(); }
    @Override public ItemResource getResource(int i) { ItemResource r = inner.getResource(i); return pass.test(r) ? r : ItemResource.EMPTY; }
    @Override public long getAmountAsLong(int i) { return pass.test(inner.getResource(i)) ? inner.getAmountAsLong(i) : 0; }
    @Override public long getCapacityAsLong(int i, ItemResource r) { return pass.test(r) ? inner.getCapacityAsLong(i, r) : 0; }
    @Override public boolean isValid(int i, ItemResource r) { return pass.test(r) && inner.isValid(i, r); }
    @Override public int insert(int i, ItemResource r, int amount, TransactionContext tx) { return pass.test(r) ? inner.insert(i, r, amount, tx) : 0; }
    @Override public int extract(int i, ItemResource r, int amount, TransactionContext tx) { return pass.test(r) ? inner.extract(i, r, amount, tx) : 0; }
}
