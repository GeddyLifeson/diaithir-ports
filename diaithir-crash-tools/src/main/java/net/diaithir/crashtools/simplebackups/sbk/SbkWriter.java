package net.diaithir.crashtools.simplebackups.sbk;

import com.google.gson.JsonParser;
import net.diaithir.crashtools.simplebackups.SimpleBackups;
import net.diaithir.crashtools.simplebackups.ToolsLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;

/**
 * Compresses a list of files into an SBK archive.
 *
 * <h2>Algorithm selection</h2>
 * <p>The preferred algorithm is read from {@link SbkWriteOptions#preferredAlgorithm}.
 * If the corresponding library is not available, the algorithm is automatically switched to the other one.
 * If neither is available, an {@link SbkException} is thrown.
 *
 * <h2>Compression pipeline</h2>
 * <ol>
 *   <li>Classify each file by extension into its {@link SbkGroup}.</li>
 *   <li>Sort within each group by relative path (deterministic output).</li>
 *   <li>Preprocess: MCA → MCAP, NBT → raw NBT, JSON → compact JSON, RAW → verbatim.</li>
 *   <li>Stream preprocessed bytes into a per-group frame buffer (≤ 16 MiB).
 *       Full frames are submitted to a thread pool for parallel compression while
 *       the main thread continues preprocessing. Compressed frames are written in
 *       submission order.</li>
 *   <li>Write: placeholder header → frame data → frame directory → XZ/zstd index →
 *       seek back and write final header.</li>
 * </ol>
 *
 * <h2>On-disk layout</h2>
 * <pre>
 *   [79-byte header]
 *   [frame data - groups: MCA, NBT, JSON, RAW in order]
 *   [frame directory]   ← header.frameDirOffset points here
 *   [compressed index block]   ← header.indexOffset points here
 * </pre>
 */
public final class SbkWriter {

    private SbkWriter() {}

    private record PendingFrame(int rawSize, Future<byte[]> future) {}

    /**
     * Compresses the given files into {@code outputFile}.
     *
     * @param sourceDir  root directory; used to compute relative paths
     * @param files      pre-filtered list of absolute file paths to the archive
     * @param levelName  world name path, prepended to relative paths in the index
     * @param outputFile target {@code .sbk} file; created or overwritten
     * @param options    compression parameters
     * @param progress   progress callback
     * @return number of files written
     */
    public static long compress(Path sourceDir, List<Path> files, Path levelName, Path outputFile, SbkWriteOptions options, SbkProgress progress) throws IOException {
        SbkAlgorithm algorithm = SbkWriter.resolveAlgorithm(options.preferredAlgorithm);
        int level = options.lzmaPreset;

        int nThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        int maxInFlight = nThreads * 2;

        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        try {
            return SbkWriter.doCompress(sourceDir, files, levelName, outputFile, algorithm, level, maxInFlight, pool, progress);
        } finally {
            pool.shutdown();
        }
    }

