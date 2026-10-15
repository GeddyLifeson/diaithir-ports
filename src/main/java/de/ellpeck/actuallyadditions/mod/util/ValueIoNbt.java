package de.ellpeck.actuallyadditions.mod.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/** 26.1 handlers save through ValueIO; Actually Additions still keeps them as CompoundTags inside its own NBT. */
public final class ValueIoNbt {
    private ValueIoNbt() {}

    public static CompoundTag save(ValueIOSerializable object, HolderLookup.Provider provider) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        object.serialize(output);
        return output.buildResult();
    }

    public static void load(ValueIOSerializable object, HolderLookup.Provider provider, CompoundTag tag) {
        object.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, provider, tag));
    }
}
