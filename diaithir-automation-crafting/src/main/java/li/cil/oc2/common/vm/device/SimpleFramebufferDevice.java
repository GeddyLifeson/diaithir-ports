/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.vm.device;

import li.cil.sedna.api.device.MemoryMappedDevice;
import li.cil.sedna.api.memory.MemoryAccessException;
import li.cil.sedna.utils.DirectByteBufferUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

public final class SimpleFramebufferDevice implements MemoryMappedDevice {
    public static final int STRIDE = 2;

    ///////////////////////////////////////////////////////////////

    private final int width, height;
    private final ByteBuffer buffer;
    private int length;
    private final BitSet dirtyLines;

    ///////////////////////////////////////////////////////////////

    public SimpleFramebufferDevice(final int width, final int height, final ByteBuffer buffer) {
        this.width = width;
        this.height = height;
        this.length = width * height * STRIDE;

        if (buffer.capacity() < length) {
            throw new IllegalArgumentException("Buffer too small.");
        }

        this.buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.dirtyLines = new BitSet(height / 2);
        this.dirtyLines.set(0, height / 2);
    }

    ///////////////////////////////////////////////////////////////

    public void close() {
        synchronized (buffer) {
            length = 0;
            dirtyLines.clear();
            DirectByteBufferUtils.release(buffer);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean hasChanges() {
        return !dirtyLines.isEmpty();
    }

    // TODO(oc2-restore): li.cil.oc2.jcodec Picture is excluded; planes are passed as raw {Y, U, V} byte arrays.
    public boolean applyChanges(final byte[][] yuvPlanes) {
        if (dirtyLines.isEmpty()) {
            return false;
        }

        synchronized (buffer) {
            convertR5G6B5ToYUV420J(buffer, width, height, yuvPlanes);
        }

        return true;
    }

    // TODO(oc2-restore): li.cil.oc2.jcodec RgbToYuv420j is excluded; conversion is a no-op until restored.
    public static void convertR5G6B5ToYUV420J(ByteBuffer rgbBuffer, int width, int height, byte[][] yuvPlanes) {
        // Intentionally left as a no-op (see TODO above).
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public long load(final int offset, final int sizeLog2) throws MemoryAccessException {
        if (offset >= 0 && offset <= length - (1 << sizeLog2)) {
            return switch (sizeLog2) {
                case 0 -> buffer.get(offset);
                case 1 -> buffer.getShort(offset);
                case 2 -> buffer.getInt(offset);
                case 3 -> buffer.getLong(offset);
                default -> throw new IllegalArgumentException();
            };
        } else {
            return 0;
        }
    }

    @Override
    public void store(final int offset, final long value, final int sizeLog2) throws MemoryAccessException {
        if (offset >= 0 && offset <= length - (1 << sizeLog2)) {
            switch (sizeLog2) {
                case 0 -> buffer.put(offset, (byte) value);
                case 1 -> buffer.putShort(offset, (short) value);
                case 2 -> buffer.putInt(offset, (int) value);
                case 3 -> buffer.putLong(offset, value);
                default -> throw new IllegalArgumentException();
            }
            setDirty(offset);
        }
    }

    ///////////////////////////////////////////////////////////////

    private void setDirty(final int offset) {
        final int pixelY = offset / (width * STRIDE);
        dirtyLines.set(pixelY / 2);
    }
}
