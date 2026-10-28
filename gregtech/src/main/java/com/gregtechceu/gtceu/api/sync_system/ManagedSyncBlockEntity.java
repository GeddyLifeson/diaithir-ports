package com.gregtechceu.gtceu.api.sync_system;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A BlockEntity that manages sync and save data via the {@code ISyncManaged} syncdata system.
 * 
 * @see ISyncManaged
 */
public abstract class ManagedSyncBlockEntity extends BlockEntity implements ISyncManaged {

    @Getter
    protected final SyncDataHolder syncDataHolder = new SyncDataHolder(this);
    @Getter
    @Setter
    private boolean isDirty;

    public ManagedSyncBlockEntity(BlockEntityCreationInfo info) {
        super(info.type(), info.pos(), info.state());
    }

    public ManagedSyncBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    /**
     * Saves BE data to world save.
     */
    @Override
    protected final void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        com.gregtechceu.gtceu.compat.NbtCompat.merge(output,
                getSyncDataHolder().serializeNBT(com.gregtechceu.gtceu.compat.NbtCompat.registries(getLevel()), false));
    }

    /**
     * Loads BE data from world save or from a client update packet.
     */
    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        loadSyncTag(com.gregtechceu.gtceu.compat.NbtCompat.toTag(input), input.lookup());
    }

    /**
     * Override this to add logic for modifying saved data before it is loaded (e.g. for cross-version
     * compatibility). When overriding, {@code super.loadSyncTag(tag, registries)} must be called AFTER any custom
     * logic.
     */
    @MustBeInvokedByOverriders
    protected void loadSyncTag(CompoundTag tag, HolderLookup.Provider registries) {
        getSyncDataHolder().deserializeNBT(registries, tag,
                (getLevel() == null ? GTCEu.isClientThread() : getLevel().isClientSide()));
    }

    /**
     * Saves this BE's data onto an item stack (replacement for the removed vanilla {@code saveToItem}).
     */
    public void saveToItem(net.minecraft.world.item.ItemStack stack, HolderLookup.Provider registries) {
        var out = com.gregtechceu.gtceu.compat.NbtCompat.output(registries);
        this.saveCustomOnly(out);
        this.removeComponentsFromTag(out);
        net.minecraft.world.item.BlockItem.setBlockEntityData(stack, this.getType(), out);
        stack.applyComponents(this.collectComponents());
    }

    /**
     * Called to gather BE data to be sent when a client loads this BE.
     */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        getSyncDataHolder().resyncAllFields();
        tag.merge(getSyncDataHolder().serializeNBT(registries, true, true));
        return tag;
    }

    /**
     * Called to get an update packet which is sent to clients to notify them when a loaded BE's data changes.
     */
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, (b, r) -> getSyncDataHolder().serializeNBT(r, true));
    }

    @Override
    public final void markAsChanged() {
        isDirty = true;
    }

    public final void updateTick() {
        setChanged();
        if (isDirty) {
            Objects.requireNonNull(getLevel()).sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(),
                    Block.UPDATE_CLIENTS);
            isDirty = false;
        }
    }
}
