package net.diaithir.perf;

import com.mojang.logging.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.function.UnaryOperator;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

/**
 * Structure templates saved by older game versions are run through the data fixer every time they load, and that
 * also forces the whole data fixer (about 280 MB of heap in this pack) to be built. The upgraded template is
 * written to disk once, keyed by the template's content and the game's data version, and read back from then on.
 */
public final class StructureUpgradeCache {
    private static final Logger LOG = LogUtils.getLogger();
    private static final Path DIR = FMLPaths.GAMEDIR.get().resolve("diaithirperf/structures");
    private static final int VERSION = SharedConstants.getCurrentVersion().dataVersion().version();

    private StructureUpgradeCache() {}

    public static CompoundTag upgrade(CompoundTag tag, int fromVersion, UnaryOperator<CompoundTag> fixer) {
        if (fromVersion >= VERSION) return tag;
        Path file;
        try {
            file = DIR.resolve(key(tag) + ".nbt");
            if (Files.isRegularFile(file)) return NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
        } catch (IOException e) {
            LOG.warn("Structure upgrade cache read failed, upgrading directly", e);
            return fixer.apply(tag);
        }
        CompoundTag fixed = fixer.apply(tag);
        try {
            Files.createDirectories(DIR);
            Path tmp = Files.createTempFile(DIR, "tmp", ".nbt");
            NbtIo.writeCompressed(fixed, tmp);
            Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOG.warn("Structure upgrade cache write failed for {}", file, e);
        }
        return fixed;
    }

    private static String key(CompoundTag tag) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(1 << 14);
        NbtIo.write(tag, new DataOutputStream(bytes));
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(bytes.toByteArray());
            return VERSION + "-" + HexFormat.of().formatHex(hash, 0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
