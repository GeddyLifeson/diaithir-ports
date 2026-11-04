package com.misterd.agritechevolved.blockentity.custom;

import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.block.custom.SiloBlock;
import com.misterd.agritechevolved.blockentity.ATEBlockEntities;
import com.misterd.agritechevolved.gui.custom.SiloMenu;
import com.misterd.agritechevolved.util.ATETags;
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
import net.minecraft.world.level.chunk.LevelChunk;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SiloBlockEntity extends BlockEntity implements MenuProvider {

    private static final int STORAGE_SLOTS = 63;
    private static final int MODULE_SLOT = 63;
    private static final int TOTAL_SLOTS = 64;

    private static final String RM_MK1 = "agritechevolved:rm_mk1";
    private static final String RM_MK2 = "agritechevolved:rm_mk2";
    private static final String RM_MK3 = "agritechevolved:rm_mk3";

    private static final int MIN_RESCAN_INTERVAL_TICKS = 20;

    private int energyStored = 0;
    private int tickCounter = 0;
    private int scanAge = MIN_RESCAN_INTERVAL_TICKS;
    private List<BlockPos> cachedTargets = new ArrayList<>();

    public final ItemStacksResourceHandler inventory = new ItemStacksResourceHandler(TOTAL_SLOTS) {
        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return index == MODULE_SLOT ? 1 : resource.toStack().getMaxStackSize();
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            if (resource.isEmpty()) return false;
            if (index == MODULE_SLOT) return isRangeModule(resource.toStack());
            return index < STORAGE_SLOTS;
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            SiloBlockEntity.this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final ResourceHandler<ItemResource> externalItemHandler = new ResourceHandler<>() {
        @Override
        public int size() { return inventory.size(); }

        @Override
        public ItemResource getResource(int index) { return inventory.getResource(index); }

        @Override
        public long getAmountAsLong(int index) { return inventory.getAmountAsLong(index); }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return inventory.getCapacityAsLong(index, resource);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) { return false; }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
            return 0;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
            if (index >= STORAGE_SLOTS) return 0;
            return inventory.extract(index, resource, amount, tx);
        }
    };

    private final EnergyHandler energyHandler = new BEEnergyHandler(this);

    public SiloBlockEntity(BlockPos pos, BlockState blockState) {
        super(ATEBlockEntities.SILO_BE.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SiloBlockEntity be) {
        if (level.isClientSide()) return;

        be.tickCounter++;
        if (be.tickCounter < Config.getSiloPullInterval()) return;
        be.tickCounter = 0;

        int required = Config.getSiloBasePowerConsumption();
        boolean hasPower = be.energyStored >= required;

        boolean changed = false;
        if (hasPower) {
            be.energyStored -= required;
            be.scanAge += Config.getSiloPullInterval();
            if (be.scanAge >= MIN_RESCAN_INTERVAL_TICKS) {
                be.rescanTargets(level, pos);
                be.scanAge = 0;
            }
            for (BlockPos targetPos : be.cachedTargets) {
                if (be.pullFromTarget(level, targetPos)) changed = true;
            }
        }

        if (changed) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        boolean shouldBePowered = hasPower;
        boolean currentlyPowered = state.getValue(SiloBlock.POWERED);
        if (shouldBePowered != currentlyPowered) {
            level.setBlock(pos, state.setValue(SiloBlock.POWERED, shouldBePowered), 3);
        }
    }

    private void rescanTargets(Level level, BlockPos pos) {
        int range = getRange();
        List<BlockPos> targets = new ArrayList<>();
        int chunkRadius = (range >> 4) + 1;
        int centerChunkX = pos.getX() >> 4;
        int centerChunkZ = pos.getZ() >> 4;

        for (int cx = -chunkRadius; cx <= chunkRadius; cx++) {
            for (int cz = -chunkRadius; cz <= chunkRadius; cz++) {
                int chunkX = centerChunkX + cx;
                int chunkZ = centerChunkZ + cz;
                if (!level.hasChunk(chunkX, chunkZ)) continue;

                LevelChunk chunk = level.getChunk(chunkX, chunkZ);
                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockPos bePos = entry.getKey();
                    if (Math.abs(bePos.getX() - pos.getX()) > range) continue;
                    if (Math.abs(bePos.getY() - pos.getY()) > range) continue;
                    if (Math.abs(bePos.getZ() - pos.getZ()) > range) continue;

                    BlockEntity be = entry.getValue();
                    if (be instanceof PlanterBlockEntity || be instanceof AdvancedPlanterBlockEntity) {
                        targets.add(bePos.immutable());
                    }
                }
            }
        }
        cachedTargets = targets;
    }

    private boolean pullFromTarget(Level level, BlockPos targetPos) {
        BlockEntity target = level.getBlockEntity(targetPos);
        ResourceHandler<ItemResource> source = getExtractHandlerFor(target);
        if (source == null) return false;

        boolean changed = false;
        for (int slot = 0; slot < source.size(); slot++) {
            ItemResource res = source.getResource(slot);
            if (res.isEmpty()) continue;
            int available = (int) source.getAmountAsLong(slot);
            if (available <= 0) continue;

            for (int storageSlot = 0; storageSlot < STORAGE_SLOTS && available > 0; storageSlot++) {
                try (Transaction tx = Transaction.openRoot()) {
                    int inserted = inventory.insert(storageSlot, res, available, tx);
                    if (inserted <= 0) continue;
                    int extracted = source.extract(slot, res, inserted, tx);
                    if (extracted != inserted) continue;
                    tx.commit();
                    available -= extracted;
                    changed = true;
                }
            }
        }
        return changed;
    }

    @Nullable
    private ResourceHandler<ItemResource> getExtractHandlerFor(@Nullable BlockEntity be) {
        if (be instanceof AdvancedPlanterBlockEntity planter) return planter.getExtractHandler();
        if (be instanceof PlanterBlockEntity planter) return planter.getExtractHandler();
        return null;
    }

    private boolean isRangeModule(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ATETags.Items.ATE_RANGE_MODULES);
    }

    private int getRangeBonus() {
        ItemStack module = getStack(MODULE_SLOT);
        if (module.isEmpty()) return 0;
        return switch (RegistryHelper.getItemId(module)) {
            case RM_MK1 -> 16;
            case RM_MK2 -> 32;
            case RM_MK3 -> 64;
            default -> 0;
        };
    }

    public int getRange() {
        return Config.getSiloBaseRange() + getRangeBonus();
    }

    public ResourceHandler<ItemResource> getExternalItemHandler(@Nullable Direction side) {
        return externalItemHandler;
    }

    public EnergyHandler getEnergyHandler(@Nullable Direction side) {
        return energyHandler;
    }

    private static class BEEnergyHandler extends SnapshotJournal<Integer> implements EnergyHandler {
        private final SiloBlockEntity be;

        BEEnergyHandler(SiloBlockEntity be) { this.be = be; }

        @Override
        protected Integer createSnapshot() { return be.energyStored; }

        @Override
        protected void revertToSnapshot(Integer snapshot) { be.energyStored = snapshot; }

        @Override
        protected void onRootCommit(Integer originalState) { be.setChanged(); }

        @Override
        public long getAmountAsLong() { return be.energyStored; }

        @Override
        public long getCapacityAsLong() { return Config.getSiloEnergyBuffer(); }

        @Override
        public int insert(int amount, TransactionContext tx) {
            int received = Math.min(amount, Config.getSiloEnergyBuffer() - be.energyStored);
            if (received <= 0) return 0;
            updateSnapshots(tx);
            be.energyStored += received;
            return received;
        }

        @Override
        public int extract(int amount, TransactionContext tx) { return 0; }
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ATEBlockEntities.SILO_BE.get(),
                (be, dir) -> be instanceof SiloBlockEntity s ? s.getExternalItemHandler(dir) : null);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ATEBlockEntities.SILO_BE.get(),
                (be, dir) -> be instanceof SiloBlockEntity s ? s.getEnergyHandler(dir) : null);
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

    public int getEnergyStored() { return energyStored; }
    public int getMaxEnergyStored() { return Config.getSiloEnergyBuffer(); }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.serialize(output);
        output.putInt("energyStored", energyStored);
        output.putInt("tickCounter", tickCounter);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        inventory.deserialize(input);
        energyStored = input.getIntOr("energyStored", 0);
        tickCounter = input.getIntOr("tickCounter", 0);
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
        return Component.translatable("gui.agritechevolved.silo");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SiloMenu(containerId, playerInventory, this);
    }
}