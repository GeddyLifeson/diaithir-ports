package net.diaithir.routing.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Containers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public final class BarrelBlockEntity extends BlockEntity {
    public final BarrelStore store = new BarrelStore(this::changed);

    public BarrelBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirRouting.BARREL_BE.get(), pos, state);
    }

    private void changed() {
        setChanged();
        if (level != null) level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
    }

    /** Insert from every face but the bottom, extract from the bottom only. A null side (a player or a tool) may do both. */
    public ResourceHandler<ItemResource> handler(@Nullable Direction side) {
        return store.view(side != Direction.DOWN, side == null || side == Direction.DOWN);
    }

    public String describe() {
        ItemResource r = store.resource();
        return r.isEmpty() ? "Day Barrel: empty" : "Day Barrel: " + BuiltInRegistries.ITEM.getKey(r.getItem()).getPath() + " " + store.count() + " / " + BarrelStore.capacity(r);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            ItemResource r = store.resource();
            int left = store.count();
            while (!r.isEmpty() && left > 0) {
                int n = Math.min(left, r.getMaxStackSize());
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), r.toStack(n));
                left -= n;
            }
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        store.save(out);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        store.load(in);
    }
}