    private static long doCompress(Path sourceDir, List<Path> files, Path levelName, Path outputFile, SbkAlgorithm algorithm, int level, int maxInFlight, ExecutorService pool, SbkProgress progress) throws IOException {
        // --- Classify + sort ---
        Map<SbkGroup, List<Path>> byGroup = new EnumMap<>(SbkGroup.class);
        for (SbkGroup g : SbkGroup.values()) byGroup.put(g, new ArrayList<>());
        for (Path file : files) {
            byGroup.get(SbkGroup.classify(sourceDir.relativize(file))).add(file);
        }

        for (SbkGroup g : SbkGroup.values()) {
            byGroup.get(g).sort(Comparator.comparing(p -> sourceDir.relativize(p).toString()));
        }

        int totalFiles = files.size();
        int[] completed = {0};

        Map<SbkGroup, SbkSolidBuilder> builders = new EnumMap<>(SbkGroup.class);
        for (SbkGroup g : SbkGroup.values()) {
            builders.put(g, new SbkSolidBuilder(g));
        }

        SbkFrameDir frameDir = new SbkFrameDir();
        long writtenFiles = 0;

        Files.deleteIfExists(outputFile);
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(outputFile.toFile(), "rw")) {

            // Write placeholder header
            randomAccessFile.write(new byte[SbkHeader.DISK_SIZE]);

            // --- Per-group streaming write with parallel frame compression ---
            for (SbkGroup group : SbkGroup.values()) {
                SbkSolidBuilder builder = builders.get(group);
                ByteArrayOutputStream frameBuffer = new ByteArrayOutputStream((int) SbkHeader.DEFAULT_FRAME_SIZE);
                Deque<PendingFrame> inFlight = new ArrayDeque<>();

                for (Path file : byGroup.get(group)) {
                    Path rel = sourceDir.relativize(file);
                    String archivePathStr = levelName.resolve(rel).toString().replace('\\', '/');

                    try {
                        byte[] rawBytes = Files.readAllBytes(file);
                        BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
                        long mtimeMs = attrs.lastModifiedTime().toMillis();
                        long originalSize = rawBytes.length;
                        int fileChecksum = SbkChecksum.xxHash32(rawBytes);

                        byte[] preprocessed = SbkWriter.preprocess(group, rawBytes);
                        //noinspection UnusedAssignment
                        rawBytes = null;

                        builder.add(archivePathStr, preprocessed.length, originalSize, mtimeMs, fileChecksum);

                        int written = 0;
                        while (written < preprocessed.length) {
                            int space = (int) (SbkHeader.DEFAULT_FRAME_SIZE - frameBuffer.size());
                            int toWrite = Math.min(space, preprocessed.length - written);
                            frameBuffer.write(preprocessed, written, toWrite);
                            written += toWrite;

                            if (frameBuffer.size() >= SbkHeader.DEFAULT_FRAME_SIZE) {
                                SbkWriter.submitFrame(frameBuffer, pool, algorithm, level, inFlight);
                                if (inFlight.size() >= maxInFlight) {
                                    SbkWriter.drainOldest(randomAccessFile, inFlight, group, frameDir);
                                }
                            }
                        }
                    } catch (IOException e) {
                        SimpleBackups.LOGGER.error("Failed to compress file {}", file, e);
                    }

                    completed[0]++;
                    progress.onFile(completed[0], totalFiles, archivePathStr);
                }

                if (frameBuffer.size() > 0) {
                    SbkWriter.submitFrame(frameBuffer, pool, algorithm, level, inFlight);
                }

                SbkWriter.drainAll(randomAccessFile, inFlight, group, frameDir);
            }

            // --- Write frame directory ---
            long frameDirOffset = randomAccessFile.getFilePointer();
            ByteArrayOutputStream frameDirOutputStream = new ByteArrayOutputStream();
            frameDir.write(new DataOutputStream(frameDirOutputStream));
            long frameDirSize = frameDirOutputStream.size();
            randomAccessFile.write(frameDirOutputStream.toByteArray());

            // --- Write compressed index (last, after frame directory) ---
            long indexOffset = randomAccessFile.getFilePointer();
            List<SbkIndexEntry> allEntries = new ArrayList<>();
            for (SbkGroup g : SbkGroup.values()) allEntries.addAll(builders.get(g).entries());

            ByteArrayOutputStream indexOutputStream = new ByteArrayOutputStream();
            SbkIndex.WriteResult indexResult = SbkIndex.write(allEntries, algorithm, level, indexOutputStream);
            randomAccessFile.write(indexOutputStream.toByteArray());

            // --- Seek back to 0 and write the final header ---
            writtenFiles = allEntries.size();
            SbkHeader finalHeader = new SbkHeader(
                    SbkHeader.FORMAT_VERSION, 0, algorithm,
                    writtenFiles,
                    SbkHeader.DEFAULT_FRAME_SIZE,
                    frameDirOffset, frameDirSize,
                    indexOffset,
                    indexResult.compressedSize(),
                    indexResult.rawSize(),
                    indexResult.checksum()
            );
            randomAccessFile.seek(0);
            ByteArrayOutputStream headerOutputStream = new ByteArrayOutputStream(SbkHeader.DISK_SIZE);
            finalHeader.write(headerOutputStream);
            randomAccessFile.write(headerOutputStream.toByteArray());
        }

