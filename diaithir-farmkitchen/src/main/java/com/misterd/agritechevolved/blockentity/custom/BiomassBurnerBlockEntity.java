package com.misterd.agritechevolved.blockentity.custom;

import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.block.custom.BiomassBurnerBlock;
import com.misterd.agritechevolved.blockentity.ATEBlockEntities;
import com.misterd.agritechevolved.gui.custom.BiomassBurnerMenu;
import com.misterd.agritechevolved.util.RegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;

public class BiomassBurnerBlockEntity extends BlockEntity implements MenuProvider {

    private static final String BIOMASS = "agritechevolved:biomass";
    private static final String CRUDE_BIOMASS = "agritechevolved:crude_biomass";
    private static final String COMPACTED_BIOMASS = "agritechevolved:compacted_biomass";
    private static final String COMPACTED_BIOMASS_BLOCK = "agritechevolved:compacted_biomass_block";

    public final ItemStacksResourceHandler inventory = new ItemStacksResourceHandler(1) {
        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return 64;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return isFuel(resource.toStack());
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            BiomassBurnerBlockEntity.this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private int energyStored = 0;
    private int progress = 0;
    private int maxProgress = 0;
    private int currentBurnValue = 0;
    private boolean isBurning = false;

    public BiomassBurnerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ATEBlockEntities.BURNER_BE.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BiomassBurnerBlockEntity be) {
        if (level.isClientSide()) return;

        boolean changed = false;

        if (!be.isBurning && be.canStartBurning()) {
            be.startBurning();
            changed = true;
        }

        if (be.isBurning && be.energyStored < Config.getBurnerEnergyBuffer()) {
            be.progress++;
            if (be.currentBurnValue > 0) {
                int generated = Math.min(be.currentBurnValue, Config.getBurnerEnergyBuffer() - be.energyStored);
                be.energyStored += generated;
                if (generated > 0) changed = true;
            }
            if (be.progress >= be.maxProgress) {
                be.completeBurning();
                changed = true;
            }
        }

        if (be.distributeEnergy()) changed = true;

        if (changed) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        boolean shouldBurn = be.isBurning;
        boolean currentlyBurn = state.getValue(BiomassBurnerBlock.BURNING);
        if (shouldBurn != currentlyBurn) {
            level.setBlock(pos, state.setValue(BiomassBurnerBlock.BURNING, shouldBurn), 3);
        }
    }

    private boolean canStartBurning() {
        ItemStack fuel = getStack(0);
        return !fuel.isEmpty() && energyStored < Config.getBurnerEnergyBuffer() && isFuel(fuel);
    }

    private void startBurning() {
        ItemStack fuel = getStack(0);
        if (fuel.isEmpty()) return;

        String id = RegistryHelper.getItemId(fuel);
        int baseRF, burnDuration, baseDuration;

        switch (id) {
            case BIOMASS -> {
                baseRF = Config.getBurnerBiomassRfValue();
                burnDuration = Config.getBurnerBiomassBurnDuration();
                baseDuration = 100;
            }
            case COMPACTED_BIOMASS -> {
                baseRF = Config.getBurnerCompactedBiomassRfValue();
                burnDuration = Config.getBurnerCompactedBiomassBurnDuration();
                baseDuration = 180;
            }
            case COMPACTED_BIOMASS_BLOCK -> {
                baseRF = Config.getBurnerCompactedBiomassBlockRfValue();
                burnDuration = Config.getBurnerCompactedBiomassBlockBurnDuration();
                baseDuration = 180;
            }
            case CRUDE_BIOMASS -> {
                baseRF = Config.getBurnerCrudeBiomassRfValue();
                burnDuration = Config.getBurnerCrudeBiomassBurnDuration();
                baseDuration = 50;
            }
            default -> {
                return;
            }
        }

        int totalRF = (int) ((float) baseRF * ((float) burnDuration / baseDuration));
        if (totalRF <= 0 || burnDuration <= 0) return;

        maxProgress = burnDuration;
        currentBurnValue = totalRF / maxProgress;
        progress = 0;
        isBurning = true;

        try (Transaction tx = Transaction.openRoot()) {
            inventory.extract(0, ItemResource.of(fuel), 1, tx);
            tx.commit();
        }
    }

    private void completeBurning() {
        progress = 0;
        maxProgress = 0;
        currentBurnValue = 0;
        isBurning = false;
    }

