package net.diaithir.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Hopper logic with no inventory of its own: it pulls from the block above (or picks items up) and pushes into the block it faces, in one pass. */
public final class ModHopperBlockEntity extends BlockEntity {
    private FilterData filter = FilterData.EMPTY;
    private int range = 3;
    private int cooldown;

    public ModHopperBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirRouting.HOPPER_BE.get(), pos, state);
    }

    private HopperKind kind() {
        return getBlockState().getBlock() instanceof ModHopperBlock h ? h.kind() : HopperKind.FILTER;
    }

    public FilterData filter() {
        return filter;
    }

    public void setFilter(FilterData f) {
        filter = f;
        setChanged();
    }

    public int collectRange() {
        return range;
    }

    public void cycleRange() {
        range = range % Config.COLLECTOR_MAX_RANGE.get() + 1;
        setChanged();
    }

    static int stackSize(HopperKind k) {
        return k == HopperKind.FAST ? 8 * Config.FAST_HOPPER_MULTIPLIER.get() : 8;
    }

    public void serverTick(ServerLevel level, BlockState state) {
        HopperKind k = kind();
        int interval = Config.HOPPER_INTERVAL.get();
        if (k == HopperKind.FAST) interval = Math.max(1, interval / Config.FAST_HOPPER_MULTIPLIER.get());
        if (++cooldown < interval) return;
        cooldown = 0;
        Direction facing = state.getValue(ModHopperBlock.FACING);
        ResourceHandler<ItemResource> out = Transfers.items(level, worldPosition.relative(facing), facing.getOpposite());
        if (out == null) return;
        int budget = k == HopperKind.FAST ? 8 * Config.FAST_HOPPER_MULTIPLIER.get() : 8;
        if (k == HopperKind.COLLECTOR) {
            collect(level, out, budget);
            return;
        }
        ResourceHandler<ItemResource> in = Transfers.items(level, worldPosition.above(), Direction.DOWN);
        if (in != null) Transfers.moveItems(in, out, budget, filter::allows);
        collectAbove(level, out, budget);
    }

    private void collectAbove(ServerLevel level, ResourceHandler<ItemResource> out, int budget) {
        AABB box = new AABB(worldPosition.above());
        pickUp(level, box, out, budget);
    }

    private void collect(ServerLevel level, ResourceHandler<ItemResource> out, int budget) {
        pickUp(level, new AABB(worldPosition).inflate(range), out, budget);
    }

    private void pickUp(ServerLevel level, AABB box, ResourceHandler<ItemResource> out, int budget) {
        for (ItemEntity e : level.getEntitiesOfClass(ItemEntity.class, box)) {
            if (budget <= 0) return;
            ItemStack s = e.getItem();
            if (s.isEmpty() || !filter.allows(s)) continue;
            int want = Math.min(s.getCount(), budget);
            int in;
            try (Transaction tx = Transaction.openRoot()) {
                in = out.insert(ItemResource.of(s), want, tx);
                if (in > 0) tx.commit();
            }
            if (in > 0) {
                s.shrink(in);
                budget -= in;
                if (s.isEmpty()) e.discard();
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.store("filter", FilterData.CODEC, filter);
        out.putInt("range", range);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        filter = in.read("filter", FilterData.CODEC).orElse(FilterData.EMPTY);
        range = Math.max(1, Math.min(Config.COLLECTOR_MAX_RANGE.get(), in.getIntOr("range", 3)));
    }
}
