package net.diaithir.wiretech;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The machine. Slots: 0 and 1 inputs, 2 output, 3 bonus output, 4 augment. Each tick it checks redstone, finds a recipe of its kind,
 * draws FE and advances. Side config sets item access per face; energy can be switched off per face.
 */
public final class MachineBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int NONE = 0, IN = 1, OUT = 2, BOTH = 3;
    public static final int DATA_COUNT = 14;

    public enum Redstone { IGNORE, HIGH, LOW }

    /** FE buffer. Only the machine draws from it. */
    public final class Buffer extends SimpleEnergyHandler {
        Buffer() {
            super(Config.BASE_CAPACITY_DEFAULT * Config.tierMult(tier), Config.BASE_RECEIVE_DEFAULT * Config.tierMult(tier), 0);
        }

        void refit() {
            capacity = Config.BASE_CAPACITY.get() * Config.tierMult(tier);
            maxInsert = Config.BASE_RECEIVE.get() * Config.tierMult(tier);
            if (energy > capacity) energy = capacity;
        }

        void drain(int amount) {
            if (amount > 0) set(Math.max(0, energy - amount));
        }

        @Override
        protected void onEnergyChanged(int previousAmount) {
            MachineBlockEntity.super.setChanged();
        }
    }

    /** Crucible tank. */
    public final class Tank extends FluidStacksResourceHandler {
        Tank() {
            super(1, Config.TANK_DEFAULT);
        }

        void setCap(int mb) {
            capacity = mb;
        }

        @Override
        protected void onContentsChanged(int index, FluidStack previous) {
            MachineBlockEntity.super.setChanged();
        }
    }

    public final Kind kind;
    public final int tier;
    private NonNullList<ItemStack> items = NonNullList.withSize(Kind.SIZE, ItemStack.EMPTY);
    public final Buffer energy;
    public final @Nullable Tank tank;
    private final int[] itemCfg = {BOTH, BOTH, BOTH, BOTH, BOTH, BOTH};
    private final boolean[] energyCfg = {true, true, true, true, true, true};
    private Redstone redstone = Redstone.IGNORE;
    private int progress; // hundredths of a tick of work
    private int draw;
    private int speedPct = 100;
    private RecipeHolder<MachineRecipe> active;
    private boolean dirty = true;
    private boolean running;
    private int tickCount;

    public MachineBlockEntity(BlockPos pos, BlockState state) {
        super(Machines.MACHINE_BE.get(), pos, state);
        MachineBlock b = (MachineBlock) state.getBlock();
        this.kind = b.kind;
        this.tier = b.tier;
        this.energy = new Buffer();
        this.tank = kind.fluid ? new Tank() : null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            energy.refit();
            if (tank != null) tank.setCap(Config.TANK.get());
        }
    }

    // ---- capabilities --------------------------------------------------------------------------------------------------------------

    public @Nullable EnergyHandler energyFor(@Nullable Direction side) {
        if (level != null && !level.isClientSide()) energy.refit();
        return side == null || energyCfg[side.get3DDataValue()] ? energy : null;
    }

    public @Nullable ResourceHandler<ItemResource> itemsFor(@Nullable Direction side) {
        if (side != null && itemCfg[side.get3DDataValue()] == NONE) return null;
        return new WorldlyContainerWrapper(this, side);
    }

    public @Nullable ResourceHandler<FluidResource> fluidFor(@Nullable Direction side) {
        if (tank == null) return null;
        if (side != null && (itemCfg[side.get3DDataValue()] & OUT) == 0) return null;
        // fluids come out only; the machine fills its own tank
        return new DelegatingResourceHandler<>(tank) {
            @Override
            public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
                return 0;
            }

            @Override
            public int insert(FluidResource resource, int amount, TransactionContext transaction) {
                return 0;
            }
        };
    }

    // ---- settings ------------------------------------------------------------------------------------------------------------------

    public Redstone redstone() {
        return redstone;
    }

    public void cycleRedstone() {
        redstone = Redstone.values()[(redstone.ordinal() + 1) % 3];
        super.setChanged();
    }

    public int itemMode(Direction d) {
        return itemCfg[d.get3DDataValue()];
    }

    public void setItemMode(Direction d, int mode) {
        itemCfg[d.get3DDataValue()] = mode & 3;
        invalidateCapabilities();
        super.setChanged();
    }

    public void cycleItemMode(int face) {
        itemCfg[face] = (itemCfg[face] + 1) & 3;
        invalidateCapabilities();
        super.setChanged();
    }

    public boolean energyOn(Direction d) {
        return energyCfg[d.get3DDataValue()];
    }

    public void setEnergyOn(Direction d, boolean on) {
        energyCfg[d.get3DDataValue()] = on;
        invalidateCapabilities();
        super.setChanged();
    }

    public void toggleEnergy(int face) {
        energyCfg[face] = !energyCfg[face];
        invalidateCapabilities();
        super.setChanged();
    }

    public int progress() {
        return progress;
    }

    public String debug() {
        return kind.id + " in0=" + items.get(0) + " out=" + items.get(2) + " active=" + (active != null) + " progress=" + progress + " fe="
                + energy.getAmountAsInt() + " draw=" + draw + " rs=" + redstone;
    }

    public boolean running() {
        return running;
    }

    public int lastDraw() {
        return draw;
    }

    /** Takes 1000 mB from the tank as a filled bucket, or empty. */
    public ItemStack bucketOut() {
        if (tank == null) return ItemStack.EMPTY;
        FluidResource r = tank.getResource(0);
        if (r.isEmpty() || tank.getAmountAsInt(0) < 1000) return ItemStack.EMPTY;
        Item bucket = r.getFluid().getBucket();
        if (bucket == Items.AIR) return ItemStack.EMPTY;
        tank.set(0, r, tank.getAmountAsInt(0) - 1000);
        return new ItemStack(bucket);
    }

    // ---- augments ------------------------------------------------------------------------------------------------------------------

    private int augments(Augment a) {
        ItemStack s = items.get(Kind.UPGRADE);
        return s.getItem() instanceof AugmentItem ai && ai.augment == a ? Math.min(s.getCount(), Config.MAX_AUGMENTS.get()) : 0;
    }

    public double speed() {
        return Config.tierSpeed(tier) * (1.0 + Config.SPEED_PER_AUGMENT.get() * augments(Augment.SPEED));
    }

    public double energyPerJob(MachineRecipe r) {
        double saving = Math.max(0.25, 1.0 - Config.SAVING_PER_AUGMENT.get() * augments(Augment.EFFICIENCY));
        return r.energy * saving * (1.0 + Config.SPEED_ENERGY_PENALTY.get() * augments(Augment.SPEED));
    }

    public float bonusChance(MachineRecipe r) {
        return (float) Math.min(1.0, r.bonusChance * (1.0 + Config.CHANCE_PER_AUGMENT.get() * augments(Augment.CHANCE)));
    }

    // ---- ticking -------------------------------------------------------------------------------------------------------------------

    @Override
    public void setChanged() {
        super.setChanged();
        dirty = true;
    }

    private List<ItemStack> inputStacks() {
        return kind.inputs == 1 ? List.of(items.get(0)) : List.of(items.get(0), items.get(1));
    }

    private boolean powered(ServerLevel level) {
        if (redstone == Redstone.IGNORE) return true;
        return (redstone == Redstone.HIGH) == level.hasNeighborSignal(worldPosition);
    }

    private void findRecipe(ServerLevel level) {
        MachineRecipe.Inputs in = new MachineRecipe.Inputs(inputStacks());
        if (in.isEmpty()) {
            active = null;
            return;
        }
        RecipeManager rm = level.recipeAccess();
        var found = rm.getRecipeFor(Machines.RECIPE_TYPES.get(kind).get(), in, level);
        RecipeHolder<MachineRecipe> next = found.orElse(null);
        if (next != active && (active == null || next == null || !next.id().equals(active.id()))) progress = 0;
        active = next;
    }

    private boolean fits(int slot, ItemStack add) {
        ItemStack cur = items.get(slot);
        if (cur.isEmpty()) return true;
        return ItemStack.isSameItemSameComponents(cur, add) && cur.getCount() + add.getCount() <= cur.getMaxStackSize();
    }

    private boolean canOutput(MachineRecipe r) {
        if (r.result.isPresent() && !fits(Kind.OUT, r.result.get().create())) return false;
        if (r.fluid.isPresent()) {
            if (tank == null) return false;
            MachineRecipe.FluidOut f = r.fluid.get();
            FluidResource cur = tank.getResource(0);
            int amt = tank.getAmountAsInt(0);
            if (!cur.isEmpty() && amt > 0 && cur.getFluid() != f.fluid()) return false;
            if (amt + f.amount() > tank.getCapacityAsInt(0, FluidResource.of(f.fluid()))) return false;
        }
        return true;
    }

    public void serverTick(ServerLevel level) {
        tickCount++;
        if (active != null) {
            if (active.value().assign(inputStacks()) == null) {
                active = null;
                progress = 0;
            }
        } else if (dirty || tickCount % 20 == 0) {
            findRecipe(level);
        }
        dirty = false;

        boolean nowRunning = false;
        draw = 0;
        if (active != null && powered(level)) {
            MachineRecipe r = active.value();
            if (canOutput(r)) {
                double speed = speed();
                int perTick = (int) Math.ceil(energyPerJob(r) * speed / r.time);
                if (energy.getAmountAsInt() >= perTick) {
                    energy.drain(perTick);
                    draw = perTick;
                    speedPct = (int) Math.round(speed * 100);
                    progress += (int) Math.max(1, Math.round(speed * 100));
                    nowRunning = true;
                    if (progress >= r.time * 100) {
                        finish(level, r);
                        progress = 0;
                        active = null;
                        dirty = true;
                    }
                }
            }
        } else if (active == null) {
            progress = 0;
        }
        if (nowRunning != running) {
            running = nowRunning;
            BlockState st = getBlockState();
            if (st.hasProperty(MachineBlock.ACTIVE) && st.getValue(MachineBlock.ACTIVE) != running) {
                level.setBlock(worldPosition, st.setValue(MachineBlock.ACTIVE, running), 3);
            }
        }
        if (running && tickCount % 20 == 0) super.setChanged();
    }

    private void finish(ServerLevel level, MachineRecipe r) {
        int[] slots = r.assign(inputStacks());
        if (slots == null) return;
        for (int i = 0; i < slots.length; i++) {
            ItemStack s = items.get(slots[i]);
            s.shrink(r.inputs.get(i).count());
            if (s.isEmpty()) items.set(slots[i], ItemStack.EMPTY);
        }
        r.result.ifPresent(t -> put(Kind.OUT, t.create()));
        if (r.bonus.isPresent() && level.getRandom().nextFloat() < bonusChance(r)) {
            ItemStack b = r.bonus.get().create();
            if (fits(Kind.BONUS, b)) put(Kind.BONUS, b);
        }
        if (r.fluid.isPresent() && tank != null) {
            MachineRecipe.FluidOut f = r.fluid.get();
            tank.set(0, FluidResource.of(f.fluid()), tank.getAmountAsInt(0) + f.amount());
        }
    }

    private void put(int slot, ItemStack add) {
        ItemStack cur = items.get(slot);
        if (cur.isEmpty()) items.set(slot, add);
        else cur.grow(add.getCount());
    }

    // ---- container -----------------------------------------------------------------------------------------------------------------

    @Override
    public int getContainerSize() {
        return Kind.SIZE;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new MachineMenu(id, inventory, kind, this, data);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot < kind.inputs) return true;
        return slot == Kind.UPGRADE && stack.getItem() instanceof AugmentItem;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public int[] getSlotsForFace(Direction d) {
        int m = itemCfg[d.get3DDataValue()];
        boolean in = (m & IN) != 0, out = (m & OUT) != 0 && !kind.fluid;
        if (in && out) return kind.inputs == 1 ? new int[] {0, 2, 3} : new int[] {0, 1, 2, 3};
        if (in) return kind.inputs == 1 ? new int[] {0} : new int[] {0, 1};
        if (out) return new int[] {2, 3};
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction d) {
        if (d == null) return canPlaceItem(slot, stack);
        return (itemCfg[d.get3DDataValue()] & IN) != 0 && slot < kind.inputs && acceptsInput(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction d) {
        return (itemCfg[d.get3DDataValue()] & OUT) != 0 && (slot == Kind.OUT || slot == Kind.BONUS);
    }

    /** True if some recipe of this machine uses the item as an input. */
    public boolean acceptsInput(ItemStack stack) {
        if (!(level instanceof ServerLevel sl)) return true;
        for (RecipeHolder<MachineRecipe> h : sl.recipeAccess().recipeMap().byType(Machines.RECIPE_TYPES.get(kind).get())) {
            for (MachineRecipe.Input in : h.value().inputs) if (in.ingredient().test(stack)) return true;
        }
        return false;
    }

    // ---- sync to the open menu ----------------------------------------------------------------------------------------------------

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int i) {
            switch (i) {
                case 0: {
                    int total = active == null ? 0 : active.value().time * 100;
                    return total <= 0 ? 0 : Math.min(1000, progress * 1000 / total);
                }
                case 1: return energy.getAmountAsInt() & 0xFFFF;
                case 2: return energy.getAmountAsInt() >>> 16;
                case 3: return (int) energy.getCapacityAsLong() & 0xFFFF;
                case 4: return (int) energy.getCapacityAsLong() >>> 16;
                case 5: return redstone.ordinal();
                case 6: {
                    int v = 0;
                    for (int f = 0; f < 6; f++) v |= itemCfg[f] << (2 * f);
                    return v;
                }
                case 7: {
                    int v = 0;
                    for (int f = 0; f < 6; f++) if (energyCfg[f]) v |= 1 << f;
                    return v;
                }
                case 8: return tank == null ? 0 : tank.getAmountAsInt(0) & 0xFFFF;
                case 9: return tank == null ? 0 : tank.getAmountAsInt(0) >>> 16;
                case 10: return tank == null ? 0 : (int) tank.getCapacityAsLong(0, FluidResource.of(net.minecraft.world.level.material.Fluids.LAVA)) / 100;
                case 11: return tank == null || tank.getResource(0).isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(tank.getResource(0).getFluid());
                case 12: return draw;
                case 13: return running ? speedPct : 0;
                default: return 0;
            }
        }

        @Override
        public void set(int i, int v) {}

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    // ---- save / load -------------------------------------------------------------------------------------------------------------

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, items);
        energy.serialize(out.child("fe"));
        if (tank != null) tank.serialize(out.child("tank"));
        out.putInt("progress", progress);
        out.putInt("redstone", redstone.ordinal());
        out.putIntArray("item_cfg", itemCfg);
        int e = 0;
        for (int f = 0; f < 6; f++) if (energyCfg[f]) e |= 1 << f;
        out.putInt("energy_cfg", e);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        items = NonNullList.withSize(Kind.SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, items);
        in.child("fe").ifPresent(energy::deserialize);
        if (tank != null) in.child("tank").ifPresent(tank::deserialize);
        progress = in.getIntOr("progress", 0);
        redstone = Redstone.values()[Math.floorMod(in.getIntOr("redstone", 0), 3)];
        in.getIntArray("item_cfg").ifPresent(a -> {
            for (int f = 0; f < 6 && f < a.length; f++) itemCfg[f] = a[f] & 3;
        });
        int e = in.getIntOr("energy_cfg", 63);
        for (int f = 0; f < 6; f++) energyCfg[f] = (e & (1 << f)) != 0;
        dirty = true;
    }
}
