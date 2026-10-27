package net.diaithir.brewing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashMap;
import java.util.Map;

/** Holds a base, a few ingredients, a fermenting clock and the finished servings. */
public final class BrewVesselEntity extends BlockEntity {
    Item base = Items.AIR;
    final Map<Item, Integer> held = new HashMap<>();
    BrewRecipe active;
    int progress;
    ItemStack drink = ItemStack.EMPTY;
    Item container = Items.AIR;

    public BrewVesselEntity(BlockPos pos, BlockState state) {
        super(DiaithirBrewing.VESSEL_ENTITY.get(), pos, state);
    }

    String station() {
        return ((BrewVesselBlock) getBlockState().getBlock()).station;
    }

    int capacity() {
        return ((BrewVesselBlock) getBlockState().getBlock()).capacity;
    }

    int count() {
        return held.values().stream().mapToInt(Integer::intValue).sum();
    }

    boolean busy() {
        return active != null;
    }

    boolean done() {
        return !drink.isEmpty();
    }

    /** Starts fermenting if the contents now match a recipe exactly. */
    void tryStart() {
        for (BrewRecipe r : BrewRecipes.all()) {
            if (r.matches(station(), base, held)) {
                active = r;
                progress = 0;
                break;
            }
        }
        sync();
    }

    void finish() {
        drink = new ItemStack(active.drink(), active.servings());
        container = active.container();
        held.clear();
        base = Items.AIR;
        active = null;
        sync();
    }

    ItemStack serve() {
        ItemStack one = drink.copyWithCount(1);
        drink.shrink(1);
        if (drink.isEmpty()) {
            drink = ItemStack.EMPTY;
            container = Items.AIR;
        }
        sync();
        return one;
    }

    void dump(Level level, BlockPos pos) {
        held.forEach((item, n) -> Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, new ItemStack(item, n)));
        if (base != Items.AIR && active != null && active.baseReturns() != Items.AIR) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, new ItemStack(active.baseReturns()));
        }
        held.clear();
        base = Items.AIR;
        active = null;
        progress = 0;
        sync();
    }

    static void tick(Level level, BlockPos pos, BlockState state, BrewVesselEntity be) {
        if (be.active != null && ++be.progress >= be.active.ticks()) {
            be.finish();
        }
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        base = itemOr(in.getStringOr("Base", ""));
        held.clear();
        in.listOrEmpty("HeldItems", ItemStack.CODEC).forEach(s -> held.put(s.getItem(), s.getCount()));
        progress = in.getIntOr("Progress", 0);
        drink = in.read("Drink", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        container = itemOr(in.getStringOr("Container", ""));
        active = null;
        if (progress > 0 || !held.isEmpty()) {
            active = BrewRecipes.all().stream().filter(r -> r.matches(station(), base, held)).findFirst().orElse(null);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("Base", BuiltInRegistries.ITEM.getKey(base).toString());
        var list = out.list("HeldItems", ItemStack.CODEC);
        held.forEach((item, n) -> list.add(new ItemStack(item, n)));
        out.putInt("Progress", progress);
        out.store("Drink", ItemStack.OPTIONAL_CODEC, drink);
        out.putString("Container", BuiltInRegistries.ITEM.getKey(container).toString());
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private static Item itemOr(String id) {
        return id.isEmpty() ? Items.AIR : BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).orElse(Items.AIR);
    }
}
