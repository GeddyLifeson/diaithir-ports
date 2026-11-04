package net.diaithir.bridges;

import net.diaithir.units.fuel.Fuel;
import net.diaithir.units.fuel.FuelStore;
import net.diaithir.units.fuel.Fuels;
import net.diaithir.units.fuel.SimpleStore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;
import java.util.OptionalInt;

/**
 * Magic-to-fuel converter. A magic reagent (tag diaithir_integration:magic_fuel, worth the same joules the Toburdeluin gets from it) is turned into
 * quanta of one magic carrier from the fuel table (mana, aura, ember, life points, vis or source), at the configured yield. The quanta are a
 * {@link FuelStore} on every face and are pushed into any neighbouring fuel store that takes them, such as a carrier tank, so a mana train or an
 * ember steam engine is fed by the same tank as a diesel one. Sneak with an empty hand to pick the carrier while the store is empty.
 */
public final class MagicCarrierEntity extends BlockEntity implements WorldlyContainer {
    private static final int PUSH_PER_TICK = 100;

    private final NonNullList<ItemStack> slot = NonNullList.withSize(1, ItemStack.EMPTY);
    private final SimpleStore[] stores;
    private final List<Fuel> carriers;
    private int pick;

    MagicCarrierEntity(BlockPos pos, BlockState state) {
        super(DiaithirBridges.MAGIC_CARRIER_ENTITY.get(), pos, state);
        carriers = Fuels.all().stream().filter(f -> f.kind() == Fuel.Kind.MAGIC).toList();
        stores = new SimpleStore[carriers.size()];
        for (int i = 0; i < stores.length; i++) stores[i] = new SimpleStore(Config.get(Config.MAGIC_BUFFER));
    }

    public FuelStore store() { return stores[pick]; }
    Fuel carrier() { return carriers.get(pick); }

    /** Quanta one stack item is worth at the current carrier. A reagent's FE is joules (1 FE = 1 J), minus the yield loss. */
    double quantaFor(ItemStack s) {
        OptionalInt fe = BridgeEntity.fuelValue(s);
        return fe.isEmpty() ? 0 : quantaFromJoules(fe.getAsInt());
    }

    /** Quanta of the current carrier that {@code joules} of reagent make, after the yield loss. */
    double quantaFromJoules(double joules) {
        return joules * (Config.get(Config.MAGIC_YIELD_PCT) / 100.0) / carrier().joulesPerUnit();
    }

    /** Next carrier, only while the store is empty. */
    boolean cycle() {
        if (stores[pick].quantity() > 0 || carriers.size() < 2) return false;
        pick = (pick + 1) % carriers.size();
        level.invalidateCapabilities(worldPosition);
        setChanged();
        return true;
    }

    int signal() { return Ports.level(stores[pick].quantity() / stores[pick].capacity()); }

    String describe() {
        Fuel f = carrier();
        return f.name() + " " + Math.round(stores[pick].quantity() * 10) / 10.0 + " / " + (int) stores[pick].capacity() + " quanta (" + Math.round(f.joulesPerUnit()) + " J each)";
    }

    void serverTick() {
        if (level == null) return;
        SimpleStore s = stores[pick];
        ItemStack in = slot.get(0);
        if (!in.isEmpty()) {
            double q = quantaFor(in);
            if (q > 0 && s.free() >= q) {
                s.insert(carrier(), q);
                in.shrink(1);
                setChanged();
            }
        }
        if (s.quantity() <= 0) return;
        for (Direction d : Direction.values()) {
            FuelStore other = level.getCapability(Ports.FUEL, worldPosition.relative(d), d.getOpposite());
            if (other == null || other == s) continue;
            if (FuelStore.transfer(s, other, PUSH_PER_TICK) > 0) { setChanged(); break; }
        }
    }

    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, slot);
        out.putString("carrier", carrier().id());
        out.putDouble("qty", stores[pick].quantity());
    }

    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        ContainerHelper.loadAllItems(in, slot);
        in.getString("carrier").ifPresent(id -> {
            for (int i = 0; i < carriers.size(); i++) if (carriers.get(i).id().equals(id)) pick = i;
        });
        double q = in.getDoubleOr("qty", 0);
        if (q > 0) stores[pick].insert(carrier(), q);
    }

    // ---- hoppers and pipes: reagents in on any face
    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return slot.get(0).isEmpty(); }
    @Override public ItemStack getItem(int i) { return slot.get(i); }
    @Override public ItemStack removeItem(int i, int n) { ItemStack r = slot.get(i).split(n); if (!r.isEmpty()) setChanged(); return r; }
    @Override public ItemStack removeItemNoUpdate(int i) { ItemStack r = slot.get(i); slot.set(i, ItemStack.EMPTY); return r; }
    @Override public void setItem(int i, ItemStack s) { slot.set(i, s); setChanged(); }
    @Override public boolean stillValid(Player p) { return Container.stillValidBlockEntity(this, p); }
    @Override public void clearContent() { slot.clear(); }
    @Override public int[] getSlotsForFace(Direction d) { return new int[] {0}; }
    @Override public boolean canPlaceItem(int i, ItemStack s) { return BridgeEntity.fuelValue(s).isPresent(); }
    @Override public boolean canPlaceItemThroughFace(int i, ItemStack s, Direction d) { return canPlaceItem(i, s); }
    @Override public boolean canTakeItemThroughFace(int i, ItemStack s, Direction d) { return false; }
}
