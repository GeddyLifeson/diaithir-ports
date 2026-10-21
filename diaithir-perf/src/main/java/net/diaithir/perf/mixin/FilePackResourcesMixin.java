package net.diaithir.perf.mixin;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Every resource listing (each registry's tags, data maps, registry entries) walked every entry of every mod jar.
 * With 316 jars that was 62% of server startup CPU. Each pack now keeps a sorted list of its file names per pack
 * type, built on first use and dropped on close, and a listing is a binary search.
 */
@Mixin(FilePackResources.class)
abstract class FilePackResourcesMixin {
    @Unique private static final Logger diaithirperf$LOG = LogUtils.getLogger();
    @Shadow @Final private FilePackResources.SharedZipFileAccess zipFileAccess;
    @Unique private final String[][] diaithirperf$names = new String[PackType.values().length][];

    @Shadow
    protected abstract String addPrefix(String path);

    @Inject(method = "listResources", at = @At("HEAD"), cancellable = true)
    private void diaithirperf$listIndexed(PackType type, String namespace, String directory, PackResources.ResourceOutput output, CallbackInfo ci) {
        ZipFile zip = ((SharedZipFileAccessAccessor) zipFileAccess).diaithirperf$zip();
        if (zip == null) return;
        String[] names = diaithirperf$names(zip, type);
        String root = addPrefix(type.getDirectory() + "/" + namespace + "/");
        String prefix = root + directory + "/";
        int i = Arrays.binarySearch(names, prefix);
        for (i = i < 0 ? -i - 1 : i; i < names.length && names[i].startsWith(prefix); i++) {
            String path = names[i].substring(root.length());
            Identifier id = Identifier.tryBuild(namespace, path);
            ZipEntry entry = zip.getEntry(names[i]);
            if (id == null) diaithirperf$LOG.warn("Invalid path in datapack: {}:{}, ignoring", namespace, path);
            else if (entry != null) output.accept(id, IoSupplier.create(zip, entry));
        }
        ci.cancel();
    }

    @Unique
    private String[] diaithirperf$names(ZipFile zip, PackType type) {
        String[] names = diaithirperf$names[type.ordinal()];
        if (names != null) return names;
        String typePrefix = addPrefix(type.getDirectory() + "/");
        List<String> found = new ArrayList<>();
        for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements(); ) {
            ZipEntry entry = e.nextElement();
            if (!entry.isDirectory() && entry.getName().startsWith(typePrefix)) found.add(entry.getName());
        }
        names = found.toArray(String[]::new);
        Arrays.sort(names);
        // Racing threads build identical arrays; whichever lands is fine.
        return diaithirperf$names[type.ordinal()] = names;
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void diaithirperf$dropIndex(CallbackInfo ci) {
        Arrays.fill(diaithirperf$names, null);
    }
}
