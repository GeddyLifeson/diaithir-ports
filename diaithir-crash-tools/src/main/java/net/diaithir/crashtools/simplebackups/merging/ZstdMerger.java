package net.diaithir.crashtools.simplebackups.merging;

import com.mojang.brigadier.context.CommandContext;
import net.diaithir.crashtools.simplebackups.BackupChain;
import net.diaithir.crashtools.simplebackups.ToolsLoader;
import net.diaithir.crashtools.simplebackups.compression.CompressionBase;
import net.minecraft.commands.CommandSourceStack;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ZstdMerger extends MergerBase {

    public ZstdMerger(BackupChain chain, CommandContext<CommandSourceStack> commandContext) {
        super(chain, commandContext);
    }

    @Override
    protected void mergeFiles(Function<Exception, IllegalStateException> onError) {
        try (OutputStream outputStream = ToolsLoader.wrapWithZstd(new FileOutputStream(this.mergedBackupPath().toFile()), -1)) {
            TarArchiveOutputStream tarOut = new TarArchiveOutputStream(outputStream);

            tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            tarOut.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
            tarOut.setAddPaxHeadersForNonAsciiNames(true);

            Map<String, Path> dataFiles = new HashMap<>();

            for (Path backupSource : this.getArchiveFiles()) {
                this.processFile(backupSource, dataFiles);
            }

            this.writeMergedArchiveFile(tarOut, dataFiles);
        } catch (IOException e) {
            throw new IllegalStateException("Error while processing backups", e);
        }
    }

    @Override
    protected CompressionBase.BackupFormat getFormat() {
        return CompressionBase.BackupFormat.ZSTD;
    }

    private void processFile(Path file, Map<String, Path> archiveFiles) throws IOException {
        if (file.toString().endsWith(this.getFormat().getExtension())) {
            try (TarArchiveInputStream tis = new TarArchiveInputStream(
                    ToolsLoader.wrapZstdInput(new FileInputStream(file.toFile()))
            )) {
                TarArchiveEntry entry = tis.getNextEntry();
                while (entry != null) {
                    String name = entry.getName();
                    archiveFiles.merge(name, file, this::getLatestModifiedFile);
                    entry = tis.getNextEntry();
                }
            }
        }
    }

    private void writeMergedArchiveFile(TarArchiveOutputStream tarOut, Map<String, Path> dataFiles) throws IOException {
        byte[] buffer = new byte[8192];

        for (Map.Entry<String, Path> mapEntry : dataFiles.entrySet()) {
            String fileName = mapEntry.getKey();
            Path archiveFilePath = mapEntry.getValue();

            try (FileInputStream fis = new FileInputStream(archiveFilePath.toFile());
                 InputStream zis = ToolsLoader.wrapZstdInput(fis);
                 TarArchiveInputStream tis = new TarArchiveInputStream(zis)) {

                TarArchiveEntry inEntry;
                while ((inEntry = tis.getNextEntry()) != null) {
                    if (!inEntry.getName().equals(fileName)) {
                        long skip = inEntry.getSize();
                        if (skip > 0) {
                            long toSkip = skip;
                            while (toSkip > 0) {
                                long s = tis.skip(toSkip);
                                if (s <= 0) break;
                                toSkip -= s;
                            }
                        } else {
                            // size unknown: drain until next entry (read until -1 or until reach end-of-entry)
                            while (true) {
                                if (tis.read(buffer) == -1) {
                                    break;
                                }
                            }
                        }

                        continue;
                    }

                    // Create a new entry to avoid reusing the input entry object
                    TarArchiveEntry outEntry = new TarArchiveEntry(inEntry.getName());
                    outEntry.setSize(inEntry.getSize() >= 0 ? inEntry.getSize() : 0);
                    outEntry.setMode(inEntry.getMode());
                    outEntry.setModTime(inEntry.getModTime());
                    outEntry.setUserId(inEntry.getLongUserId());
                    outEntry.setGroupId(inEntry.getLongGroupId());
                    if (inEntry.isSymbolicLink()) outEntry.setLinkName(inEntry.getLinkName());

                    tarOut.putArchiveEntry(outEntry);

                    // Copy contents; if size is known, read exactly that many bytes
                    if (inEntry.getSize() >= 0) {
                        long remaining = inEntry.getSize();
                        int read;
                        while (remaining > 0 && (read = tis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                            tarOut.write(buffer, 0, read);
                            remaining -= read;
                        }
                    } else {
                        // size unknown: copy until stream signals end of entry
                        int read;
                        while ((read = tis.read(buffer)) != -1) {
                            tarOut.write(buffer, 0, read);
                        }
                    }

                    tarOut.closeArchiveEntry();
                    break;
                }
            }
        }
    }
}
