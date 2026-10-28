package com.gregtechceu.gtceu.compat;

import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;

public final class TagCompat {

    private TagCompat() {}

    /**
     * Resolves a tag to a (possibly not yet bound) HolderSet. Tags are not bound while mod content registers, so
     * fall back to the registration lookup, which hands out sets that are bound once tags load.
     */
    public static <T> HolderSet.Named<T> tag(Registry<T> registry, TagKey<T> tag) {
        try {
            return registry.getOrThrow(tag);
        } catch (IllegalStateException e) {
            return BuiltInRegistries.acquireBootstrapRegistrationLookup(registry).getOrThrow(tag);
        }
    }
}
