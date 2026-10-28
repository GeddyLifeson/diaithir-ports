package com.gregtechceu.gtceu.compat;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * 26.1 replaced SavedData.Factory / SavedData#save by codec based {@link SavedDataType}s. This bridges the
 * 1.21-style classes of this mod (which expose a {@code save(CompoundTag, HolderLookup.Provider)} method and a
 * {@code (CompoundTag, Provider)} loader) to the new system.
 */
public final class SavedDataCompat {
    private SavedDataCompat() {}

    private static final Map<DimensionDataStorage, Map<String, SavedDataType<?>>> TYPES = new WeakHashMap<>();

    @SuppressWarnings("unchecked")
    public static synchronized <T extends SavedData> T computeIfAbsent(ServerLevel level, String name,
                                                                       Supplier<T> constructor,
                                                                       BiFunction<CompoundTag, HolderLookup.Provider, T> loader) {
        DimensionDataStorage storage = level.getDataStorage();
        SavedDataType<T> type = (SavedDataType<T>) TYPES.computeIfAbsent(storage, s -> new java.util.HashMap<>())
                .computeIfAbsent(name, n -> {
                    HolderLookup.Provider provider = level.registryAccess();
                    return new SavedDataType<T>(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID, sanitize(n)), constructor,
                            CompoundTag.CODEC.xmap(tag -> loader.apply(tag, provider),
                                    data -> save(data, provider)));
                });
        return storage.computeIfAbsent(type);
    }

    private static String sanitize(String name) {
        String s = name.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_");
        return s.startsWith("gtceu_") ? s.substring("gtceu_".length()) : s;
    }

    private static CompoundTag save(SavedData data, HolderLookup.Provider provider) {
        try {
            Method m = findSave(data.getClass());
            return (CompoundTag) m.invoke(data, new CompoundTag(), provider);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot save " + data.getClass(), e);
        }
    }

    private static Method findSave(Class<?> c) throws NoSuchMethodException {
        for (Class<?> k = c; k != null; k = k.getSuperclass()) {
            try {
                Method m = k.getDeclaredMethod("save", CompoundTag.class, HolderLookup.Provider.class);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException ignored) {}
        }
        throw new NoSuchMethodException(c + "#save(CompoundTag, Provider)");
    }
}
