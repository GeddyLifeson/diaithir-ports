package net.diaithir.druidry.ars.api.ritual;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Mutable state of a ritual in progress, persisted with the brazier. */
public class RitualContext {
    public List<ItemStack> consumedItems = new ArrayList<>();
    public boolean isStarted;
    public boolean isDone;
    public boolean needsSourceToRun = true;
    public int progress;

    public void write(HolderLookup.Provider provider, CompoundTag tag) {
        tag.putBoolean("started", isStarted);
        tag.putBoolean("done", isDone);
        tag.putBoolean("needsSource", needsSourceToRun);
        tag.putInt("progress", progress);
        ListTag list = new ListTag();
        for (ItemStack stack : consumedItems) {
            if (!stack.isEmpty()) {
                list.add(ItemStack.CODEC.encodeStart(provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), stack).getOrThrow());
            }
        }
        tag.put("consumed", list);
    }

    public static RitualContext read(HolderLookup.Provider provider, CompoundTag tag) {
        RitualContext context = new RitualContext();
        context.isStarted = tag.getBooleanOr("started", false);
        context.isDone = tag.getBooleanOr("done", false);
        context.needsSourceToRun = tag.getBooleanOr("needsSource", true);
        context.progress = tag.getIntOr("progress", 0);
        for (var entry : tag.getListOrEmpty("consumed")) {
            ItemStack.CODEC.parse(provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), entry).result().ifPresent(context.consumedItems::add);
        }
        return context;
    }
}
