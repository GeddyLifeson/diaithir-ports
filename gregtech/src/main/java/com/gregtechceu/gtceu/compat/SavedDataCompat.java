package com.gregtechceu.gtceu.compat;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.minecraft.world.level.saveddata.SavedData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.saveddata.SavedDataType;


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

    private static final Map<SavedDataStorage, Map<String, SavedDataType<?>>> TYPES = new WeakHashMap<>();

    @SuppressWarnings("unchecked")
    public static synchronized <T extends SavedData> T computeIfAbsent(ServerLevel level, String name,
                                                                       Supplier<T> constructor,
                                                                       BiFunction<CompoundTag, HolderLookup.Provider, T> loader) {
        SavedDataStorage storage = level.getDataStorage();
        HolderLookup.Provider provider = level.registryAccess();
        Map<String, SavedDataType<?>> map = TYPES.computeIfAbsent(storage, k -> new java.util.HashMap<>());
        SavedDataType<T> type = (SavedDataType<T>) map.computeIfAbsent(name, n -> {
            Codec<T> codec = new Codec<>() {

                @Override
                public <O> DataResult<com.mojang.datafixers.util.Pair<T, O>> decode(com.mojang.serialization.DynamicOps<O> ops, O input) {
                    net.minecraft.nbt.Tag tag = ops.convertTo(NbtOps.INSTANCE, input);
                    if (!(tag instanceof CompoundTag ct)) return DataResult.error(() -> "Not a compound");
                    try {
                        return DataResult.success(com.mojang.datafixers.util.Pair.of(loader.apply(ct, provider), input));
                    } catch (Exception e) {
                        return DataResult.error(() -> "Failed to load " + n + ": " + e);
                    }
                }

                @Override
                public <O> DataResult<O> encode(T input, com.mojang.serialization.DynamicOps<O> ops, O prefix) {
                    try {
                        return DataResult.success(NbtOps.INSTANCE.convertTo(ops, save(input, provider)));
                    } catch (Exception e) {
                        return DataResult.error(() -> "Failed to save " + n + ": " + e);
                    }
                }
            };
            return new SavedDataType<>(GTCEu.id(sanitize(n)), constructor, codec);
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
