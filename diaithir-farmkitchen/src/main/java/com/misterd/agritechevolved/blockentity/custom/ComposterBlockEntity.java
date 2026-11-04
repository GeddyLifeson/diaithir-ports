package com.misterd.agritechevolved.blockentity.custom;

import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.block.custom.ComposterBlock;
import com.misterd.agritechevolved.blockentity.ATEBlockEntities;
import com.misterd.agritechevolved.gui.custom.ComposterMenu;
import com.misterd.agritechevolved.item.ATEItems;
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

public class ComposterBlockEntity extends BlockEntity implements MenuProvider {

    private static final int INPUT_SLOTS_START = 0;
    private static final int INPUT_SLOTS_COUNT = 12;
    private static final int OUTPUT_SLOTS_START = 12;
    private static final int OUTPUT_SLOTS_COUNT = 3;
    private static final int MODULE_SLOT = 15;
    private static final int TOTAL_SLOTS = 16;

    private static final String SM_MK1 = "agritechevolved:sm_mk1";
    private static final String SM_MK2 = "agritechevolved:sm_mk2";
    private static final String SM_MK3 = "agritechevolved:sm_mk3";

    private static final float COMPOST_TARGET = 7.0f;

    private int progress = 0;
    private int energyStored = 0;

    public final ItemStacksResourceHandler inventory = new ItemStacksResourceHandler(TOTAL_SLOTS) {
        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return index == MODULE_SLOT ? 1 : resource.toStack().getMaxStackSize();
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            if (resource.isEmpty()) return false;
            if (index >= INPUT_SLOTS_START && index < INPUT_SLOTS_START + INPUT_SLOTS_COUNT)
                return isCompostableItem(resource.toStack());
            if (index >= OUTPUT_SLOTS_START && index < OUTPUT_SLOTS_START + OUTPUT_SLOTS_COUNT)
                return true;
            return index == MODULE_SLOT && isSpeedModule(resource.toStack());
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            ComposterBlockEntity.this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public ComposterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ATEBlockEntities.COMPOSTER_BE.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ComposterBlockEntity be) {
        if (level.isClientSide()) return;

        boolean changed = false;
        int requiredEnergy = Config.getComposterBasePowerConsumption();
        boolean hasPower = be.energyStored >= requiredEnergy;

        int baseTime = Config.getComposterBaseProcessingTime();
        if (hasPower) baseTime /= 3;
        int actualTime = (int) Math.max(1, baseTime / be.getModuleSpeedModifier());

        if (be.canProcess()) {
            if (be.progress == 0) be.progress = 1;
            else be.progress++;
            changed = true;

            if (be.progress >= actualTime) {
                be.processItems();
                if (hasPower) {
                    int adjusted = (int) Math.ceil(requiredEnergy * be.getModulePowerModifier());
                    be.energyStored = Math.max(0, be.energyStored - adjusted);
                }
                be.progress = 0;
            }
        } else if (be.progress > 0) {
            be.progress = 0;
            changed = true;
        }

        if (changed) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            boolean shouldBePowered = be.progress > 0;
            boolean currentlyPowered = state.getValue(ComposterBlock.POWERED);
            if (shouldBePowered != currentlyPowered) {
                level.setBlock(pos, state.setValue(ComposterBlock.POWERED, shouldBePowered), 3);
            }
        }
    }

    private float getTotalCompostValue() {
        float total = 0f;
        for (int i = INPUT_SLOTS_START; i < INPUT_SLOTS_START + INPUT_SLOTS_COUNT; i++) {
            ItemStack stack = getStack(i);
            if (!stack.isEmpty()) {
                total += getCompostChance(stack) * stack.getCount();
            }
        }
        return total;
    }

    private boolean canProcess() {
        return getTotalCompostValue() >= COMPOST_TARGET && hasSpaceForBiomass();
    }

    private boolean hasSpaceForBiomass() {
        ItemStack biomass = new ItemStack(ATEItems.BIOMASS.get());
        for (int i = OUTPUT_SLOTS_START; i < OUTPUT_SLOTS_START + OUTPUT_SLOTS_COUNT; i++) {
            ItemStack output = getStack(i);
            if (output.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(output, biomass) && output.getCount() < output.getMaxStackSize()) return true;
        }
        return false;
    }

    private void processItems() {
        float remaining = COMPOST_TARGET;
        for (int i = INPUT_SLOTS_START; i < INPUT_SLOTS_START + INPUT_SLOTS_COUNT && remaining > 0f; i++) {
            ItemStack stack = getStack(i);
            if (stack.isEmpty()) continue;
            float chance = getCompostChance(stack);
            if (chance <= 0f) continue;
            int needed = (int) Math.ceil(remaining / chance);
            int taken = Math.min(needed, stack.getCount());
            try (Transaction tx = Transaction.openRoot()) {
                inventory.extract(i, ItemResource.of(stack), taken, tx);
                tx.commit();
            }
            remaining -= chance * taken;
        }
        addBiomassToOutput();
    }

    private void addBiomassToOutput() {
        ItemResource biomassRes = ItemResource.of(new ItemStack(ATEItems.BIOMASS.get()));
        for (int i = OUTPUT_SLOTS_START; i < OUTPUT_SLOTS_START + OUTPUT_SLOTS_COUNT; i++) {
            try (Transaction tx = Transaction.openRoot()) {
                int inserted = inventory.insert(i, biomassRes, 1, tx);
                if (inserted > 0) { tx.commit(); return; }
            }
        }
    }

    private float getCompostChance(ItemStack stack) {
        return net.minecraft.world.level.block.ComposterBlock.getValue(stack);
    }

    public boolean isCompostableItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return getCompostChance(stack) > 0f;
    }

    private boolean isSpeedModule(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String id = RegistryHelper.getItemId(stack);
        return id.equals(SM_MK1) || id.equals(SM_MK2) || id.equals(SM_MK3);
    }

    private double getModuleSpeedModifier() {
        ItemStack module = getStack(MODULE_SLOT);
        if (module.isEmpty()) return 1.0;
        return switch (RegistryHelper.getItemId(module)) {
            case SM_MK1 -> Config.getSpeedModuleMk1Multiplier();
            case SM_MK2 -> Config.getSpeedModuleMk2Multiplier();
            case SM_MK3 -> Config.getSpeedModuleMk3Multiplier();
            default -> 1.0;
        };
    }

    private double getModulePowerModifier() {
        ItemStack module = getStack(MODULE_SLOT);
        if (module.isEmpty()) return 1.0;
        return switch (RegistryHelper.getItemId(module)) {
            case SM_MK1 -> Config.getSpeedModuleMk1PowerMultiplier();
            case SM_MK2 -> Config.getSpeedModuleMk2PowerMultiplier();
            case SM_MK3 -> Config.getSpeedModuleMk3PowerMultiplier();
            default -> 1.0;
        };
    }

    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        return new ResourceHandler<>() {
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
            public boolean isValid(int index, ItemResource resource) {
                return inventory.isValid(index, resource);
            }

            @Override
            public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
                boolean isInput = index >= INPUT_SLOTS_START && index < INPUT_SLOTS_START + INPUT_SLOTS_COUNT;
                boolean isModule = index == MODULE_SLOT;
                return (isInput || isModule) ? inventory.insert(index, resource, amount, tx) : 0;
            }

            @Override
            public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
                boolean isOutput = index >= OUTPUT_SLOTS_START && index < OUTPUT_SLOTS_START + OUTPUT_SLOTS_COUNT;
                return isOutput ? inventory.extract(index, resource, amount, tx) : 0;
            }
        };
    }

    public EnergyHandler getEnergyHandler(@Nullable Direction side) {
        return new BEEnergyHandler(this);
    }

    private static class BEEnergyHandler extends SnapshotJournal<Integer> implements EnergyHandler {
        private final ComposterBlockEntity be;

        BEEnergyHandler(ComposterBlockEntity be) { this.be = be; }

        @Override
        protected Integer createSnapshot() { return be.energyStored; }

        @Override
        protected void revertToSnapshot(Integer snapshot) { be.energyStored = snapshot; }

        @Override
        protected void onRootCommit(Integer originalState) { be.setChanged(); }

        @Override
        public long getAmountAsLong() { return be.energyStored; }

        @Override
        public long getCapacityAsLong() { return Config.getComposterEnergyBuffer(); }

        @Override
        public int insert(int amount, TransactionContext tx) {
            int received = Math.min(amount, Config.getComposterEnergyBuffer() - be.energyStored);
            if (received <= 0) return 0;
            updateSnapshots(tx);
            be.energyStored += received;
            return received;
        }

        @Override
        public int extract(int amount, TransactionContext tx) { return 0; }
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ATEBlockEntities.COMPOSTER_BE.get(),
                (be, dir) -> be instanceof ComposterBlockEntity c ? c.getItemHandler(dir) : null);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ATEBlockEntities.COMPOSTER_BE.get(),
                (be, dir) -> be instanceof ComposterBlockEntity c ? c.getEnergyHandler(dir) : null);
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
    public int getMaxEnergyStored() { return Config.getComposterEnergyBuffer(); }
    public int getProgress() { return progress; }

    public int getMaxProgress() {
        int baseTime = Config.getComposterBaseProcessingTime();
        if (energyStored >= Config.getComposterBasePowerConsumption()) baseTime /= 3;
        return (int) Math.max(1, baseTime / getModuleSpeedModifier());
    }

    public int getCompostValueCollected() { return (int)(getTotalCompostValue() * 100 / COMPOST_TARGET); }
    public int getCompostValueRequired() { return 100; }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.serialize(output);
        output.putInt("progress", progress);
        output.putInt("energyStored", energyStored);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        inventory.deserialize(input);
        progress = input.getIntOr("progress", 0);
        energyStored = input.getIntOr("energyStored", 0);
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
        return Component.translatable("gui.agritechevolved.composter");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ComposterMenu(containerId, playerInventory, this);
    }
}