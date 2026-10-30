package com.otectus.immersivetanning.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/** The {@code immersive_tanning} compound inside a stack's custom data, where hide, leather and leatherwork data live. */
public final class StackNbt {
    public static final String ROOT = "immersive_tanning";

    /** A copy of the compound, or null when the stack has none. */
    public static @Nullable CompoundTag root(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains(ROOT)) return null;
        return data.copyTag().getCompoundOrEmpty(ROOT);
    }

    /** Edits the compound in place, creating it first when the stack has none. */
    public static void edit(ItemStack stack, Consumer<CompoundTag> edit) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag root = tag.getCompoundOrEmpty(ROOT);
            edit.accept(root);
            tag.put(ROOT, root);
        });
    }

    public static void set(ItemStack stack, CompoundTag root) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put(ROOT, root));
    }

    private StackNbt() {}
}
