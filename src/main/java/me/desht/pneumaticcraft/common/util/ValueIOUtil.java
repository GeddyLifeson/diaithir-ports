package me.desht.pneumaticcraft.common.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * Bridges 26.1's ValueIO serialization back to the CompoundTag-based save/load code used throughout this mod.
 */
public class ValueIOUtil {
    public static CompoundTag save(ValueIOSerializable obj, HolderLookup.Provider provider) {
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        obj.serialize(out);
        return out.buildResult();
    }

    /** Entities/BEs keep their CompoundTag save hooks, stored under this one key of the 26.1 value tree. */
    public static final String COMPAT_KEY = "pnc";

    public static void storeCompat(ValueOutput output, CompoundTag tag) {
        output.store(COMPAT_KEY, CompoundTag.CODEC, tag);
    }

    public static CompoundTag readCompat(ValueInput input) {
        return input.read(COMPAT_KEY, CompoundTag.CODEC).orElseGet(CompoundTag::new);
    }

    public static void load(ValueIOSerializable obj, HolderLookup.Provider provider, CompoundTag tag) {
        obj.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, provider, tag));
    }
}
