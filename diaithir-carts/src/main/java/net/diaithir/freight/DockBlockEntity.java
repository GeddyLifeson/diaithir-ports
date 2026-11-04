package net.diaithir.freight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** See {@link LoadingDock}. Rift docks register by channel while loaded; the partner is looked up on every access, so nothing goes stale. */
public final class DockBlockEntity extends BlockEntity {
    public static final int CHANNELS = 64;
    private static final List<DockBlockEntity> RIFTS = new CopyOnWriteArrayList<>();
    private int channel = 1;

    public DockBlockEntity(BlockPos pos, BlockState state) { super(Freight.DOCK_BE.get(), pos, state); }

    private boolean rift() { return getBlockState().getBlock() instanceof LoadingDock d && d.rift; }

    @Override
    public void setLevel(net.minecraft.world.level.Level level) {
        super.setLevel(level);
        if (!level.isClientSide() && rift() && !RIFTS.contains(this)) RIFTS.add(this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        RIFTS.remove(this);
    }

    public int cycleChannel(int by) {
        channel = Math.floorMod(channel - 1 + by, CHANNELS) + 1;
        setChanged();
        return channel;
    }

    public int channel() { return channel; }

    /** The dock whose container this one serves: itself when it is a plain dock, the partner when it is a rift dock. */
    private @Nullable DockBlockEntity source() {
        if (!rift()) return this;
        for (DockBlockEntity o : RIFTS) {
            if (o != this && !o.isRemoved() && o.channel == channel && o.level != null && o.level.isLoaded(o.worldPosition)) return o;
        }
        return null;
    }

    private @Nullable ContainerCoreEntity container() {
        DockBlockEntity s = source();
        if (s == null || s.level == null) return null;
        Direction facing = s.getBlockState().getValue(LoadingDock.FACING);
        return Containers.findAlong(s.level, s.worldPosition, facing, 3);
    }

    /** Every load's items as one Container: the deployed container ahead, else a carrier (trailer, flatcar, hold) ahead. */
    private @Nullable Container view() {
        DockBlockEntity s = source();
        if (s == null || s.level == null) return null;
        Direction facing = s.getBlockState().getValue(LoadingDock.FACING);
        ContainerCoreEntity c = Containers.findAlong(s.level, s.worldPosition, facing, 3);
        if (c != null) return Containers.view(s.level, c);
        CargoCarrier k = Carriers.near(s.level, s.worldPosition, facing, 3);
        return k == null ? null : Carriers.view(k);
    }

    public @Nullable ResourceHandler<ItemResource> items() {
        Container v = view();
        return v == null ? null : VanillaContainerWrapper.of(v);
    }

    public @Nullable ResourceHandler<FluidResource> fluids() {
        ContainerCoreEntity c = container();
        return c == null ? null : c.tank();
    }

    /** 0 to 15: share of the container's item slots that hold something. */
    public int fillSignal() {
        Container v = view();
        if (v == null) return 0;
        int used = 0, n = v.getContainerSize();
        for (int i = 0; i < n; i++) if (!v.getItem(i).isEmpty()) used++;
        return n == 0 ? 0 : 1 + (int) (14L * used / n);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putInt("channel", channel);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        channel = Math.max(1, Math.min(CHANNELS, in.getIntOr("channel", 1)));
    }
}
