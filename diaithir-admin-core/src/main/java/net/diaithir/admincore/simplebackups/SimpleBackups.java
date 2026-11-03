// Ported from Simple Backups (Apache-2.0) https://github.com/ChaoticTrials/SimpleBackups
// Modifications: shared constants/helpers; level id/dir derived from MinecraftServer#getWorldPath instead of the protected storageSource.
package net.diaithir.admincore.simplebackups;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

public final class SimpleBackups {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MODID = "simplebackups";
    public static final String EXTENSION = ".zip";

    private SimpleBackups() {}

    public static String levelId(MinecraftServer server) {
        Path root = server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
        Path name = root.getFileName();
        return name == null ? "world" : name.toString();
    }

    public static Path levelDir(MinecraftServer server) throws IOException {
        return server.getWorldPath(LevelResource.ROOT).toRealPath();
    }
}
