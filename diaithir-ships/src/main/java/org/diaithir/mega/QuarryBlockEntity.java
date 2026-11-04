package org.diaithir.mega;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.Tags;
import org.diaithir.mega.cleanroom.DrillHead;
import org.diaithir.mega.cleanroom.QuarryPlan;

public class QuarryBlockEntity extends BlockEntity {
    public enum Status { RUNNING, NO_HEAD, HEAD_TOO_SOFT, FULL, DONE }

    public static final int HALF = 2;            // footprint 5 x 5
    public static final int DEPTH = 16;
    public static final int SLOTS = 27;
    private static final String DEFAULT_SPEC = "megamachines:walking_dragline";

    private final NonNullList<ItemStack> inv = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    private String specId = DEFAULT_SPEC;
    private int head = -1;
    private double wear, carry;
    private long dugSaved;
    private QuarryPlan plan;
    private Status status = Status.RUNNING;

    public QuarryBlockEntity(BlockPos pos, BlockState state) { super(MegaBlocks.QUARRY_BE.get(), pos, state); }

    public Status status() { return status; }
    public long dug() { return plan == null ? dugSaved : plan.dugCells(); }
    public long total() { return plan == null ? 0 : plan.totalCells(); }
    public void setSpec(String id) { if (MegaRates.get(id) != null) { specId = id; setChanged(); } }
    public DrillHead head() { return head < 0 ? null : DrillHead.values()[head]; }

    public boolean installHead(DrillHead h) {
        if (head >= 0) return false;
        head = h.ordinal(); wear = 0; setChanged();
        return true;
    }

    private void ensurePlan() {
        if (plan != null || level == null) return;
        BlockPos p = worldPosition;
        int top = p.getY() - 1, floor = Math.max(level.getMinY(), top - DEPTH + 1);
        plan = new QuarryPlan(p.getX() - HALF, p.getZ() - HALF, 2 * HALF + 1, 2 * HALF + 1, top, floor);
        for (long i = 0; i < dugSaved; i++) plan.next();
    }

    /** One server tick: accumulate rate x head factor, then dig whole cells. */
    public void serverTick() {
        if (level == null || level.isClientSide()) return;
        ensurePlan();
        if (plan.done()) { status = Status.DONE; return; }
        DrillHead h = head();
        if (h == null) { status = Status.NO_HEAD; return; }
        MegaRates.Row row = MegaRates.get(specId);
        carry += row.digM3s() / 20.0 * h.speed;
        // look at the next cell without consuming it until we can dig it
        while (carry >= 1.0 && !plan.done()) {
            int[] c = plan.peek();
            BlockPos cp = new BlockPos(c[0], c[1], c[2]);
            BlockState s = level.getBlockState(cp);
            if (s.isAir() || s.getDestroySpeed(level, cp) < 0 || !s.getFluidState().isEmpty()) { plan.next(); continue; }   // nothing to dig
            String medium = mediumOf(s);
            if (!h.canCut(medium)) { status = Status.HEAD_TOO_SOFT; carry = Math.min(carry, 1.0); return; }
            var drops = Block.getDrops(s, (net.minecraft.server.level.ServerLevel) level, cp, level.getBlockEntity(cp));
            if (!fits(drops)) { status = Status.FULL; carry = Math.min(carry, 1.0); return; }   // hopper-limited: nothing spills
            for (ItemStack d : drops) insert(d);
            level.destroyBlock(cp, false);
            plan.next();
            carry -= 1.0;
            wear += h.wear(medium, 1.0);
            if (wear >= 1.0) { head = -1; wear = 0; status = Status.NO_HEAD; setChanged(); return; }
        }
        status = Status.RUNNING;
        setChanged();
    }

    public static String mediumOf(BlockState s) {
        if (s.is(Tags.Blocks.ORES)) return "ore";
        if (s.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL)) return "soil";
        if (s.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE)) return "rock";
        return "overburden";
    }

    private boolean fits(Iterable<ItemStack> drops) {
        NonNullList<ItemStack> sim = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < SLOTS; i++) sim.set(i, inv.get(i).copy());
        for (ItemStack d : drops) if (!put(sim, d.copy())) return false;
        return true;
    }

    private void insert(ItemStack d) { put(inv, d.copy()); }

    private static boolean put(NonNullList<ItemStack> to, ItemStack s) {
        for (int i = 0; i < to.size() && !s.isEmpty(); i++) {
            ItemStack t = to.get(i);
            if (!t.isEmpty() && ItemStack.isSameItemSameComponents(t, s) && t.getCount() < t.getMaxStackSize()) {
                int m = Math.min(s.getCount(), t.getMaxStackSize() - t.getCount());
                t.grow(m); s.shrink(m);
            }
        }
        for (int i = 0; i < to.size() && !s.isEmpty(); i++) if (to.get(i).isEmpty()) { to.set(i, s.copy()); s.setCount(0); }
        return s.isEmpty();
    }

    /** Hand the stored output to the player. Returns stacks moved. */
    public int give(Player p) {
        int n = 0;
        for (int i = 0; i < SLOTS; i++) {
            if (inv.get(i).isEmpty()) continue;
            ItemStack s = inv.get(i).copy();
            if (!p.getInventory().add(s)) { inv.set(i, s); continue; }
            inv.set(i, ItemStack.EMPTY); n++;
        }
        setChanged();
        return n;
    }

    public int storedCount() { int n = 0; for (ItemStack s : inv) n += s.getCount(); return n; }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        specId = in.getStringOr("spec", DEFAULT_SPEC);
        head = in.getIntOr("head", -1);
        wear = in.getDoubleOr("wear", 0);
        carry = in.getDoubleOr("carry", 0);
        dugSaved = in.getLongOr("dug", 0);
        plan = null;
        inv.clear();
        ContainerHelper.loadAllItems(in, inv);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("spec", specId);
        out.putInt("head", head);
        out.putDouble("wear", wear);
        out.putDouble("carry", carry);
        out.putLong("dug", dug());
        ContainerHelper.saveAllItems(out, inv);
    }
}
