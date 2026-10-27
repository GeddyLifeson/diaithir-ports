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

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.HashMap;
import java.util.Map;

/** Holds a base, a few ingredients, a fermenting clock and the finished servings. */
public final class BrewVesselEntity extends BlockEntity {
    Item base = Items.AIR;
    final Map<Item, Integer> held = new HashMap<>();
    BrewRecipe active;
    int progress; // quarter-ticks: a good cellar ferments up to 1.5 times faster
    int score = -1;
    int settle = -1;
    static final int SETTLE_TICKS = 100;
    ItemStack drink = ItemStack.EMPTY;
    Item container = Items.AIR;

    public BrewVesselEntity(BlockPos pos, BlockState state) {
        super(DiaithirBrewing.VESSEL_ENTITY.get(), pos, state);
    }

    String station() {
        return ((BrewVesselBlock) getBlockState().getBlock()).station;
    }

    /** The block's size, raised so every recipe for this station fits. */
    int capacity() {
        return Math.max(((BrewVesselBlock) getBlockState().getBlock()).capacity, BrewRecipes.largest(station()));
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

    boolean acceptsBase(Item item) {
        return !busy() && !done() && base == Items.AIR && BrewRecipes.usesBase(station(), item);
    }

    boolean acceptsIngredient(Item item) {
        return !busy() && !done() && count() < capacity() && BrewRecipes.usesIngredient(station(), item);
    }

    /** Starts fermenting when the contents match a recipe exactly; if a bigger recipe could still be completed, waits a few seconds for more input. */
    void tryStart() {
        settle = -1;
        BrewRecipe exact = BrewRecipes.all().stream().filter(r -> r.matches(station(), base, held)).findFirst().orElse(null);
        if (exact == null) {
            sync();
        } else if (BrewRecipes.extendable(station(), base, held)) {
            settle = SETTLE_TICKS;
            sync();
        } else {
            begin(exact);
        }
    }

    /** Empty-hand click while waiting: brew what is in there now. */
    boolean startNow() {
        BrewRecipe exact = settle >= 0 ? BrewRecipes.all().stream().filter(r -> r.matches(station(), base, held)).findFirst().orElse(null) : null;
        if (exact == null) {
            return false;
        }
        begin(exact);
        return true;
    }

    private void begin(BrewRecipe r) {
        active = r;
        progress = 0;
        settle = -1;
        sync();
    }

    void finish(int score) {
        Item out = level != null && Cellar.spoils(active, score, level.getRandom()) ? active.spoiled() : active.drink();
        drink = new ItemStack(out, active.servings());
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
        if (be.active != null) {
            if (level.getGameTime() % 20 == 0 || be.score < 0) {
                be.score = Cellar.score(level, pos);
            }
            be.progress += Cellar.rate(be.score);
            if (be.progress >= be.active.ticks() * 4) {
                be.finish(be.score);
            }
        } else if (be.settle > 0 && --be.settle == 0) {
            be.startNow();
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
        settle = -1;
        if (progress > 0) {
            active = BrewRecipes.all().stream().filter(r -> r.matches(station(), base, held)).findFirst().orElse(null);
        } else if (!held.isEmpty() && !done()) {
            settle = 1;
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

    /** Hoppers, pipes and Create belts: slot 0 takes one base or ingredient at a time, slot 1 gives the finished drinks. */
    final class Automation extends SnapshotJournal<Object[]> implements ResourceHandler<ItemResource> {
        @Override
        protected Object[] createSnapshot() {
            return new Object[] {base, new HashMap<>(held), active, progress, drink.copy(), container};
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void revertToSnapshot(Object[] s) {
            base = (Item) s[0];
            held.clear();
            held.putAll((Map<Item, Integer>) s[1]);
            active = (BrewRecipe) s[2];
            progress = (Integer) s[3];
            drink = (ItemStack) s[4];
            container = (Item) s[5];
        }

        @Override
        protected void onRootCommit(Object[] original) {
            sync();
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public ItemResource getResource(int index) {
            return index == 1 && done() ? ItemResource.of(drink) : ItemResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index) {
            return index == 1 ? drink.getCount() : 0;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return index == 1 ? drink.getCount() : acceptsBase(resource.getItem()) || acceptsIngredient(resource.getItem()) ? 1 : 0;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return index == 0 && (acceptsBase(resource.getItem()) || acceptsIngredient(resource.getItem()));
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
            Item item = resource.getItem();
            if (index != 0 || amount < 1 || !isValid(0, resource)) {
                return 0;
            }
            updateSnapshots(tx);
            if (acceptsBase(item)) {
                base = item;
            } else {
                held.merge(item, 1, Integer::sum);
            }
            tryStart();
            return 1;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
            if (index != 1 || !done() || drink.getItem() != resource.getItem()) {
                return 0;
            }
            int n = Math.min(amount, drink.getCount());
            updateSnapshots(tx);
            drink.shrink(n);
            if (drink.isEmpty()) {
                drink = ItemStack.EMPTY;
                container = Items.AIR;
            }
            return n;
        }
    }

    final Automation automation = new Automation();

    /** Pipes: one bucket of a recipe's base fluid (water, milk...) fills the base slot, same as pouring the bucket by hand. */
    final class FluidIntake implements ResourceHandler<FluidResource> {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public FluidResource getResource(int index) {
            return FluidResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index) {
            return 0;
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource) {
            return isValid(0, resource) ? FluidType.BUCKET_VOLUME : 0;
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return index == 0 && !resource.isEmpty() && acceptsBase(resource.getFluid().getBucket());
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext tx) {
            if (amount < FluidType.BUCKET_VOLUME || !isValid(index, resource)) {
                return 0;
            }
            automation.updateSnapshots(tx);
            base = resource.getFluid().getBucket();
            tryStart();
            return FluidType.BUCKET_VOLUME;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext tx) {
            return 0;
        }
    }

    final FluidIntake fluidIntake = new FluidIntake();
}
