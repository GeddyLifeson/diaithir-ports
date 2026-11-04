package net.diaithir.crashtools.simplebackups.sbk;

import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Converts MCA region files to/from the MCAP intermediate format used in SBK archives.
 *
 * <p>MCA format: 4096-byte location table (1024 × u32 BE), 4096-byte timestamp table,
 * then variable-length chunk sectors. Each occupied chunk sector: 4-byte BE length +
 * 1-byte compression type + compressed NBT bytes.
 *
 * <p>MCAP format:
 * <pre>
 *   magic        4 bytes   {0x4D, 0x43, 0x41, 0x50}
 *   chunk_count  u16 LE
 *   For each chunk in ascending Hilbert order:
 *     local_x    u8
 *     local_z    u8
 *     raw_len    u32 LE    decompressed NBT byte count
 *     nbt_data   ...       raw (decompressed) NBT bytes
 * </pre>
 */
public final class McapProcessor {

    public static final byte[] MAGIC = {0x4D, 0x43, 0x41, 0x50};
    private static final int SECTOR_SIZE = 4096;

    private McapProcessor() {}

    /**
     * Convert MCA bytes to MCAP bytes.
     * Reads all occupied chunks, decompresses them, sorts by Hilbert index, writes MCAP.
     */
    public static byte[] toMcap(byte[] mcaBytes) throws IOException {
        if (mcaBytes.length < SECTOR_SIZE * 2) {
            // Empty or too small region file
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(6);
            outputStream.write(MAGIC);
            outputStream.write(0); // chunk_count lo
            outputStream.write(0); // chunk_count hi

            return outputStream.toByteArray();
        }

        // Read location table: 1024 u32 BE entries (z-major, x-minor)
        DataInputStream headerStream = new DataInputStream(new ByteArrayInputStream(mcaBytes, 0, SECTOR_SIZE));

        record ChunkLoc(int x, int z, int sectorOffset, int sectorCount) {}
        List<ChunkLoc> occupied = new ArrayList<>();
        for (int z = 0; z < 32; z++) {
            for (int x = 0; x < 32; x++) {
                int loc = headerStream.readInt(); // BE
                int sectorOffset = (loc >>> 8) & 0xFFFFFF;
                int sectorCount = loc & 0xFF;
                if (sectorOffset != 0 && sectorCount != 0) {
                    occupied.add(new ChunkLoc(x, z, sectorOffset, sectorCount));
                }
            }
        }

        // Sort by Hilbert index
        occupied.sort(Comparator.comparingInt(c -> HilbertCurve.index(c.x(), c.z())));

        // Decompress each chunk to raw NBT
        record ChunkEntry(int x, int z, byte[] rawNbt) {}
        List<ChunkEntry> entries = new ArrayList<>(occupied.size());
        for (ChunkLoc loc : occupied) {
            int byteOffset = loc.sectorOffset() * SECTOR_SIZE;
            int available = mcaBytes.length - byteOffset;
            if (available < 5) continue; // need at least 4-byte length + 1-byte type
            DataInputStream chunkStream = new DataInputStream(
                    new ByteArrayInputStream(mcaBytes, byteOffset, Math.min(loc.sectorCount() * SECTOR_SIZE, available)));
            int length = chunkStream.readInt(); // BE
            if (length <= 0) continue;
            int compressionType = chunkStream.readUnsignedByte();
            byte[] compressedNbt = chunkStream.readNBytes(length - 1);
            byte[] rawNbt = McapProcessor.decompress(compressionType, compressedNbt);

            entries.add(new ChunkEntry(loc.x(), loc.z(), rawNbt));
        }

        // Write MCAP
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(MAGIC);
        int count = entries.size();
        outputStream.write(count & 0xFF); // chunk_count u16 LE
        outputStream.write((count >>> 8) & 0xFF);
        for (ChunkEntry entry : entries) {
            outputStream.write(entry.x());
            outputStream.write(entry.z());
            int rawLen = entry.rawNbt().length;
            outputStream.write(rawLen & 0xFF); // raw_len u32 LE
            outputStream.write((rawLen >>> 8) & 0xFF);
            outputStream.write((rawLen >>> 16) & 0xFF);
            outputStream.write((rawLen >>> 24) & 0xFF);
            outputStream.write(entry.rawNbt());
        }

        return outputStream.toByteArray();
    }

