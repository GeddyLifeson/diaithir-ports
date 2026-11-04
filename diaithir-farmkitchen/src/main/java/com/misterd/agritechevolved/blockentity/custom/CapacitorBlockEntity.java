package com.misterd.agritechevolved.blockentity.custom;

import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.block.ATEBlocks;
import com.misterd.agritechevolved.block.custom.CapacitorTier1Block;
import com.misterd.agritechevolved.block.custom.CapacitorTier2Block;
import com.misterd.agritechevolved.block.custom.CapacitorTier3Block;
import com.misterd.agritechevolved.blockentity.ATEBlockEntities;
import com.misterd.agritechevolved.gui.custom.CapacitorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;

public class CapacitorBlockEntity extends BlockEntity implements MenuProvider {

    private int energyStored = 0;
    private int capacity = 0;
    private int tier = 1;
    private int transferRate = 512;

    public CapacitorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ATEBlockEntities.CAPACITOR_BE.get(), pos, blockState);
        initializeCapacitor(blockState);
    }

    private void initializeCapacitor(BlockState state) {
        if (state.is(ATEBlocks.CAPACITOR_TIER_1.get())) {
            tier = 1;
            transferRate = Config.getCapacitorT1TransferRate();
            capacity = Config.getCapacitorT1Buffer();
        } else if (state.is(ATEBlocks.CAPACITOR_TIER_2.get())) {
            tier = 2;
            transferRate = Config.getCapacitorT2TransferRate();
            capacity = Config.getCapacitorT2Buffer();
        } else if (state.is(ATEBlocks.CAPACITOR_TIER_3.get())) {
            tier = 3;
            transferRate = Config.getCapacitorT3TransferRate();
            capacity = Config.getCapacitorT3Buffer();
        } else {
            tier = 1;
            transferRate = Config.getCapacitorT1TransferRate();
            capacity = Config.getCapacitorT1Buffer();
        }

        energyStored = Math.min(energyStored, capacity);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CapacitorBlockEntity be) {
        if (level.isClientSide()) return;

        boolean changed = false;

        if (be.energyStored > 0) {
            for (Direction dir : Direction.values()) {
                if (be.energyStored <= 0) break;

                BlockPos neighborPos = pos.relative(dir);
                if (level.getBlockEntity(neighborPos) == null) continue;

                EnergyHandler neighbor = level.getCapability(Capabilities.Energy.BLOCK, neighborPos, dir.getOpposite());
                if (neighbor == null) continue;

                int toTransfer = Math.min(be.transferRate, be.energyStored);
                if (toTransfer <= 0) continue;

                try (Transaction tx = Transaction.openRoot()) {
                    int transferred = neighbor.insert(toTransfer, tx);
                    if (transferred > 0) {
                        be.energyStored -= transferred;
                        tx.commit();
                        changed = true;
                    }
                }
            }
        }

        if (changed) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        boolean hasEnergy = be.energyStored > 0;
        BooleanProperty prop = null;
        if (state.is(ATEBlocks.CAPACITOR_TIER_1.get())) prop = CapacitorTier1Block.HAS_ENERGY;
        else if (state.is(ATEBlocks.CAPACITOR_TIER_2.get())) prop = CapacitorTier2Block.HAS_ENERGY;
        else if (state.is(ATEBlocks.CAPACITOR_TIER_3.get())) prop = CapacitorTier3Block.HAS_ENERGY;

        if (prop != null && state.getValue(prop) != hasEnergy) {
            level.setBlock(pos, state.setValue(prop, hasEnergy), 3);
        }
    }

    public EnergyHandler getEnergyHandler(@Nullable Direction side) {
        return new BEEnergyHandler(this);
    }

    private static class BEEnergyHandler extends SnapshotJournal<Integer> implements EnergyHandler {
        private final CapacitorBlockEntity be;

        BEEnergyHandler(CapacitorBlockEntity be) {
            this.be = be;
        }

        @Override
        protected Integer createSnapshot() {
            return be.energyStored;
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            be.energyStored = snapshot;
        }

        @Override
        protected void onRootCommit(Integer originalState) {
            be.setChanged();
        }

        @Override
        public long getAmountAsLong() {
            return be.energyStored;
        }

        @Override
        public long getCapacityAsLong() {
            return be.capacity;
        }

        @Override
        public int insert(int amount, TransactionContext tx) {
            int received = Math.min(Math.min(amount, be.transferRate), be.capacity - be.energyStored);
            if (received <= 0) return 0;
            updateSnapshots(tx);
            be.energyStored += received;
            return received;
        }

        @Override
        public int extract(int amount, TransactionContext tx) {
            int extracted = Math.min(Math.min(amount, be.transferRate), be.energyStored);
            if (extracted <= 0) return 0;
            updateSnapshots(tx);
            be.energyStored -= extracted;
            return extracted;
        }
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ATEBlockEntities.CAPACITOR_BE.get(),
                (be, dir) -> be instanceof CapacitorBlockEntity c ? c.getEnergyHandler(dir) : null);
    }

    public int getEnergyStored() {
        return energyStored;
    }

    public int getMaxEnergyStored() {
        return capacity;
    }

    public int getTier() {
        return tier;
    }

    public void forceSetEnergy(int energy) {
        energyStored = Math.min(energy, capacity);
        setChanged();
    }

    public int getTransferRate() {
        return transferRate;
    }

    public String getTierName() {
        return switch (tier) {
            case 1  -> "Tier 1";
            case 2  -> "Tier 2";
            case 3  -> "Tier 3";
            default -> "Unknown";
        };
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("energyStored", energyStored);
        output.putInt("tier", tier);
        output.putInt("transferRate", transferRate);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tier = input.getIntOr("tier", 1);
        transferRate = input.getIntOr("transferRate", 512);
        if (getBlockState() != null) initializeCapacitor(getBlockState());
        energyStored = Math.min(input.getIntOr("energyStored", 0), capacity);
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) level.invalidateCapabilities(getBlockPos());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) level.invalidateCapabilities(getBlockPos());
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.agritechevolved.capacitor_tier" + tier);
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CapacitorMenu(containerId, playerInventory, this);
    }
}