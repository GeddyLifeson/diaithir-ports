package net.diaithir.druidry.ars.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static net.diaithir.druidry.ars.setup.registry.RegistryHelper.getRegistryName;

public class NBTUtil {

    public static int INT_LIST_TAG_TYPE = 11;

    public static CompoundTag storeBlockPos(CompoundTag tag, String prefix, BlockPos pos) {
        if (pos == null)
            return tag;
        writePositional(tag, prefix, pos.getX(), pos.getY(), pos.getZ());
        return tag;
    }

    public static CompoundTag storeVec(CompoundTag tag, String prefix, Vec3 vec) {
        writePositional(tag, prefix, vec.x, vec.y, vec.z);
        return tag;
    }

    public static CompoundTag removeBlockPos(CompoundTag tag, String prefix) {
        tag.remove(prefix + "_x");
        tag.remove(prefix + "_y");
        tag.remove(prefix + "_z");
        return tag;
    }

    public static CompoundTag writePositional(CompoundTag tag, String prefix, double x, double y, double z) {
        tag.putDouble(prefix + "_x", x);
        tag.putDouble(prefix + "_y", y);
        tag.putDouble(prefix + "_z", z);
        return tag;
    }

    @Deprecated
    public static BlockPos getBlockPos(CompoundTag tag, String prefix) {
        return BlockPos.containing(tag.getDoubleOr(prefix + "_x", 0), tag.getDoubleOr(prefix + "_y", 0), tag.getDoubleOr(prefix + "_z", 0));
    }

    public static BlockPos getPos(int[] arr) {
        return new BlockPos(arr[0], arr[1], arr[2]);
    }

    public static Vec3 getVec(CompoundTag tag, String prefix) {
        if (tag == null) {
            return null;
        }
        return new Vec3(tag.getDoubleOr(prefix + "_x", 0), tag.getDoubleOr(prefix + "_y", 0), tag.getDoubleOr(prefix + "_z", 0));
    }

    public static @Nullable BlockPos getNullablePos(CompoundTag tag, String prefix) {
        if (!tag.contains(prefix + "_x"))
            return null;
        return BlockPos.containing(tag.getDoubleOr(prefix + "_x", 0), tag.getDoubleOr(prefix + "_y", 0), tag.getDoubleOr(prefix + "_z", 0));
    }


    public static boolean hasBlockPos(CompoundTag tag, String prefix) {
        return tag.contains(prefix + "_x");
    }

    public static List<ItemStack> readItems(HolderLookup.Provider pRegistries, CompoundTag tag, String prefix) {
        List<ItemStack> stacks = new ArrayList<>();

        if (tag == null)
            return stacks;
        try {
            CompoundTag itemsTag = tag.getCompoundOrEmpty(prefix + "_tag");
            int numItems = itemsTag.getIntOr("itemsSize", 0);
            for (int i = 0; i < numItems; i++) {
                String key = prefix + "_" + i;
                stacks.add(ItemStack.CODEC.parse(pRegistries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), itemsTag.getCompoundOrEmpty(key)).result().orElse(ItemStack.EMPTY));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stacks;
    }

    public static void writeItems(HolderLookup.Provider pRegistries, CompoundTag tag, String prefix, List<ItemStack> items) {
        CompoundTag allItemsTag = new CompoundTag();
        items = items.stream().filter(stack -> !stack.isEmpty()).collect(Collectors.toList());
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            Tag itemTag = ItemStack.CODEC.encodeStart(pRegistries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), stack).getOrThrow();
            allItemsTag.put(prefix + "_" + i, itemTag);
        }
        allItemsTag.putInt("itemsSize", items.size());
        tag.put(prefix + "_tag", allItemsTag);
    }


    public static List<String> readStrings(CompoundTag tag, String prefix) {
        List<String> strings = new ArrayList<>();
        if (tag == null)
            return strings;

        for (String s : tag.keySet()) {
            if (s.contains(prefix)) {
                strings.add(tag.getStringOr(s, ""));
            }
        }
        return strings;
    }

    public static void writeStrings(CompoundTag tag, String prefix, Collection<String> strings) {
        int i = 0;
        for (String s : strings) {
            tag.putString(prefix + "_" + i, s);
            i++;
        }
    }

    public static void writeResourceLocations(CompoundTag tag, String prefix, Collection<Identifier> resourceLocations) {
        writeStrings(tag, prefix, resourceLocations.stream().map(Identifier::toString).collect(Collectors.toList()));
    }

    public static List<Identifier> readResourceLocations(CompoundTag tag, String prefix) {
        return readStrings(tag, prefix).stream().map(Identifier::tryParse).collect(Collectors.toList());
    }

    public static String getItemKey(ItemStack stack, String prefix) {
        return prefix + getRegistryName(stack.getItem()).toString();
    }
}
