package net.diaithir.linkedstorage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

/** One linked block. It holds only owner, frequency and lock; the contents live in LinkedData under the channel key. */
public final class LinkedBlockEntity extends BlockEntity {
    private UUID owner = ChannelKey.PUBLIC_SCOPE;
    private int frequency;
    private Lock lock = Lock.OWNER;
    private final SharedHandlers.Items items = new SharedHandlers.Items(this);
    private final SharedHandlers.Fluids fluids = new SharedHandlers.Fluids(this);
    private final SharedHandlers.Energy energy = new SharedHandlers.Energy(this);

    public LinkedBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirLinkedStorage.LINKED_BE.get(), pos, state);
    }

    private LinkKind kind() {
        return getBlockState().getBlock() instanceof LinkedBlock b ? b.kind() : LinkKind.CHEST;
    }

    public LinkedData shared() {
        return LinkedData.get((ServerLevel) level);
    }

    public ChannelKey key() {
        return ChannelKey.of(kind(), lock, owner, frequency);
    }

    public UUID owner() {
        return owner;
    }

    public int frequency() {
        return frequency;
    }

    public Lock lock() {
        return lock;
    }

    public void setOwner(UUID id) {
        owner = id;
        setChanged();
    }

    public void setFrequency(int f) {
        frequency = f & (Frequency.CHANNELS - 1);
        setChanged();
    }

    public void cycleDye(int slot) {
        setFrequency(Frequency.cycle(frequency, slot));
    }

    public void cycleLock() {
        lock = lock.next();
        setChanged();
    }

    public boolean mayUse(UUID player) {
        LinkedData d = shared();
        return Access.allowed(lock, owner, new HashSet<>(d.team(owner)), player);
    }

    public IItemHandler itemHandler() {
        return kind() == LinkKind.CHEST ? items : null;
    }

    public IFluidHandler fluidHandler() {
        return kind() == LinkKind.TANK ? fluids : null;
    }

    public IEnergyStorage energyHandler() {
        return kind() == LinkKind.CELL ? energy : null;
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.store("owner", UUIDUtil.CODEC, owner);
        out.putInt("frequency", frequency);
        out.putInt("lock", lock.ordinal());
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        Optional<UUID> o = in.read("owner", UUIDUtil.CODEC);
        owner = o.orElse(ChannelKey.PUBLIC_SCOPE);
        frequency = in.getIntOr("frequency", 0);
        lock = Lock.byOrdinal(in.getIntOr("lock", 0));
    }
}
