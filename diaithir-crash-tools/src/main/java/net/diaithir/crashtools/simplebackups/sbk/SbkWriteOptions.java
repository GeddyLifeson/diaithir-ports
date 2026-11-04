package net.diaithir.crashtools.simplebackups.sbk;

import net.diaithir.crashtools.simplebackups.ToolsLoader;

/**
 * Compression parameters for {@link SbkWriter}.
 *
 * <p>Build with {@link #builder()}:
 * <pre>{@code
 * SbkWriteOptions opts = SbkWriteOptions.builder()
 *     .lzmaPreset(6)
 *     .algorithm(SbkAlgorithm.LZMA2)
 *     .build();
 * }</pre>
 */
public final class SbkWriteOptions {

    /**
     * Default LZMA2 preset / base compression level when none is specified.
     */
    public static final int DEFAULT_PRESET = 3;

    /**
     * LZMA2 preset (0–9) or base compression level passed to the selected algorithm.
     * For zstd this is mapped to zstd levels 1–19 by {@link SbkFrameCompressor}.
     */
    public final int lzmaPreset;

    /**
     * Absolute timestamp filter for direct callers that do not pre-filter files.
     * Files with {@code mtime < sinceMs} are excluded. {@code 0} means no filter.
     * Not used by {@link net.diaithir.crashtools.simplebackups.compression.SbkCompression}.
     */
    public final long sinceMs;

    /**
     * Preferred compression algorithm.
     * If {@link SbkAlgorithm#ZSTD} is requested but zstd-jni is unavailable,
     * {@link SbkWriter} falls back to {@link SbkAlgorithm#LZMA2} with a warning.
     */
    public final SbkAlgorithm preferredAlgorithm;

    private SbkWriteOptions(int lzmaPreset, long sinceMs, SbkAlgorithm preferredAlgorithm) {
        this.lzmaPreset = lzmaPreset;
        this.sinceMs = sinceMs;
        this.preferredAlgorithm = preferredAlgorithm;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int lzmaPreset = SbkWriteOptions.DEFAULT_PRESET;
        private long sinceMs = 0L;
        private SbkAlgorithm preferredAlgorithm = ToolsLoader.isLzmaAvailable() ? SbkAlgorithm.LZMA2 : SbkAlgorithm.ZSTD;

        public Builder lzmaPreset(int preset) {
            this.lzmaPreset = preset;

            return this;
        }

        public Builder sinceMs(long ms) {
            this.sinceMs = ms;

            return this;
        }

        public Builder algorithm(SbkAlgorithm algorithm) {
            this.preferredAlgorithm = algorithm;

            return this;
        }

        public SbkWriteOptions build() {
            return new SbkWriteOptions(this.lzmaPreset, this.sinceMs, this.preferredAlgorithm);
        }
    }
}
