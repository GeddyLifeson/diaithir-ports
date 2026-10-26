package net.diaithir.bridges;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

import java.util.OptionalInt;

/**
 * One block entity for every machine and tier (see {@link Spec}). SOURCE burns a magic reagent from the tag diaithir_integration:magic_fuel and pushes
 * Forge Energy to its neighbours; CHARGER takes Forge Energy and makes Duslachluibhe at its tier's cost; RELAY carries Forge Energy between tech mods
 * at its tier's throughput, losing a share on the way.
 */
public final class BridgeEntity extends BlockEntity implements WorldlyContainer {
    static final TagKey<Item> FUEL = TagKey.create(Registries.ITEM, Identifier.parse("diaithir_integration:magic_fuel"));
    static final TagKey<Item> HERB = TagKey.create(Registries.ITEM, Identifier.parse("diaithir_integration:herb_dusts"));
    static final String OUT_ITEM = "diaithir_integration:duslachluibhe";

    private final NonNullList<ItemStack> slot = NonNullList.withSize(1, ItemStack.EMPTY);
    private final SimpleEnergyHandler energy;
    private int burnLeft;
    private final Spec spec;

    BridgeEntity(BlockPos pos, BlockState state) {
        super(DiaithirBridges.ENTITY.get(), pos, state);
        this.spec = state.getBlock() instanceof BridgeBlock b ? b.spec() : Spec.ALL.get(0);
        this.energy = switch (spec.kind()) {
            case SOURCE -> new Handler(spec.capacity(), 0, spec.outPerTick());
            case CHARGER -> new Handler(spec.capacity(), spec.inPerTick(), 0);
            case RELAY -> new Handler(spec.capacity(), spec.inPerTick(), spec.outPerTick());
        };
    }

    private final class Handler extends SimpleEnergyHandler {
        Handler(int cap, int in, int out) { super(cap, in, out); }
        @Override protected void onEnergyChanged(int previous) { setChanged(); }
        void drain(int n) { set(Math.max(0, energy - n)); }
        void fill(int n) { set(Math.min(capacity, energy + n)); }
    }

    EnergyHandler energy() { return energy; }

    /** FE a stack of fuel is worth per item, or empty when it is not a magic reagent. */
    static OptionalInt fuelValue(ItemStack s) {
        if (s.isEmpty() || !s.is(FUEL)) return OptionalInt.empty();
        return OptionalInt.of(s.is(HERB) || s.is(BuiltInRegistries.ITEM.getValue(Identifier.parse(OUT_ITEM))) ? 4_000 : 16_000);
    }

    void serverTick() {
        if (level == null) return;
        Handler h = (Handler) energy;
        switch (spec.kind()) {
            case SOURCE -> {
                int rate = spec.rate();
                if (burnLeft <= 0 && h.getAmountAsInt() + rate * 10 < h.getCapacityAsInt()) {
                    OptionalInt v = fuelValue(slot.get(0));
                    if (v.isPresent()) {
                        slot.get(0).shrink(1);
                        burnLeft = (int) ((long) v.getAsInt() * spec.effPct() / 100 / rate);
                        setChanged();
                    }
                }
                if (burnLeft > 0) { burnLeft--; h.fill(rate); }
                push(h, 0);
            }
            case RELAY -> push(h, spec.lossPct());
            case CHARGER -> {
                Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(OUT_ITEM));
                ItemStack out = slot.get(0);
                while (item != net.minecraft.world.item.Items.AIR && h.getAmountAsInt() >= spec.cost()
                        && (out.isEmpty() || (out.is(item) && out.getCount() < out.getMaxStackSize()))) {
                    h.drain(spec.cost());
                    if (out.isEmpty()) { slot.set(0, new ItemStack(item)); out = slot.get(0); } else out.grow(1);
                    setChanged();
                }
            }
        }
    }

    /** Push stored FE to the first neighbour that takes it; lossPct of what moved is also burned off. */
    private void push(Handler h, int lossPct) {
        if (h.getAmountAsInt() <= 0) return;
        for (Direction d : Direction.values()) {
            var target = level.getCapability(Capabilities.Energy.BLOCK, worldPosition.relative(d), d.getOpposite());
            int moved = EnergyHandlerUtil.move(h, target, spec.outPerTick(), null);
            if (moved > 0) {
                if (lossPct > 0) h.drain(moved * lossPct / 100);
                break;
            }
        }
    }

    Component status() {
        String what = switch (spec.kind()) {
            case SOURCE -> "tier " + spec.tier() + ", " + spec.rate() + " FE/t, " + (burnLeft > 0 ? "burning, " + burnLeft / 20 + " s left" : "idle");
            case CHARGER -> "tier " + spec.tier() + ", takes " + spec.inPerTick() + " FE/t, makes Duslachluibhe at " + spec.cost() + " FE";
            case RELAY -> "tier " + spec.tier() + ", carries " + spec.outPerTick() + " FE/t, loses " + spec.lossPct() + "%";
        };
        return Component.literal(energy.getAmountAsInt() + " / " + energy.getCapacityAsInt() + " FE, " + what);
    }

    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, slot);
        out.putInt("burn", burnLeft);
        ((Handler) energy).serialize(out.child("energy"));
    }

    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        ContainerHelper.loadAllItems(in, slot);
        burnLeft = in.getIntOr("burn", 0);
        in.child("energy").ifPresent(((Handler) energy)::deserialize);
    }

    // ---- hoppers and pipes (vanilla container face rules)
    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return slot.get(0).isEmpty(); }
    @Override public ItemStack getItem(int i) { return slot.get(i); }
    @Override public ItemStack removeItem(int i, int n) { ItemStack r = slot.get(i).split(n); if (!r.isEmpty()) setChanged(); return r; }
    @Override public ItemStack removeItemNoUpdate(int i) { ItemStack r = slot.get(i); slot.set(i, ItemStack.EMPTY); return r; }
    @Override public void setItem(int i, ItemStack s) { slot.set(i, s); setChanged(); }
    @Override public boolean stillValid(Player p) { return Container.stillValidBlockEntity(this, p); }
    @Override public void clearContent() { slot.clear(); }
    @Override public int[] getSlotsForFace(Direction d) { return new int[] {0}; }
    @Override public boolean canPlaceItem(int i, ItemStack s) { return spec.kind() == Spec.Kind.SOURCE && fuelValue(s).isPresent(); }
    @Override public boolean canPlaceItemThroughFace(int i, ItemStack s, Direction d) { return canPlaceItem(i, s); }
    @Override public boolean canTakeItemThroughFace(int i, ItemStack s, Direction d) { return spec.kind() == Spec.Kind.CHARGER; }
}
