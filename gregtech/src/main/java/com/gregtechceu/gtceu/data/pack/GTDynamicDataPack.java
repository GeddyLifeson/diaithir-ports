package com.gregtechceu.gtceu.data.pack;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.InclusiveRange;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Server data pack whose files (recipes, loot tables, tags) are generated in code on the first access of each
 * reload. Replaces the mixin based pack of the 1.21.1 mod.
 */
public class GTDynamicDataPack implements PackResources {

    private static final Map<Identifier, byte[]> FILES = new TreeMap<>();

    private final PackLocationInfo info;

    public GTDynamicDataPack(PackLocationInfo info) {
        this.info = info;
    }

    /** Forget the generated files; they are rebuilt on the next resource access. */
    public static synchronized void invalidate() {
        tagsGenerated = false;
        recipesGenerated = false;
        FILES.clear();
    }

    public static synchronized void addResource(Identifier file, byte[] data) {
        FILES.put(file, data);
    }

    public static synchronized boolean has(Identifier file) {
        return FILES.containsKey(file);
    }

    private static boolean tagsGenerated = false;
    private static boolean recipesGenerated = false;

    private static synchronized void ensureTags() {
        if (tagsGenerated) return;
        tagsGenerated = true;
        try {
            GTDynamicDataGenerator.generateTagsAndLoot();
        } catch (Throwable t) {
            GTCEu.LOGGER.error("Failed to generate GTCEu dynamic tags", t);
        }
    }

    private static synchronized void ensureRecipes() {
        if (recipesGenerated) return;
        recipesGenerated = true;
        try {
            GTDynamicDataGenerator.generateRecipes();
        } catch (Throwable t) {
            GTCEu.LOGGER.error("Failed to generate GTCEu dynamic recipes", t);
        }
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        return null;
    }

    @Override
    public synchronized @Nullable IoSupplier<InputStream> getResource(PackType type, Identifier location) {
        if (type != PackType.SERVER_DATA) return null;
        byte[] data = FILES.get(location);
        return data == null ? null : () -> new ByteArrayInputStream(data);
    }

    @Override
    public synchronized void listResources(PackType type, String namespace, String directory,
                                           ResourceOutput output) {
        if (type != PackType.SERVER_DATA) return;
        if (!(directory.equals("recipe") || directory.equals("loot_table") || directory.equals("tags") ||
                directory.startsWith("tags/") || directory.equals("advancement"))) {
            return;
        }
        if (directory.equals("recipe")) {
            if (GTDynamicDataGenerator.RESOURCES == null) return;
            ensureRecipes();
        } else {
            ensureTags();
        }
        String prefix = directory.isEmpty() ? "" : directory + "/";
        for (var e : FILES.entrySet()) {
            Identifier id = e.getKey();
            if (id.getNamespace().equals(namespace) && id.getPath().startsWith(prefix)) {
                byte[] data = e.getValue();
                output.accept(id, () -> new ByteArrayInputStream(data));
            }
        }
    }

    @Override
    public synchronized Set<String> getNamespaces(PackType type) {
        if (type != PackType.SERVER_DATA) return Set.of();
        return Set.of(GTCEu.MOD_ID, "minecraft", "c", "neoforge");
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T> T getMetadataSection(MetadataSectionType<T> type) {
        if (type.equals(PackMetadataSection.SERVER_TYPE) || type.equals(PackMetadataSection.FALLBACK_TYPE)) {
            var fmt = SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA);
            return (T) new PackMetadataSection(Component.literal("GTCEu dynamic data"), new InclusiveRange<>(fmt));
        }
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return info;
    }

    @Override
    public void close() {}
}