    /**
     * Convert MCAP bytes back to MCA bytes.
     * Re-compresses each chunk with zlib level 6 (type=2), rebuilds 8 KiB header.
     */
    public static byte[] fromMcap(byte[] mcapBytes) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(mcapBytes));

        // Validate magic
        byte[] magic = in.readNBytes(4);
        if (!Arrays.equals(magic, MAGIC)) {
            throw new SbkException("Invalid MCAP magic");
        }

        int countLo = in.readUnsignedByte();
        int countHi = in.readUnsignedByte();
        int chunkCount = countLo | (countHi << 8);

        record ChunkData(int x, int z, byte[] rawNbt) {}
        List<ChunkData> chunks = new ArrayList<>(chunkCount);
        for (int i = 0; i < chunkCount; i++) {
            int x = in.readUnsignedByte();
            int z = in.readUnsignedByte();
            int b0 = in.readUnsignedByte();
            int b1 = in.readUnsignedByte();
            int b2 = in.readUnsignedByte();
            int b3 = in.readUnsignedByte();
            int rawLen = b0 | (b1 << 8) | (b2 << 16) | (b3 << 24);
            byte[] rawNbt = in.readNBytes(rawLen);

            chunks.add(new ChunkData(x, z, rawNbt));
        }

        // Re-compress each chunk with zlib level 6
        record CompressedChunk(int x, int z, byte[] zlibData) {}
        List<CompressedChunk> compressed = new ArrayList<>(chunks.size());
        for (ChunkData chunk : chunks) {
            byte[] zlibData = McapProcessor.zlibCompress(chunk.rawNbt(), 6);
            compressed.add(new CompressedChunk(chunk.x(), chunk.z(), zlibData));
        }

        // Build MCA: location table (4096 bytes) + timestamp table (4096 bytes, zeroed) + chunk sectors
        // Offsets start at sector 2 (after the two header sectors)
        int[] locationTable = new int[1024]; // indexed by z*32+x
        int currentSector = 2; // sectors 0 and 1 are the header sectors

        ByteArrayOutputStream chunkDataOutputStream = new ByteArrayOutputStream();
        DataOutputStream chunkDataOut = new DataOutputStream(chunkDataOutputStream);

        for (CompressedChunk cc : compressed) {
            int idx = cc.z() * 32 + cc.x();
            byte[] zlibData = cc.zlibData();
            // chunk data: length(4 BE) + compression_type(1) + compressed_bytes
            int dataLength = 1 + zlibData.length; // compression_type + data
            int totalBytes = 4 + dataLength;
            int sectorCount = (totalBytes + SECTOR_SIZE - 1) / SECTOR_SIZE;

            // write chunk: 4-byte BE length + 1-byte type + data + padding to sector boundary
            chunkDataOut.writeInt(dataLength); // BE length (includes type byte)
            chunkDataOut.writeByte(2); // zlib
            chunkDataOut.write(zlibData);
            // pad to sector boundary
            int written = 4 + 1 + zlibData.length;
            int padded = sectorCount * SECTOR_SIZE;
            for (int p = written; p < padded; p++) chunkDataOut.writeByte(0);

            locationTable[idx] = ((currentSector & 0xFFFFFF) << 8) | (sectorCount & 0xFF);
            currentSector += sectorCount;
        }

        chunkDataOut.flush();
        byte[] chunkData = chunkDataOutputStream.toByteArray();

        ByteArrayOutputStream mcaOutputStream = new ByteArrayOutputStream(SECTOR_SIZE * 2 + chunkData.length);
        DataOutputStream mcaOut = new DataOutputStream(mcaOutputStream);
        for (int i = 0; i < 1024; i++) {
            mcaOut.writeInt(locationTable[i]); // location table (1024 × u32 BE)
        }

        for (int i = 0; i < 1024; i++) {
            mcaOut.writeInt(0); // timestamp table (zeroed)
        }

        mcaOut.write(chunkData);
        mcaOut.flush();

        return mcaOutputStream.toByteArray();
    }

    private static byte[] decompress(int compressionType, byte[] data) throws IOException {
        return switch(compressionType) {
            case 1 -> { // gzip
                try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data))) {
                    yield gzip.readAllBytes();
                }
            }
            case 2 -> { // zlib
                try (InflaterInputStream inflater = new InflaterInputStream(new ByteArrayInputStream(data))) {
                    yield inflater.readAllBytes();
                }
            }
            case 3 -> data; // uncompressed
            case 4 -> { // zstd
                try (ZstdCompressorInputStream zstd = new ZstdCompressorInputStream(new ByteArrayInputStream(data))) {
                    yield zstd.readAllBytes();
                }
            }
            default -> throw new SbkException("Unknown MCA compression type: " + compressionType);
        };
    }

    private static byte[] zlibCompress(byte[] data, int level) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(level);
        try (DeflaterOutputStream dos = new DeflaterOutputStream(outputStream, deflater)) {
            dos.write(data);
        } finally {
            deflater.end();
        }

        return outputStream.toByteArray();
    }
}
