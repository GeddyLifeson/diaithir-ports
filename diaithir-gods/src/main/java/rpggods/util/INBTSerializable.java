package rpggods.util;

import net.minecraft.nbt.Tag;

/**
 * Replacement for the Forge interface of the same name, which no longer exists in NeoForge.
 */
public interface INBTSerializable<T extends Tag> {
    T serializeNBT();

    void deserializeNBT(T nbt);
}
