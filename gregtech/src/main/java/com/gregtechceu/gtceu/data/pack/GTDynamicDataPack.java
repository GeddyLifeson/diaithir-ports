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
    private static boolean generated = false;

    private final PackLocationInfo info;

    public GTDynamicDataPack(PackLocationInfo info) {
        this.info = info;
    }

    /** Forget the generated files; they are rebuilt on the next resource access. */
    public static synchronized void invalidate() {
        generated = false;
        FILES.clear();
    }

    public static synchronized void addResource(Identifier file, byte[] data) {
        FILES.put(file, data);
    }

    private static synchronized void ensureGenerated() {
        if (generated) return;
        generated = true;
        try {
            GTDynamicDataGenerator.generate();
        } catch (Throwable t) {
            GTCEu.LOGGER.error("Failed to generate GTCEu dynamic data", t);
        }
        GTCEu.LOGGER.info("Generated {} dynamic data files", FILES.size());
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        return null;
    }

    @Override
    public synchronized @Nullable IoSupplier<InputStream> getResource(PackType type, Identifier location) {
        if (type != PackType.SERVER_DATA) return null;
        ensureGenerated();
        byte[] data = FILES.get(location);
        return data == null ? null : () -> new ByteArrayInputStream(data);
    }

    @Override
    public synchronized void listResources(PackType type, String namespace, String directory,
                                           ResourceOutput output) {
        if (type != PackType.SERVER_DATA) return;
        ensureGenerated();
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
        ensureGenerated();
        var set = new java.util.HashSet<String>();
        FILES.keySet().forEach(id -> set.add(id.getNamespace()));
        return set;
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
