package net.diaithir.crafting.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Containers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class OperatorSeatBlockEntity extends BlockEntity {
    public final FoodStore food = new FoodStore(this::changed);
    /** Seconds of work the worker has already eaten but not spent. */
    private double reserve;

    public OperatorSeatBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirCrafting.OPERATOR_SEAT_BE.get(), pos, state);
    }

    public double reserveSeconds() { return reserve; }

    /** Spends seconds of work, eating food as needed. Returns the seconds it could not pay (0 when fully staffed). */
    double spend(double seconds) {
        double need = seconds;
        double take = Math.min(reserve, need);
        reserve -= take;
        need -= take;
        while (need > 1.0E-9) {
            int n = food.eatOne();
            if (n <= 0) break;
            reserve += n * Config.SECONDS_PER_NUTRITION.get();
            take = Math.min(reserve, need);
            reserve -= take;
            need -= take;
        }
        changed();
        return Math.max(0, need);
    }

    void changed() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            boolean staffed = reserve > 0 || food.count() > 0;
            BlockState s = getBlockState();
            if (s.hasProperty(OperatorSeatBlock.STAFFED) && s.getValue(OperatorSeatBlock.STAFFED) != staffed) {
                level.setBlock(worldPosition, s.setValue(OperatorSeatBlock.STAFFED, staffed), 3);
            }
        }
    }

    public String describe() {
        ItemResource r = food.resource();
        String f = r.isEmpty() ? "no food" : BuiltInRegistries.ITEM.getKey(r.getItem()).getPath() + " x" + food.count();
        return "Operator Seat: " + f + ", " + Math.round(reserve) + " s of work in hand";
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            ItemResource r = food.resource();
            int left = food.count();
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
        food.save(out);
        out.putDouble("reserve", reserve);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        food.load(in);
        reserve = in.getDoubleOr("reserve", 0.0);
    }
}
