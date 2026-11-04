package net.diaithir.automationcrafting.packages;

import com.mojang.serialization.Codec;
import net.diaithir.automationcrafting.DiaithirAutomationCrafting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * State of one bench. target is a package id. Researcher: need and packNeed are what research still lacks. Packager: need is what the crate still lacks.
 * The output buffer holds finished crates (Packager) or unpacked goods (Unpackager). Everything that changes only touches fields, so a failed
 * transaction rolls it back; finishing a research (which writes world data) is done by the tick.
 */
public final class BenchEntity extends BlockEntity {
    static final int MAX_CRATES = 64;
    private static final Codec<Map<String, Integer>> MAP = Codec.unboundedMap(Codec.STRING, Codec.INT);

    String target = "";
    Tally need = new Tally();
    Tally packNeed = new Tally();
    String outItem = "";
    String outCrate = "";
    int outCount;
    private final Io io = new Io();

    public ResourceHandler<ItemResource> io() {
        return io;
    }

    public BenchEntity(BlockPos pos, BlockState state) {
        super(DiaithirAutomationCrafting.BENCH_BE.get(), pos, state);
    }

    Kind kind() {
        return ((BenchBlock) getBlockState().getBlock()).kind;
    }

    // ---- hand use ------------------------------------------------------------------------------------------------------------------

    public boolean insertByHand(ItemStack stack) {
        try (Transaction tx = Transaction.openRoot()) {
            int n = io.insert(0, ItemResource.of(stack), stack.getCount(), tx);
            if (n <= 0) return false;
            tx.commit();
            stack.shrink(n);
            return true;
        }
    }

    public boolean hasOutput() {
        return outCount > 0;
    }

    public List<ItemStack> takeOutput() {
        List<ItemStack> out = new ArrayList<>();
        int left = outCount;
        while (left > 0) {
            ItemStack s = outStack(Math.min(left, 64));
            left -= s.getCount();
            out.add(s);
        }
        outCount = 0;
        outItem = "";
        outCrate = "";
        sync();
        return out;
    }

    /** Picks the next package this bench can work on. Returns the message to show. */
    public String cycle() {
        Kind k = kind();
        if (k == Kind.UNPACKAGER) return "An Unpackager needs no setting. Put a package in it.";
        if (!(level instanceof ServerLevel sl)) return "";
        ResearchState rs = ResearchState.get(sl.getServer());
        List<PackageDef> choices = new ArrayList<>();
        for (PackageDef d : Packages.all()) {
            boolean ok = k == Kind.RESEARCHER ? !rs.has(d.id()) && rs.available(d) : rs.has(d.id());
            if (ok) choices.add(d);
        }
        if (choices.isEmpty()) return k == Kind.RESEARCHER ? "Nothing left to research." : "Nothing researched yet. Use a Researcher first.";
        int at = -1;
        for (int i = 0; i < choices.size(); i++) if (choices.get(i).id().equals(target)) at = i;
        PackageDef next = choices.get((at + 1) % choices.size());
        refund();
        setTarget(next);
        sync();
        return describe();
    }

    public String clear() {
        refund();
        target = "";
        need = new Tally();
        packNeed = new Tally();
        sync();
        return "Cleared. Anything put in was handed back.";
    }

    private void setTarget(PackageDef d) {
        target = d.id();
        if (kind() == Kind.RESEARCHER) {
            need = new Tally(d.researchItems());
            packNeed = new Tally(d.researchPacks());
        } else {
            need = new Tally(d.inputs());
            packNeed = new Tally();
        }
    }

    /** Drops what was already put in for the current target. */
    private void refund() {
        PackageDef d = Packages.get(target);
        if (d == null || !(level instanceof ServerLevel sl)) return;
        List<PackageDef.Req> reqs = kind() == Kind.RESEARCHER ? d.researchItems() : d.inputs();
        for (PackageDef.Req r : reqs) drop(sl, Packages.itemOf(r.item()), r.count() - need.view().getOrDefault(r.item(), 0), null);
        if (kind() == Kind.RESEARCHER) {
            for (PackageDef.Req r : d.researchPacks()) {
                PackageDef pd = Packages.get(r.item());
                if (pd != null) drop(sl, null, r.count() - packNeed.view().getOrDefault(r.item(), 0), pd);
            }
        }
    }

    private void drop(ServerLevel sl, net.minecraft.world.item.Item item, int n, PackageDef crate) {
        while (n > 0) {
            ItemStack s = crate != null ? Packages.crate(crate, Math.min(n, 16)) : new ItemStack(item, Math.min(n, item.getDefaultMaxStackSize()));
            n -= s.getCount();
            sl.addFreshEntity(new ItemEntity(sl, worldPosition.getX() + 0.5, worldPosition.getY() + 1.1, worldPosition.getZ() + 0.5, s));
        }
    }

    public String describe() {
        PackageDef d = Packages.get(target);
        switch (kind()) {
            case UNPACKAGER:
                return outCount > 0 ? "Holds " + outCount + " x " + outItem + ". Use empty-handed to take it." : "Idle. Put a package in it.";
            case RESEARCHER:
                if (d == null) return "Idle. Use a Package Reader to pick what to research.";
                return "Researching " + d.name() + ". Missing: " + missing();
            default:
                if (d == null) return "Idle. Use a Package Reader to pick a researched package." + (outCount > 0 ? " Holds " + outCount + " finished." : "");
                return "Packing " + d.name() + ". Missing: " + missing() + (outCount > 0 ? ". Holds " + outCount + " finished." : "");
        }
    }

