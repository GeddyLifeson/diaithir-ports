/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Optional;

public final class NBTUtils {
    public static <T extends Enum<T>> void putEnum(final CompoundTag compound, final String key, @Nullable final Enum<T> value) {
        if (value != null) {
            compound.putInt(key, value.ordinal());
        }
    }

    @Nullable
    public static <T extends Enum<T>> T getEnum(final CompoundTag compound, final String key, final Class<T> enumType) {
        if (!compound.contains(key)) {
            return null;
        }

        final int ordinal = compound.getIntOr(key, 0);
        try {
            return enumType.getEnumConstants()[ordinal];
        } catch (final IndexOutOfBoundsException ignored) {
            return null;
        }
    }

    public static CompoundTag getChildTag(@Nullable final ItemStack stack, final String... path) {
        if (stack == null || !stack.has(DataComponents.CUSTOM_DATA)) {
            return new CompoundTag();
        }

        return getChildTag(stack.get(DataComponents.CUSTOM_DATA), path);
    }

    public static CompoundTag getChildTag(@Nullable final CustomData nbt, final String... path) {
        if (nbt == null) {
            return new CompoundTag();
        }

        return getChildTag(nbt.copyTag(), path);
    }

    public static CompoundTag getChildTag(@Nullable final CompoundTag tag, final String... path) {
        if (tag == null) {
            return new CompoundTag();
        }

        CompoundTag childTag = tag;
        for (final String tagName : path) {
            if (!childTag.contains(tagName)) {
                return new CompoundTag();
            }
            childTag = childTag.getCompoundOrEmpty(tagName);
        }

        return childTag;
    }

    public static CompoundTag getOrCreateChildTag(final CompoundTag tag, final String... path) {
        CompoundTag childTag = tag;
        for (final String tagName : path) {
            if (!childTag.contains(tagName)) {
                childTag.put(tagName, new CompoundTag());
            }
            childTag = childTag.getCompoundOrEmpty(tagName);
        }
        return childTag;
    }

    public static CompoundTag makeInventoryTag(HolderLookup.Provider provider, final ItemStack... items) {
        final ItemStackHandler handler = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, items));
        final TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        handler.serialize(output);
        return output.buildResult();
    }

    /// Tries to read an older format read/writeBlockPos used to use
    public static Optional<BlockPos> readBlockPosLegacy(CompoundTag tag)
    {
        if (tag.getInt("X").isEmpty() ||
            tag.getInt("Y").isEmpty() ||
            tag.getInt("Z").isEmpty()
        ) {
            return Optional.empty();
        }
        return Optional.of(new BlockPos(tag.getIntOr("X", 0), tag.getIntOr("Y", 0), tag.getIntOr("Z", 0)));
    }
}
