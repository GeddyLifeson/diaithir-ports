package com.simibubi.create.foundation.data;

import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

/** Datagen helpers are no-ops: addon jars already contain generated assets. Only the shapes the addons call are provided. */
public class AssetLookup {
    public static <C, P> NonNullBiConsumer<C, P> existingItemModel() { return (c, p) -> {}; }
    public static <C, P> NonNullBiConsumer<C, P> itemModelWithPartials() { return (c, p) -> {}; }
    public static <C, P> NonNullBiConsumer<C, P> customBlockItemModel(String... path) { return (c, p) -> {}; }
    public static <C, P> NonNullBiConsumer<C, P> customGenericItemModel(String... path) { return (c, p) -> {}; }
}