    private boolean distributeEnergy() {
        if (energyStored <= 0) return false;

        boolean distributed = false;
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(dir);
            if (level.getBlockEntity(neighborPos) == null) continue;

            EnergyHandler neighbor = level.getCapability(Capabilities.Energy.BLOCK, neighborPos, dir.getOpposite());
            if (neighbor == null) continue;

            int toTransfer = Math.min(1000, energyStored);
            if (toTransfer <= 0) continue;

            try (Transaction tx = Transaction.openRoot()) {
                int transferred = neighbor.insert(toTransfer, tx);
                if (transferred > 0) {
                    energyStored -= transferred;
                    tx.commit();
                    distributed = true;
                }
            }
        }
        return distributed;
    }

    private static boolean isFuel(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String id = RegistryHelper.getItemId(stack);
        return id.equals(BIOMASS) || id.equals(CRUDE_BIOMASS)
                || id.equals(COMPACTED_BIOMASS) || id.equals(COMPACTED_BIOMASS_BLOCK);
    }

    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        return new ResourceHandler<>() {
            @Override
            public int size() {
                return inventory.size();
            }

            @Override
            public ItemResource getResource(int index) {
                return inventory.getResource(index);
            }

            @Override
            public long getAmountAsLong(int index) {
                return inventory.getAmountAsLong(index);
            }

            @Override
            public long getCapacityAsLong(int index, ItemResource resource) {
                return inventory.getCapacityAsLong(index, resource);
            }

            @Override
            public boolean isValid(int index, ItemResource resource) {
                return isFuel(resource.toStack());
            }

            @Override
            public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
                if (!isFuel(resource.toStack())) return 0;
                return inventory.insert(index, resource, amount, tx);
            }

            @Override
            public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
                return inventory.extract(index, resource, amount, tx);
            }
        };
    }

    public EnergyHandler getEnergyHandler(@Nullable Direction side) {
        return new BEEnergyHandler(this);
    }

    private static class BEEnergyHandler extends SnapshotJournal<Integer> implements EnergyHandler {
        private final BiomassBurnerBlockEntity be;

        BEEnergyHandler(BiomassBurnerBlockEntity be) { this.be = be; }

        @Override
        protected Integer createSnapshot() {
            return be.energyStored;
        }

        @Override protected void revertToSnapshot(Integer snapshot) {
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
            return Config.getBurnerEnergyBuffer();
        }

        @Override
        public int insert(int amount, TransactionContext tx) {
            return 0;
        }

        @Override
        public int extract(int amount, TransactionContext tx) {
            int extracted = Math.min(amount, be.energyStored);
            if (extracted <= 0) return 0;
            updateSnapshots(tx);
            be.energyStored -= extracted;
            return extracted;
        }
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ATEBlockEntities.BURNER_BE.get(),
                (be, dir) -> be instanceof BiomassBurnerBlockEntity b ? b.getItemHandler(dir) : null);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ATEBlockEntities.BURNER_BE.get(),
                (be, dir) -> be instanceof BiomassBurnerBlockEntity b ? b.getEnergyHandler(dir) : null);
    }

    public ItemStack getStack(int slot) {
        ItemResource res = inventory.getResource(slot);
        if (res.isEmpty()) return ItemStack.EMPTY;
        return res.toStack(inventory.getAmountAsInt(slot));
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        drops();
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(inventory.size());
        for (int i = 0; i < inventory.size(); i++) {
            inv.setItem(i, getStack(i));
        }
        Containers.dropContents(level, worldPosition, inv);
    }

    public int getEnergyStored() {
        return energyStored;
    }

    public int getMaxEnergy() {
        return Config.getBurnerEnergyBuffer();
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public boolean isBurning() {
        return isBurning;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.serialize(output);
        output.putInt("energyStored", energyStored);
        output.putInt("progress", progress);
        output.putInt("maxProgress", maxProgress);
        output.putInt("currentBurnValue", currentBurnValue);
        output.putBoolean("isBurning", isBurning);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        inventory.deserialize(input);
        energyStored = input.getIntOr("energyStored", 0);
        progress = input.getIntOr("progress", 0);
        maxProgress = input.getIntOr("maxProgress", 0);
        currentBurnValue = input.getIntOr("currentBurnValue", 0);
        isBurning = input.getBooleanOr("isBurning", false);
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
        return Component.translatable("gui.agritechevolved.biomass_burner");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BiomassBurnerMenu(containerId, playerInventory, this);
    }
}