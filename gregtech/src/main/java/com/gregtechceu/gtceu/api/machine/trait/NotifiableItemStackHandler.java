package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.DummyCraftingInput;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.items.IItemHandlerModifiable;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;

@ExtensionMethod(SizedIngredientExtensions.class)
public class NotifiableItemStackHandler extends NotifiableRecipeHandlerTrait<SizedIngredient>
                                        implements ICapabilityTrait, IItemHandlerModifiable {

    public static final MachineTraitType<NotifiableItemStackHandler> TYPE = new MachineTraitType<>(
            NotifiableItemStackHandler.class);

    @Override
    public MachineTraitType<NotifiableItemStackHandler> getTraitType() {
        return TYPE;
    }

    @Getter
    public final IO handlerIO;
    @Getter
    public final IO capabilityIO;
    @SaveField
    @SyncToClient
    public final CustomItemStackHandler storage;
    @Accessors(fluent = true)
    @Getter
    @Setter
    private boolean shouldSearchContent = true;
    private Boolean isEmpty;

    public NotifiableItemStackHandler(MetaMachine machine, int slots, IO handlerIO, IO capabilityIO,
                                      IntFunction<CustomItemStackHandler> storageFactory) {
        super(machine);
        this.handlerIO = handlerIO;
        this.storage = storageFactory.apply(slots);
        this.capabilityIO = capabilityIO;
        this.storage.setOnContentsChanged(this::onContentsChanged);
    }

    public NotifiableItemStackHandler(MetaMachine machine, int slots, IO handlerIO, IO capabilityIO) {
        this(machine, slots, handlerIO, capabilityIO, CustomItemStackHandler::new);
    }

    public NotifiableItemStackHandler(MetaMachine machine, int slots, IO handlerIO) {
        this(machine, slots, handlerIO, handlerIO);
    }

    public NotifiableItemStackHandler setFilter(Predicate<ItemStack> filter) {
        this.storage.setFilter(filter);
        return this;
    }

    public void onContentsChanged() {
        isEmpty = null;
        syncDataHolder.markClientSyncFieldDirty("storage");
        notifyListeners();
    }

    @Override
    public List<SizedIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<SizedIngredient> left,
                                                   boolean simulate) {
        return handleRecipe(io, recipe, left, simulate, handlerIO, storage);
    }

    // TODO: See if implementable in outside callers and unstatic; or move to different common class if not
    // Notable caller is ItemRecipeHandler, used for MinerLogic
    public static List<SizedIngredient> handleRecipe(IO io, GTRecipe recipe, List<SizedIngredient> left,
                                                     boolean simulate,
                                                     IO handlerIO, CustomItemStackHandler storage) { return null; /* ported stub */ }

    private static @Nullable ItemStack getActioned(CustomItemStackHandler storage, int index, List<?> actions) { return null; /* ported stub */ }

    @Override
    public RecipeCapability<SizedIngredient> getCapability() {
        return ItemRecipeCapability.CAP;
    }

    public int getSlots() {
        return storage.getSlots();
    }

    @Override
    public int getSize() {
        return getSlots();
    }

    @Override
    public @NotNull List<Object> getContents() {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < getSlots(); ++i) {
            ItemStack stack = getStackInSlot(i);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return new ArrayList<>(stacks);
    }

    @Override
    public double getTotalContentAmount() {
        long amount = 0;
        for (int i = 0; i < getSlots(); ++i) {
            ItemStack stack = getStackInSlot(i);
            if (!stack.isEmpty()) {
                amount += stack.getCount();
            }
        }
        return amount;
    }

    public boolean isEmpty() {
        if (isEmpty == null) {
            isEmpty = true;
            for (int i = 0; i < storage.getSlots(); i++) {
                if (!storage.getStackInSlot(i).isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }
        }
        return isEmpty;
    }

    public void exportToNearby(@NotNull Direction... facings) {
        if (isEmpty()) return;
        var level = getMachine().getLevel();
        var pos = getMachine().getBlockPos();
        for (Direction facing : facings) {
            var filter = getMachine().getItemCapFilter(facing, IO.OUT);
            GTTransferUtils.getAdjacentItemHandler(level, pos, facing)
                    .ifPresent(adj -> GTTransferUtils.transferItemsFiltered(this, adj, filter));
        }
    }

    public void importFromNearby(@NotNull Direction... facings) {
        var level = getMachine().getLevel();
        var pos = getMachine().getBlockPos();
        for (Direction facing : facings) {
            var filter = getMachine().getItemCapFilter(facing, IO.IN);
            GTTransferUtils.getAdjacentItemHandler(level, pos, facing)
                    .ifPresent(adj -> GTTransferUtils.transferItemsFiltered(adj, this, filter));
        }
    }

    //////////////////////////////////////
    // ******* Capability ********//
    //////////////////////////////////////
    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return storage.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int index, @NotNull ItemStack stack) {
        storage.setStackInSlot(index, stack);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (canCapInput()) {
            return storage.insertItem(slot, stack, simulate);
        }
        return stack;
    }

    public ItemStack insertItemInternal(int slot, @NotNull ItemStack stack, boolean simulate) {
        return storage.insertItem(slot, stack, simulate);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (canCapOutput()) {
            return storage.extractItem(slot, amount, simulate);
        }
        return ItemStack.EMPTY;
    }

    public ItemStack extractItemInternal(int slot, int amount, boolean simulate) {
        return storage.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return storage.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return storage.isItemValid(slot, stack);
    }

    public void dropInventoryInWorld() {
        storage.dropInventoryInWorld(getLevel(), getMachine().getBlockPos());
    }

    public static class KJSCallWrapper {/* ported: removed */
    }
}