    private String missing() {
        List<String> parts = new ArrayList<>();
        need.view().forEach((k, v) -> parts.add(v + " " + k));
        packNeed.view().forEach((k, v) -> {
            PackageDef p = Packages.get(k);
            parts.add(v + " " + (p == null ? k : p.name()) + " package");
        });
        return parts.isEmpty() ? "nothing" : String.join(", ", parts);
    }

    ItemStack outStack(int n) {
        if (!outCrate.isEmpty()) {
            PackageDef d = Packages.get(outCrate);
            return d == null ? ItemStack.EMPTY : Packages.crate(d, n);
        }
        return outItem.isEmpty() ? ItemStack.EMPTY : new ItemStack(Packages.itemOf(outItem), n);
    }

    // ---- tick ----------------------------------------------------------------------------------------------------------------------

    static void tick(Level level, BlockPos pos, BlockState state, BenchEntity be) {
        if (!(level instanceof ServerLevel sl) || (sl.getGameTime() + pos.asLong()) % 10 != 0) return;
        PackageDef d = Packages.get(be.target);
        if (d == null) return;
        Kind k = be.kind();
        if (k == Kind.RESEARCHER && be.need.done() && be.packNeed.done()) {
            ResearchState.get(sl.getServer()).set(d.id(), true);
            be.target = "";
            be.need = new Tally();
            be.packNeed = new Tally();
            be.sync();
        } else if (k == Kind.PACKAGER && be.need.done() && be.outCount < MAX_CRATES && (be.outCrate.isEmpty() || be.outCrate.equals(d.id()))) {
            be.outCrate = d.id();
            be.outItem = "";
            be.outCount++;
            be.need = new Tally(d.inputs());
            be.sync();
        }
    }

    // ---- item handler --------------------------------------------------------------------------------------------------------------

    /** Slot 0 takes inputs, slot 1 gives the output. Hoppers, pipes and belts see the same thing. */
    final class Io extends SnapshotJournal<Object[]> implements ResourceHandler<ItemResource> {
        @Override
        protected Object[] createSnapshot() {
            return new Object[] {target, need.copy(), packNeed.copy(), outItem, outCrate, outCount};
        }

        @Override
        protected void revertToSnapshot(Object[] s) {
            target = (String) s[0];
            need = (Tally) s[1];
            packNeed = (Tally) s[2];
            outItem = (String) s[3];
            outCrate = (String) s[4];
            outCount = (Integer) s[5];
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
            if (index != 1 || outCount <= 0) return ItemResource.EMPTY;
            ItemStack s = outStack(1);
            return s.isEmpty() ? ItemResource.EMPTY : ItemResource.of(s);
        }

        @Override
        public long getAmountAsLong(int index) {
            return index == 1 ? outCount : 0;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return index == 1 ? outCount : wants(resource) ? Integer.MAX_VALUE : 0;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return index == 0 && wants(resource);
        }

        private boolean wants(ItemResource r) {
            ItemStack s = r.toStack(1);
            String crate = Packages.crateId(s);
            return switch (kind()) {
                case UNPACKAGER -> outCount == 0 && Packages.get(crate) != null;
                case RESEARCHER -> crate.isEmpty() ? need.wants(Packages.keyOf(s.getItem())) : packNeed.wants(crate);
                case PACKAGER -> crate.isEmpty() && outCount < MAX_CRATES && need.wants(Packages.keyOf(s.getItem()));
            };
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
            if (index != 0 || amount < 1 || !wants(resource)) return 0;
            ItemStack s = resource.toStack(1);
            String crate = Packages.crateId(s);
            updateSnapshots(tx);
            if (kind() == Kind.UNPACKAGER) {
                PackageDef d = Packages.get(crate);
                outItem = Packages.keyOf(Packages.itemOf(d.result().item()));
                outCrate = "";
                outCount = d.result().count();
                return 1;
            }
            return crate.isEmpty() ? need.accept(Packages.keyOf(s.getItem()), amount) : packNeed.accept(crate, amount);
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
            if (index != 1 || outCount <= 0 || amount < 1) return 0;
            ItemStack have = outStack(1);
            if (have.isEmpty() || !resource.matches(have)) return 0;
            int n = Math.min(amount, outCount);
            updateSnapshots(tx);
            outCount -= n;
            if (outCount == 0) {
                outItem = "";
                outCrate = "";
            }
            return n;
        }
    }

    // ---- saving --------------------------------------------------------------------------------------------------------------------

    private void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("target", target);
        out.store("need", MAP, new LinkedHashMap<>(need.view()));
        out.store("packNeed", MAP, new LinkedHashMap<>(packNeed.view()));
        out.putString("outItem", outItem);
        out.putString("outCrate", outCrate);
        out.putInt("outCount", outCount);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        target = in.getStringOr("target", "");
        need = new Tally(in.read("need", MAP).orElse(Map.of()));
        packNeed = new Tally(in.read("packNeed", MAP).orElse(Map.of()));
        outItem = in.getStringOr("outItem", "");
        outCrate = in.getStringOr("outCrate", "");
        outCount = in.getIntOr("outCount", 0);
    }
}
