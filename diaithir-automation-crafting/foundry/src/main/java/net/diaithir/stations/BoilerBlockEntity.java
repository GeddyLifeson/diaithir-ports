package net.diaithir.stations;

import net.diaithir.crafting.foundry.MachineStats;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Hand-fed boiler. One fuel item burns for its burn time at the boiler's rated watts (tech.boiler, 50 kW) and stores that as joules. */
public final class BoilerBlockEntity extends BlockEntity {
    public static final double CAPACITY_J = 20_000_000.0;
    private ItemStack fuel = ItemStack.EMPTY;
    private double joules;

    public BoilerBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirStations.BOILER_BE.get(), pos, state);
    }

    static double ratedWatts() {
        MachineStats.Stat s = MachineStats.of("tech.boiler");
        return s == null || s.watts() <= 0 ? 50_000.0 : s.watts();
    }

    /** Joules one item of this fuel gives, 0 when it does not burn. */
    double joulesOf(ItemStack s) {
        if (level == null || s.isEmpty()) return 0;
        return level.fuelValues().burnDuration(s) / 20.0 * ratedWatts();
    }

    public double stored() { return joules; }

    double potentialJoules() { return joules + (fuel.isEmpty() ? 0 : fuel.getCount() * joulesOf(fuel)); }

    /** Feeds fuel. Returns how many items went in. */
    public int feed(ItemStack s) {
        if (joulesOf(s) <= 0) return 0;
        if (!fuel.isEmpty() && !ItemStack.isSameItemSameComponents(fuel, s)) return 0;
        int n = fuel.isEmpty() ? Math.min(s.getCount(), s.getMaxStackSize()) : Math.min(s.getCount(), fuel.getMaxStackSize() - fuel.getCount());
        if (n <= 0) return 0;
        if (fuel.isEmpty()) fuel = s.copyWithCount(n); else fuel.grow(n);
        changed();
        return n;
    }

    /** Spends joules, burning fuel as needed. Returns the joules it could not pay. */
    double spend(double need) {
        while (need > 1.0E-6) {
            double take = Math.min(joules, need);
            joules -= take;
            need -= take;
            if (need <= 1.0E-6 || fuel.isEmpty()) break;
            joules += joulesOf(fuel);
            fuel.shrink(1);
            if (fuel.isEmpty()) fuel = ItemStack.EMPTY;
        }
        changed();
        return Math.max(0, need);
    }

    void changed() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            boolean lit = joules > 0 || !fuel.isEmpty();
            BlockState s = getBlockState();
            if (s.hasProperty(BoilerBlock.LIT) && s.getValue(BoilerBlock.LIT) != lit) level.setBlock(worldPosition, s.setValue(BoilerBlock.LIT, lit), 3);
        }
    }

    public Component describe() {
        return Component.literal("Boiler: " + (fuel.isEmpty() ? "no fuel" : fuel.getHoverName().getString() + " x" + fuel.getCount()) + ", " + Math.round(joules / 1000.0) + " kJ in the tank");
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide() && !fuel.isEmpty()) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), fuel);
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.store("fuel", ItemStack.OPTIONAL_CODEC, fuel);
        out.putDouble("joules", joules);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        fuel = in.read("fuel", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        joules = in.getDoubleOr("joules", 0.0);
    }
}
