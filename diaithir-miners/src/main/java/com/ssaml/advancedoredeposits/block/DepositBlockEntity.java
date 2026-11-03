package com.ssaml.advancedoredeposits.block;

import com.ssaml.advancedoredeposits.DepositType;
import com.ssaml.advancedoredeposits.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Remaining-count store for one patch tile (Advanced Ore Deposits, MIT, samllea1). */
public class DepositBlockEntity extends BlockEntity {
    private DepositType oreType;
    private int remainingBlocks = 4096;
    private int maxBlocks = 4096;
    private int patchDiameter = 16;
    private long patchId;

    public DepositBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DEPOSIT.get(), pos, state);
        this.oreType = state.getBlock() instanceof DepositBlock b ? b.type() : DepositType.IRON;
    }

    public DepositType oreType() { return oreType; }
    public int remainingBlocks() { return remainingBlocks; }
    public int maxBlocks() { return maxBlocks; }
    public int patchDiameter() { return patchDiameter; }
    public long patchId() { return patchId; }

    public void initialize(DepositType oreType, int remaining, int max, int diameter, long patchId) {
        this.oreType = oreType;
        this.remainingBlocks = Math.max(0, remaining);
        this.maxBlocks = Math.max(this.remainingBlocks, max);
        this.patchDiameter = Math.max(1, diameter);
        this.patchId = patchId;
        setChanged();
    }

    /** Uses one mine cycle. Returns true while the tile still has cycles left. */
    public boolean consumeMineCycle() {
        if (remainingBlocks <= 0) {
            return false;
        }
        remainingBlocks--;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return remainingBlocks > 0;
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("OreType", oreType.id());
        out.putInt("RemainingBlocks", remainingBlocks);
        out.putInt("MaxBlocks", maxBlocks);
        out.putInt("PatchDiameter", patchDiameter);
        out.putLong("PatchId", patchId);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        DepositType.byId(in.getStringOr("OreType", oreType.id())).ifPresent(t -> this.oreType = t);
        this.remainingBlocks = in.getIntOr("RemainingBlocks", 4096);
        this.maxBlocks = in.getIntOr("MaxBlocks", 4096);
        this.patchDiameter = in.getIntOr("PatchDiameter", 16);
        this.patchId = in.getLongOr("PatchId", 0L);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
