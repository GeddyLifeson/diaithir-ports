package me.desht.pneumaticcraft.compat;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;

/**
 * Replacement for the NeoForge 1.21 INBTSerializable, which no longer exists in 26.1.
 */
public interface INBTSerializable<T extends Tag> {
    T serializeNBT(HolderLookup.Provider provider);

    void deserializeNBT(HolderLookup.Provider provider, T nbt);
}
