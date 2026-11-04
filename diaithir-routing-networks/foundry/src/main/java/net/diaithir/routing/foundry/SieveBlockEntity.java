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
import org.jspecify.annotations.Nullable;

public final class SieveBlockEntity extends BlockEntity {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    public final Filter filter = new Filter();

    public SieveBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirRouting.SIEVE_BE.get(), pos, state);
    }

    /** An empty filter lets everything through, a signal inverts the result (so an empty filter plus a signal blocks everything). */
    boolean passes(ItemResource r) {
        boolean inverted = level != null && level.hasNeighborSignal(worldPosition);
        return (filter.isEmpty() || filter.matches(r)) != inverted;
    }

    /** The filtered view of the inventory the sieve points at, for every face except the one it points out of. */
    public @Nullable ResourceHandler<ItemResource> handler(@Nullable Direction side) {
        Direction front = getBlockState().getValue(SieveBlock.FACING);
        if (level == null || side == front) return null;
        if (DEPTH.get() >= 6) return null;
        DEPTH.set(DEPTH.get() + 1);
        try {
            ResourceHandler<ItemResource> target = level.getCapability(Capabilities.Item.BLOCK, worldPosition.relative(front), front.getOpposite());
            return target == null ? null : new FilteredView(target, this::passes);
        } finally {
            DEPTH.set(DEPTH.get() - 1);
        }
    }

    public String describe() {
        return "Sieve: " + filter.describe() + (level != null && level.hasNeighborSignal(worldPosition) ? " (inverted by redstone)" : "");
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        filter.save(out, "filter");
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        filter.load(in, "filter");
    }
}
