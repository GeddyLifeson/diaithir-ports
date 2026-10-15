package com.buuz135.industrial.api;

import net.minecraft.nbt.NbtOps;

import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public interface IMachineSettings {

    public static CompoundTag writeInventory(RegistryAccess registryAccess, InventoryComponent<?> component) {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < component.getSlots(); i++) {
            var stack = component.getStackInSlot(i);
            if (!stack.isEmpty()) {
                tag.put(i + "", ItemStack.OPTIONAL_CODEC.encodeStart(registryAccess.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow());
            }
        }
        return tag;
    }

    public static List<ItemStack> readInventory(RegistryAccess registryAccess, CompoundTag tag) {
        List<ItemStack> stacks = new ArrayList<>();
        for (String allKey : tag.keySet()) {
            stacks.add(ItemStack.OPTIONAL_CODEC.parse(registryAccess.createSerializationContext(NbtOps.INSTANCE), tag.getCompoundOrEmpty(allKey)).result().orElse(ItemStack.EMPTY));
        }
        return stacks;
    }

    void loadSettings(Player player, CompoundTag tag);

    void saveSettings(Player player, CompoundTag tag);
}
