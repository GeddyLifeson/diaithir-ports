package com.buuz135.industrial.item.infinity;

import com.buuz135.industrial.utils.IFAttachments;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An infinity item's energy as other mods see it. The charge lives on the item (a long component), so a charge is an
 * exchange of the accessed item for a charged copy, which the access commits or rolls back with the transaction.
 */
public class InfinityItemEnergy implements EnergyHandler {
    private final ItemAccess access;

    public InfinityItemEnergy(ItemAccess access) {
        this.access = access;
    }

    @Override
    public long getAmountAsLong() {
        return Math.min(access.getResource().getOrDefault(IFAttachments.INFINITY_ITEM_POWER.get(), 0L), getCapacityAsLong());
    }

    @Override
    public long getCapacityAsLong() {
        return InfinityTier.ARTIFACT.getPowerNeeded();
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        ItemResource item = access.getResource();
        if (!item.getOrDefault(IFAttachments.INFINITY_ITEM_CAN_CHARGE.get(), false)) return 0;
        long stored = getAmountAsLong();
        int received = (int) Math.min(getCapacityAsLong() - stored, amount);
        if (received <= 0) return 0;
        return access.exchange(item.with(IFAttachments.INFINITY_ITEM_POWER, stored + received), 1, transaction) == 1 ? received : 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return 0;
    }
}
