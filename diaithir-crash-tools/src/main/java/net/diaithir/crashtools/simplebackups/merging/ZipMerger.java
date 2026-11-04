package net.diaithir.crashtools.simplebackups.merging;

import com.mojang.brigadier.context.CommandContext;
import net.diaithir.crashtools.simplebackups.BackupChain;
import net.diaithir.crashtools.simplebackups.compression.CompressionBase;
import net.minecraft.commands.CommandSourceStack;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipMerger extends MergerBase {

    public ZipMerger(BackupChain chain, CommandContext<CommandSourceStack> commandContext) {
        super(chain, commandContext);
    }

    @Override
    protected void mergeFiles(Function<Exception, IllegalStateException> onError) {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(this.mergedBackupPath().toFile()))) {
            Map<String, Path> dataFiles = new HashMap<>();

            // Walk the file tree of the output path
            for (Path backupSource : this.getArchiveFiles()) {
                this.processFile(backupSource, dataFiles);
            }

            // Write the merged zip file
            this.writeMergedZipFile(zos, dataFiles);
        } catch (IOException e) {
            throw new IllegalStateException("Error while processing backups", e);
        }
    }

    @Override
    protected CompressionBase.BackupFormat getFormat() {
        return CompressionBase.BackupFormat.ZIP;
    }

    private void processFile(Path file, Map<String, Path> zipFiles) throws IOException {
        if (file.toString().endsWith(this.getFormat().getExtension())) {
            try (ZipFile zipFile = new ZipFile(file.toFile())) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String name = entry.getName();

                    zipFiles.merge(name, file, this::getLatestModifiedFile);
                }
            }
        }
    }

    private void writeMergedZipFile(ZipOutputStream zos, Map<String, Path> zipFiles) throws IOException {
        for (Map.Entry<String, Path> entry : zipFiles.entrySet()) {
            String fileName = entry.getKey();
            Path zipFilePath = entry.getValue();

            try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
                ZipEntry zipEntry = zipFile.getEntry(fileName);
                if (zipEntry != null) {
                    zos.putNextEntry(new ZipEntry(fileName));

                    try (InputStream is = zipFile.getInputStream(zipEntry)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = is.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                    }

                    zos.closeEntry();
                }
            }
        }
    }
}
