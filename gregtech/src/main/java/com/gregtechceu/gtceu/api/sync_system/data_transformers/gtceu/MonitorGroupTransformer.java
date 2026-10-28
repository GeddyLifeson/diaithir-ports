package com.gregtechceu.gtceu.api.sync_system.data_transformers.gtceu;

import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;

import org.jetbrains.annotations.Nullable;

public class MonitorGroupTransformer implements ValueTransformer<MonitorGroup> {

    @Override
    public CompoundTag serializeNBT(MonitorGroup value, ValueTransformer.TransformerContext<MonitorGroup> context) {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", value.getName());
        ListTag list = new ListTag();
        value.getMonitorPositions().forEach(pos -> list.add(com.gregtechceu.gtceu.compat.NbtCompat.writeBlockPos(pos)));
        if (value.getTargetRaw() != null) {
            tag.put("targetPos", com.gregtechceu.gtceu.compat.NbtCompat.writeBlockPos(value.getTargetRaw()));
            if (value.getTargetCoverSide() != null) {
                tag.putString("targetSide", value.getTargetCoverSide().getSerializedName());
            }
        }
        tag.put("positions", list);
        tag.putInt("dataSlot", value.getDataSlot());
        tag.put("items", com.gregtechceu.gtceu.compat.NbtCompat.save(value.getItemStackHandler(), context.lookup()));
        tag.put("placeholderSlots", com.gregtechceu.gtceu.compat.NbtCompat.save(value.getPlaceholderSlotsHandler(), context.lookup()));
        return tag;
    }

    @Override
    public @Nullable MonitorGroup deserializeNBT(Tag tag, ValueTransformer.TransformerContext<MonitorGroup> context) {
        var compoundTag = ValueTransformer.assertTagType(CompoundTag.class, tag, context);
        CustomItemStackHandler handler = new CustomItemStackHandler(),
                placeholderSlotsHandler = new CustomItemStackHandler();
        com.gregtechceu.gtceu.compat.NbtCompat.load(handler, context.lookup(), compoundTag.getCompoundOrEmpty("items"));
        com.gregtechceu.gtceu.compat.NbtCompat.load(placeholderSlotsHandler, context.lookup(), compoundTag.getCompoundOrEmpty("placeholderSlots"));
        var group = new MonitorGroup(compoundTag.getStringOr("name", ""), handler, placeholderSlotsHandler);
        ListTag list = compoundTag.getListOrEmpty("positions");
        for (int i = 0; i < list.size(); i++) {
            int[] aint = list.getIntArray(i).orElse(new int[0]);
            if (aint.length != 3) continue;
            group.add(new BlockPos(aint[0], aint[1], aint[2]));
        }
        if (compoundTag.contains("targetPos")) {
            group.setTarget(com.gregtechceu.gtceu.compat.NbtCompat.readBlockPos(compoundTag, "targetPos").orElse(BlockPos.ZERO));
            if (compoundTag.contains("targetSide")) {
                group.setTargetCoverSide(Direction.byName(compoundTag.getStringOr("targetSide", "")));
            }
            if (compoundTag.contains("dataSlot")) {
                group.setDataSlot(compoundTag.getIntOr("dataSlot", 0));
            }
        }
        return group;
    }
}
