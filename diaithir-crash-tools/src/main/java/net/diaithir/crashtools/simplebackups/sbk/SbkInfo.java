package net.diaithir.crashtools.simplebackups.sbk;

import javax.annotation.Nonnull;
import java.util.List;

public record SbkInfo(int formatVersion, long fileCount, long frameSizeBytes,
                      int[] groupFrames, long[] groupSizes, List<SbkIndexEntry> entries) {

    @Nonnull
    @Override
    public String toString() {
        return "Format: " + this.formatVersion
                + ", Files: " + this.fileCount
                + ", FrameSize: " + this.frameSizeBytes
                + ", Groups: " + this.groupFrames.length
                + "," + this.groupSizes.length;
    }
}
