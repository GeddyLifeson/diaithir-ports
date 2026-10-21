package net.diaithir.perf.mixin;

import java.util.zip.ZipFile;
import net.minecraft.server.packs.FilePackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FilePackResources.SharedZipFileAccess.class)
public interface SharedZipFileAccessAccessor {
    @Invoker("getOrCreateZipFile")
    ZipFile diaithirperf$zip();
}
