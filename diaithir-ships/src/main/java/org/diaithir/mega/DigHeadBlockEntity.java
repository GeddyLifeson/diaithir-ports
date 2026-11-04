package org.diaithir.mega;

import java.util.List;
import java.util.function.DoubleSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.joml.Vector3d;
import org.valkyrienskies.tournament.util.helper.Helper3dKt;

/**
 * Digs the world in front of it from a Valkyrien Skies ship. The head's ship coordinates (and its facing) are converted to world coordinates, the first
 * diggable block within {@link #REACH} metres ahead is the target, and {@link Dig} sets the pace from the linked cab's catalogue row. Drops go through the
 * {@link Assay} roll and into an adjacent container only (any block with an item handler: a pallet, crate, chest, loading dock). The whole drop must fit
 * before the block is removed, in one transaction, so nothing ever spills; a full container stalls the head.
 */
public class DigHeadBlockEntity extends BlockEntity {
    public enum Status { NO_CAB, HALTED, NO_TARGET, BLOCKED, DIGGING }

    public static final int REACH = 2;
    private static final int CAB_RANGE = 12;
    private static final int RESCAN_TICKS = 40;

    private BlockPos cab;
    private int rescan;
    private long dugTotal;
    private Dig dig;
    private String digSpec = "";
    private Status status = Status.NO_CAB;
    /** Watts delivered to the head. Default is the spec's full rating until the power grid supplies a real figure. */
    private DoubleSupplier power;

    public DigHeadBlockEntity(BlockPos pos, BlockState state) { super(MegaMod.DIG_HEAD_BE.get(), pos, state); }

    public Status status() { return status; }

    public long dugTotal() { return dugTotal; }

    public void setPower(DoubleSupplier watts) { power = watts; }

    public void link(BlockPos cabPos) {
        cab = cabPos;
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DigHeadBlockEntity be) {
        if (level instanceof ServerLevel sl) be.serverTick(sl, pos, state);
    }

    private void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        MegaCabBlockEntity c = findCab(level, pos);
        if (c == null || MegaSpecs.get(c.specId()) == null) { status = Status.NO_CAB; return; }
        if (!c.mayRun()) { status = Status.HALTED; return; }
        MegaSpec spec = MegaSpecs.get(c.specId());
        if (dig == null || !digSpec.equals(spec.id())) { dig = new Dig(spec); digSpec = spec.id(); }

        Direction face = state.getValue(DigHeadBlock.FACING);
        BlockPos target = target(level, pos, face);
        if (target == null) { status = Status.NO_TARGET; return; }
        List<ResourceHandler<ItemResource>> outs = outputs(level, pos, face);
        if (outs.isEmpty()) { status = Status.BLOCKED; return; }

        BlockState ts = level.getBlockState(target);
        String blockId = BuiltInRegistries.BLOCK.getKey(ts.getBlock()).toString();
        Deposits store = Deposits.get(level.getServer());
        String key = Deposits.key(level.dimension().toString(), target.getX() >> 4, target.getZ() >> 4, blockId);
        Dig.Deposit dep = store.deposit(key);
        double watts = power != null ? power.getAsDouble() : spec.powerMW() * 1e6;

        int blocks = dig.tick(watts, 1, dep);
        if (blocks == 0) { status = dep.left() <= 0 ? Status.NO_TARGET : Status.DIGGING; store.store(key, dep); return; }
        // Sample the assay on the deposit as it stood before this block, then deliver or give the volume back.
        double fractionBefore = (dep.left() + blocks) / dep.initial();
        if (deliver(level, target, ts, blockId, fractionBefore, outs)) {
            dugTotal++;
            status = Status.DIGGING;
        } else {
            dig.refund(blocks, dep);
            status = Status.BLOCKED;
        }
        store.store(key, dep);
    }

    /** Ship coordinates to world: the world cell {@code k} metres ahead of the head along its (ship-rotated) facing, first diggable one. */
    private BlockPos target(ServerLevel level, BlockPos pos, Direction face) {
        Vector3d c = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        Vector3d w = Helper3dKt.convertShipToWorldSpace(level, c);
        Vector3d ahead = Helper3dKt.convertShipToWorldSpace(level, new Vector3d(c).add(face.getStepX(), face.getStepY(), face.getStepZ()));
        Vector3d dir = ahead.sub(w);
        for (int k = 1; k <= REACH; k++) {
            BlockPos p = BlockPos.containing(w.x + dir.x * k, w.y + dir.y * k, w.z + dir.z * k);
            BlockState s = level.getBlockState(p);
            if (!s.isAir() && s.getFluidState().isEmpty() && s.getDestroySpeed(level, p) >= 0) return p;
        }
        return null;
    }

    /** Every adjacent item handler except the cutting face. */
    private List<ResourceHandler<ItemResource>> outputs(ServerLevel level, BlockPos pos, Direction face) {
        List<ResourceHandler<ItemResource>> out = new java.util.ArrayList<>();
        for (Direction d : Direction.values()) {
            if (d == face) continue;
            ResourceHandler<ItemResource> h = level.getCapability(Capabilities.Item.BLOCK, pos.relative(d), d.getOpposite());
            if (h != null) out.add(h);
        }
        return out;
    }

    /** Insert all drops (after the assay roll) in one transaction; only then remove the block. */
    private boolean deliver(ServerLevel level, BlockPos target, BlockState ts, String blockId, double fraction, List<ResourceHandler<ItemResource>> outs) {
        Assay.Reading reading = Assay.read(blockId);
        List<ItemStack> drops = Block.getDrops(ts, level, target, level.getBlockEntity(target));
        try (Transaction tx = Transaction.openRoot()) {
            for (ItemStack d : drops) {
                int left = Assay.apply(d.getCount(), reading, fraction);
                ItemResource res = ItemResource.of(d);
                for (ResourceHandler<ItemResource> h : outs) {
                    if (left <= 0) break;
                    left -= h.insert(res, left, tx);
                }
                if (left > 0) return false;                  // does not fit: transaction closes uncommitted, block stays
            }
            tx.commit();
        }
        level.removeBlock(target, false);
        return true;
    }

    private MegaCabBlockEntity findCab(ServerLevel level, BlockPos pos) {
        if (cab != null && level.getBlockEntity(cab) instanceof MegaCabBlockEntity c) return c;
        if (rescan-- > 0) return null;
        rescan = RESCAN_TICKS;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-CAB_RANGE, -CAB_RANGE, -CAB_RANGE), pos.offset(CAB_RANGE, CAB_RANGE, CAB_RANGE))) {
            if (level.getBlockEntity(p) instanceof MegaCabBlockEntity c) {
                cab = p.immutable();
                setChanged();
                return c;
            }
        }
        return null;
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        dugTotal = in.getLongOr("dug", 0L);
        int[] c = in.getIntArray("cab").orElse(new int[0]);
        cab = c.length == 3 ? new BlockPos(c[0], c[1], c[2]) : null;
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putLong("dug", dugTotal);
        if (cab != null) out.putIntArray("cab", new int[] {cab.getX(), cab.getY(), cab.getZ()});
    }
}
