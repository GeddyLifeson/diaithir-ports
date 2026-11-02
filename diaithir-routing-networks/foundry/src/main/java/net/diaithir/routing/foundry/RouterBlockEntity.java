package net.diaithir.routing.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class RouterBlockEntity extends BlockEntity {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private final Filter[] filters = new Filter[6];

    public RouterBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirRouting.ROUTER_BE.get(), pos, state);
        for (int i = 0; i < 6; i++) filters[i] = new Filter();
    }

    public Filter filter(Direction d) {
        return filters[d.get3DDataValue()];
    }

    private @Nullable ResourceHandler<ItemResource> neighbour(Direction d) {
        return level == null ? null : level.getCapability(Capabilities.Item.BLOCK, worldPosition.relative(d), d.getOpposite());
    }

    /** Pushes an item that arrived on face "from" out of the other faces. Moves into the real inventories inside the caller's transaction. */
    int route(Direction from, ItemResource r, int amount, TransactionContext tx) {
        if (level == null || level.isClientSide() || DEPTH.get() >= 6) return 0;
        DEPTH.set(DEPTH.get() + 1);
        try {
            int moved = push(from, r, amount, tx, true);
            if (moved == 0) moved = push(from, r, amount, tx, false);
            return moved;
        } finally {
            DEPTH.set(DEPTH.get() - 1);
        }
    }

    private int push(Direction from, ItemResource r, int amount, TransactionContext tx, boolean matching) {
        int moved = 0;
        for (Direction d : Direction.values()) {
            if (d == from) continue;
            Filter f = filter(d);
            if (matching ? f.isEmpty() || !f.matches(r) : !f.isEmpty()) continue;
            ResourceHandler<ItemResource> h = neighbour(d);
            if (h == null) continue;
            moved += h.insert(r, amount - moved, tx);
            if (moved >= amount) break;
        }
        return moved;
    }

    /** What a neighbour sees when it pushes into face "side": a sink that never stores anything. */
    public ResourceHandler<ItemResource> handler(Direction side) {
        return new ResourceHandler<>() {
            @Override public int size() { return 1; }
            @Override public ItemResource getResource(int index) { return ItemResource.EMPTY; }
            @Override public long getAmountAsLong(int index) { return 0; }
            @Override public long getCapacityAsLong(int index, ItemResource r) { return Integer.MAX_VALUE; }
            @Override public boolean isValid(int index, ItemResource r) { return index == 0; }
            @Override public int insert(int index, ItemResource r, int amount, TransactionContext tx) { return index == 0 && amount > 0 ? route(side, r, amount, tx) : 0; }
            @Override public int extract(int index, ItemResource r, int amount, TransactionContext tx) { return 0; }
        };
    }

    public String describe() {
        StringBuilder sb = new StringBuilder("Router:");
        for (Direction d : Direction.values()) sb.append(' ').append(d.getName()).append('[').append(filter(d).describe()).append(']');
        return sb.toString();
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        for (Direction d : Direction.values()) filter(d).save(out, "f_" + d.getName());
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        for (Direction d : Direction.values()) filter(d).load(in, "f_" + d.getName());
    }
}
