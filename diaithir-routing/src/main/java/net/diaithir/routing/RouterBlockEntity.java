package net.diaithir.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

/** Router state: up to nine modules, a router-wide filter, range cards and links to other routers in the same dimension. */
public final class RouterBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_MODULES = 9;
    public static final int MAX_LINKS = 8;

    private record Installed(ModuleKind kind, FilterData filter) {
    }

    private final List<Installed> modules = new ArrayList<>();
    private final List<GlobalPos> links = new ArrayList<>();
    private FilterData filter = FilterData.EMPTY;
    private int rangeCards;
    private int cooldown;
    private int roundRobin;

    public RouterBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirRouting.ROUTER_BE.get(), pos, state);
    }

    public boolean installModule(ModuleKind kind, FilterData data) {
        if (modules.size() >= MAX_MODULES) return false;
        modules.add(new Installed(kind, data));
        setChanged();
        return true;
    }

    private static ItemStack toStack(Installed i) {
        ItemStack out = new ItemStack(DiaithirRouting.MODULES.get(i.kind().ordinal()).get());
        if (!i.filter().entries().isEmpty() || i.filter().blacklist()) out.set(DiaithirRouting.FILTER_DATA.get(), i.filter());
        return out;
    }

    public ItemStack popModule() {
        if (modules.isEmpty()) return ItemStack.EMPTY;
        Installed i = modules.remove(modules.size() - 1);
        setChanged();
        return toStack(i);
    }

    /** Menu view of module slot i: the module item with its filter component, or empty. */
    public ItemStack moduleStack(int i) {
        return i >= 0 && i < modules.size() ? toStack(modules.get(i)) : ItemStack.EMPTY;
    }

    /** Sets module slot i from a stack; an empty or non-module stack clears it. Slots stay packed so the list never has holes. */
    public void setModuleStack(int i, ItemStack s) {
        if (i < 0 || i >= MAX_MODULES) return;
        if (s.isEmpty() || !(s.getItem() instanceof ModuleItem m)) {
            if (i < modules.size()) modules.remove(i);
        } else {
            Installed in = new Installed(m.kind(), s.getOrDefault(RoutingRefs.filterData(), FilterData.EMPTY));
            if (i < modules.size()) modules.set(i, in);
            else if (modules.size() < MAX_MODULES) modules.add(in);
        }
        setChanged();
    }

    public int moduleCount() {
        return modules.size();
    }

    public void addRangeCard() {
        rangeCards++;
        setChanged();
    }

    public int range() {
        return Config.BASE_RANGE.get() + rangeCards * Config.RANGE_PER_CARD.get();
    }

    public FilterData filter() {
        return filter;
    }

    public void setFilter(FilterData f) {
        filter = f;
        setChanged();
    }

    /** Sets one router-wide filter flag: 0 blacklist, 1 match tag, 2 match durability, 3 ignore data. Flips it. */
    public void toggleFlag(int flag) {
        switch (flag) {
            case 0 -> filter = filter.withBlacklist(!filter.blacklist());
            case 1 -> filter = filter.withMatchTag(!filter.matchTag());
            case 2 -> filter = filter.withMatchDurability(!filter.matchDurability());
            case 3 -> filter = filter.withIgnoreData(!filter.ignoreData());
            default -> {
                return;
            }
        }
        setChanged();
    }

    public boolean link(GlobalPos other) {
        if (links.contains(other)) return true;
        if (links.size() >= MAX_LINKS) return false;
        if (other.pos().distSqr(worldPosition) > (double) range() * range()) return false;
        links.add(other);
        setChanged();
        return true;
    }

    public void clearLinks() {
        links.clear();
        setChanged();
    }

    public String describe() {
        StringBuilder sb = new StringBuilder("Router: ").append(modules.size()).append("/").append(MAX_MODULES).append(" modules [");
        for (int i = 0; i < modules.size(); i++) sb.append(i == 0 ? "" : ", ").append(modules.get(i).kind().id());
        return sb.append("], range ").append(range()).append(", ").append(links.size()).append(" links, filter ").append(filter.describe()).toString();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.diaithirrouting.router");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new RouterMenu(id, inv, this);
    }

    public void dropContents(ServerLevel level, BlockPos pos) {
        while (!modules.isEmpty()) {
            ItemStack s = popModule();
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, s));
        }
        for (int i = 0; i < rangeCards; i++) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(DiaithirRouting.RANGE_CARD.get())));
        }
        rangeCards = 0;
    }

    public void serverTick(ServerLevel level) {
        if (modules.isEmpty() || ++cooldown < Config.OPERATION_INTERVAL.get()) return;
        cooldown = 0;
        List<Direction> sides = adjacentSides(level);
        for (Installed m : new ArrayList<>(modules)) run(level, m, sides);
    }

    private List<Direction> adjacentSides(ServerLevel level) {
        List<Direction> out = new ArrayList<>();
        for (Direction d : Direction.values()) {
            BlockEntity n = level.getBlockEntity(worldPosition.relative(d));
            if (n != null && !(n instanceof RouterBlockEntity)) out.add(d);
        }
        return out;
    }

    private List<RouterBlockEntity> linked(ServerLevel level) {
        List<RouterBlockEntity> out = new ArrayList<>();
        double r2 = (double) range() * range();
        for (GlobalPos g : links) {
            if (!g.dimension().equals(level.dimension()) || g.pos().distSqr(worldPosition) > r2 || !level.isLoaded(g.pos())) continue;
            if (level.getBlockEntity(g.pos()) instanceof RouterBlockEntity r) out.add(r);
        }
        return out;
    }

    private boolean accepts(Installed m, ItemStack s) {
        return filter.allows(s) && m.filter().allows(s);
    }

    private void run(ServerLevel level, Installed m, List<Direction> sides) {
        int items = Config.ITEMS_PER_OPERATION.get();
        switch (m.kind()) {
            case EXTRACTOR, SENDER, FILTER -> pushItems(level, m, sides, items, false);
            case PULLER -> pullItems(level, m, sides, items);
            case SORTER -> pushItems(level, m, sides, items, true);
            case ROUND_ROBIN -> roundRobin(level, m, sides, items);
            case VACUUM -> vacuum(level, m, sides, items);
            case VOID -> voidAll(level, m, sides, items);
            case FLUID -> pushFluid(level, sides);
            case ENERGY -> pushEnergy(level, sides);
        }
    }

    /** Moves from every adjacent inventory to linked routers' inventories first, then to other adjacent inventories. In sorter mode only wanted items move to a destination that wants them. */
    private void pushItems(ServerLevel level, Installed m, List<Direction> sides, int budget, boolean sorter) {
        for (Direction from : sides) {
            if (budget <= 0) return;
            ResourceHandler<ItemResource> src = Transfers.items(level, worldPosition.relative(from), from.getOpposite());
            if (src == null) continue;
            for (RouterBlockEntity other : linked(level)) {
                for (Direction od : other.adjacentSides(level)) {
                    ResourceHandler<ItemResource> dst = Transfers.items(level, other.worldPosition.relative(od), od.getOpposite());
                    if (dst != null) budget -= Transfers.moveItems(src, dst, budget, s -> accepts(m, s) && other.filter.allows(s)
                            && (!sorter || other.filter.entries().isEmpty() || !other.filter.blacklist()));
                }
            }
            for (Direction to : sides) {
                if (to == from || budget <= 0) continue;
                ResourceHandler<ItemResource> dst = Transfers.items(level, worldPosition.relative(to), to.getOpposite());
                if (dst != null) budget -= Transfers.moveItems(src, dst, budget, s -> accepts(m, s));
            }
        }
    }

    private void pullItems(ServerLevel level, Installed m, List<Direction> sides, int budget) {
        for (RouterBlockEntity other : linked(level)) {
            for (Direction od : other.adjacentSides(level)) {
                ResourceHandler<ItemResource> src = Transfers.items(level, other.worldPosition.relative(od), od.getOpposite());
                if (src == null) continue;
                for (Direction to : sides) {
                    ResourceHandler<ItemResource> dst = Transfers.items(level, worldPosition.relative(to), to.getOpposite());
                    if (dst != null) budget -= Transfers.moveItems(src, dst, budget, s -> accepts(m, s) && other.filter.allows(s));
                    if (budget <= 0) return;
                }
            }
        }
    }

    private void roundRobin(ServerLevel level, Installed m, List<Direction> sides, int budget) {
        if (sides.size() < 2) return;
        Direction from = sides.get(roundRobin % sides.size());
        Direction to = sides.get((roundRobin + 1) % sides.size());
        roundRobin = (roundRobin + 1) % sides.size();
        ResourceHandler<ItemResource> src = Transfers.items(level, worldPosition.relative(from), from.getOpposite());
        ResourceHandler<ItemResource> dst = Transfers.items(level, worldPosition.relative(to), to.getOpposite());
        if (src != null && dst != null) Transfers.moveItems(src, dst, budget, s -> accepts(m, s));
    }

    private void vacuum(ServerLevel level, Installed m, List<Direction> sides, int budget) {
        int r = Math.min(range(), 8);
        AABB box = new AABB(worldPosition).inflate(r);
        for (ItemEntity e : level.getEntitiesOfClass(ItemEntity.class, box)) {
            if (budget <= 0) return;
            ItemStack s = e.getItem();
            if (s.isEmpty() || !accepts(m, s)) continue;
            for (Direction to : sides) {
                ResourceHandler<ItemResource> dst = Transfers.items(level, worldPosition.relative(to), to.getOpposite());
                if (dst == null) continue;
                ItemResource res = ItemResource.of(s);
                int want = Math.min(s.getCount(), budget);
                int in;
                try (Transaction tx = Transaction.openRoot()) {
                    in = dst.insert(res, want, tx);
                    if (in > 0) tx.commit();
                }
                if (in > 0) {
                    s.shrink(in);
                    budget -= in;
                    if (s.isEmpty()) e.discard();
                    break;
                }
            }
        }
    }

    private void voidAll(ServerLevel level, Installed m, List<Direction> sides, int budget) {
        for (Direction d : sides) {
            ResourceHandler<ItemResource> src = Transfers.items(level, worldPosition.relative(d), d.getOpposite());
            if (src != null) budget -= Transfers.voidItems(src, budget, s -> accepts(m, s));
            if (budget <= 0) return;
        }
    }

    private void pushFluid(ServerLevel level, List<Direction> sides) {
        int amount = Config.FLUID_PER_OPERATION.get();
        for (Direction from : sides) {
            ResourceHandler<FluidResource> src = Transfers.fluids(level, worldPosition.relative(from), from.getOpposite());
            if (src == null) continue;
            for (RouterBlockEntity other : linked(level)) {
                for (Direction od : other.adjacentSides(level)) {
                    ResourceHandler<FluidResource> dst = Transfers.fluids(level, other.worldPosition.relative(od), od.getOpposite());
                    if (dst != null && Transfers.moveFluid(src, dst, amount) > 0) return;
                }
            }
            for (Direction to : sides) {
                if (to == from) continue;
                ResourceHandler<FluidResource> dst = Transfers.fluids(level, worldPosition.relative(to), to.getOpposite());
                if (dst != null && Transfers.moveFluid(src, dst, amount) > 0) return;
            }
        }
    }

    private void pushEnergy(ServerLevel level, List<Direction> sides) {
        int amount = Config.ENERGY_PER_OPERATION.get();
        for (Direction from : sides) {
            EnergyHandler src = Transfers.energy(level, worldPosition.relative(from), from.getOpposite());
            if (src == null) continue;
            for (RouterBlockEntity other : linked(level)) {
                for (Direction od : other.adjacentSides(level)) {
                    EnergyHandler dst = Transfers.energy(level, other.worldPosition.relative(od), od.getOpposite());
                    if (dst != null && Transfers.moveEnergy(src, dst, amount) > 0) return;
                }
            }
            for (Direction to : sides) {
                if (to == from) continue;
                EnergyHandler dst = Transfers.energy(level, worldPosition.relative(to), to.getOpposite());
                if (dst != null && Transfers.moveEnergy(src, dst, amount) > 0) return;
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putInt("range_cards", rangeCards);
        out.store("filter", FilterData.CODEC, filter);
        List<Integer> kinds = new ArrayList<>();
        List<FilterData> filters = new ArrayList<>();
        for (Installed i : modules) {
            kinds.add(i.kind().ordinal());
            filters.add(i.filter());
        }
        out.store("module_kinds", com.mojang.serialization.Codec.INT.listOf(), kinds);
        out.store("module_filters", FilterData.CODEC.listOf(), filters);
        out.store("links", GlobalPos.CODEC.listOf(), new ArrayList<>(links));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        rangeCards = in.getIntOr("range_cards", 0);
        filter = in.read("filter", FilterData.CODEC).orElse(FilterData.EMPTY);
        modules.clear();
        List<Integer> kinds = in.read("module_kinds", com.mojang.serialization.Codec.INT.listOf()).orElse(List.of());
        List<FilterData> filters = in.read("module_filters", FilterData.CODEC.listOf()).orElse(List.of());
        ModuleKind[] all = ModuleKind.values();
        for (int i = 0; i < kinds.size() && i < MAX_MODULES; i++) {
            int k = kinds.get(i);
            if (k >= 0 && k < all.length) modules.add(new Installed(all[k], i < filters.size() ? filters.get(i) : FilterData.EMPTY));
        }
        links.clear();
        in.read("links", GlobalPos.CODEC.listOf()).ifPresent(links::addAll);
    }
}