        return writtenFiles;
    }

    /**
     * Resolves the actual algorithm to use. Falls back to the other algorithm if only one is available.
     *
     * @throws SbkException if neither algorithm is available
     */
    private static SbkAlgorithm resolveAlgorithm(SbkAlgorithm preferred) {
        if (!ToolsLoader.isLzmaAvailable() && !ToolsLoader.isZstdAvailable()) {
            throw new SbkException(
                    "Neither xz-java nor zstd-jni are available in external-dependencies. " +
                            "Download them and place them in the external-dependencies directory.");
        }

        if (preferred == SbkAlgorithm.LZMA2 && !ToolsLoader.isLzmaAvailable()) {
            SimpleBackups.LOGGER.warn(
                    "[SBK] xz-java not found in external-dependencies. Falling back to zstd. To use lzma2, download xz-java and place it in {}.",
                    ToolsLoader.RELATIVE_TOOLS_DIR);

            return SbkWriter.resolveAlgorithm(SbkAlgorithm.ZSTD);
        }

        if (preferred == SbkAlgorithm.ZSTD && !ToolsLoader.isZstdAvailable()) {
            SimpleBackups.LOGGER.warn(
                    "[SBK] zstd-jni not found in external-dependencies. Falling back to lzma2. To use zstd, download zstd-jni and place it in {}.",
                    ToolsLoader.RELATIVE_TOOLS_DIR);

            return SbkAlgorithm.LZMA2;
        }

        return preferred;
    }

    private static void submitFrame(ByteArrayOutputStream frameBuffer, ExecutorService pool, SbkAlgorithm algorithm, int level, Deque<PendingFrame> inFlight) {
        byte[] raw = frameBuffer.toByteArray();
        int rawSize = raw.length;
        Future<byte[]> future = pool.submit(() -> SbkFrameCompressor.compress(raw, algorithm, level));

        inFlight.add(new PendingFrame(rawSize, future));
        frameBuffer.reset();
    }

    private static void drainOldest(RandomAccessFile raf, Deque<PendingFrame> inFlight, SbkGroup group, SbkFrameDir frameDir) throws IOException {
        PendingFrame pf = inFlight.poll();
        if (pf != null) {
            SbkWriter.writeCompressedFrame(raf, pf, group, frameDir);
        }
    }

    private static void drainAll(RandomAccessFile raf, Deque<PendingFrame> inFlight, SbkGroup group, SbkFrameDir frameDir) throws IOException {
        while (!inFlight.isEmpty()) {
            SbkWriter.drainOldest(raf, inFlight, group, frameDir);
        }
    }

    private static void writeCompressedFrame(RandomAccessFile raf, PendingFrame pf, SbkGroup group, SbkFrameDir frameDir) throws IOException {
        byte[] compressed;
        try {
            compressed = pf.future().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SbkException("Frame compression interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException ioe) throw ioe;
            throw new SbkException("Frame compression failed", cause);
        }

        long frameOffset = raf.getFilePointer();
        raf.write(compressed);
        int checksum = SbkChecksum.xxHash32(compressed);

        frameDir.add(group, new SbkFrameEntry(frameOffset, compressed.length, pf.rawSize(), checksum));
    }

    private static byte[] preprocess(SbkGroup group, byte[] bytes) throws IOException {
        return switch(group) {
            case MCA -> McapProcessor.toMcap(bytes);
            case NBT -> SbkWriter.stripGzip(bytes);
            case JSON -> SbkWriter.minifyJson(bytes);
            case RAW -> bytes;
        };
    }

    private static byte[] stripGzip(byte[] bytes) {
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            return gzip.readAllBytes();
        } catch (IOException e) {
            return bytes;
        }
    }

    private static byte[] minifyJson(byte[] bytes) {
        try {
            String json = new String(bytes, StandardCharsets.UTF_8);
            return JsonParser.parseString(json).toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            return bytes;
        }
    }
}
