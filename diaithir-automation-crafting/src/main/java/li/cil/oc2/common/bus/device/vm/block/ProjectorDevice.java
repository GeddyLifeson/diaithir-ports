/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.bus.device.vm.block;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import li.cil.oc2.api.bus.device.vm.VMDevice;
import li.cil.oc2.api.bus.device.vm.VMDeviceLoadResult;
import li.cil.oc2.api.bus.device.vm.context.VMContext;
import li.cil.oc2.common.Constants;
import li.cil.oc2.common.bus.device.util.IdentityProxy;
import li.cil.oc2.common.bus.device.util.OptionalAddress;
import li.cil.oc2.common.serialization.BlobStorage;
import li.cil.oc2.common.util.NBTTagIds;
import li.cil.oc2.common.vm.device.SimpleFramebufferDevice;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.UUID;

public final class ProjectorDevice extends IdentityProxy<BlockEntity> implements VMDevice {
    private static final String ADDRESS_TAG_NAME = "address";
    private static final String BLOB_HANDLE_TAG_NAME = "blob";

    public static final int WIDTH = 640;
    public static final int HEIGHT = 480;

    ///////////////////////////////////////////////////////////////

    private final BooleanConsumer onMountedChanged;

    @Nullable private SimpleFramebufferDevice device;

    ///////////////////////////////////////////////////////////////

    private final OptionalAddress address = new OptionalAddress();
    @Nullable private UUID blobHandle;

    ///////////////////////////////////////////////////////////////

    public ProjectorDevice(final BlockEntity identity, final BooleanConsumer onMountedChanged) {
        super(identity);
        this.onMountedChanged = onMountedChanged;
    }

    ///////////////////////////////////////////////////////////////

    public boolean hasChanges() {
        final SimpleFramebufferDevice framebufferDevice = device;
        return framebufferDevice != null && framebufferDevice.hasChanges();
    }

    // TODO(oc2-restore): jcodec Picture is not available in this compile stage; applying changes is a no-op.
    public boolean applyChanges(final Object picture) {
        return false;
    }

    @Override
    public VMDeviceLoadResult mount(final VMContext context) {
        if (!allocateDevice(context)) {
            return VMDeviceLoadResult.fail();
        }

        assert device != null;
        if (!address.claim(context, device)) {
            return VMDeviceLoadResult.fail();
        }

        onMountedChanged.accept(true);

        return VMDeviceLoadResult.success();
    }

    @Override
    public void unmount() {
        final SimpleFramebufferDevice framebufferDevice = device;
        device = null;
        if (framebufferDevice != null) {
            framebufferDevice.close();
        }

        if (blobHandle != null) {
            BlobStorage.close(blobHandle);
        }

        onMountedChanged.accept(false);
    }

    @Override
    public void dispose() {
        if (blobHandle != null) {
            BlobStorage.delete(blobHandle);
            blobHandle = null;
        }

        address.clear();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        final CompoundTag tag = new CompoundTag();

        if (blobHandle != null) {
            tag.putIntArray(BLOB_HANDLE_TAG_NAME, UUIDUtil.uuidToIntArray(blobHandle));
        }
        if (address.isPresent()) {
            tag.putLong(ADDRESS_TAG_NAME, address.getAsLong());
        }

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, final CompoundTag tag) {
        final int[] blobHandleArray = tag.getIntArray(BLOB_HANDLE_TAG_NAME).orElse(null);
        if (blobHandleArray != null && blobHandleArray.length == 4) {
            blobHandle = UUIDUtil.uuidFromIntArray(blobHandleArray);
        }
        if (tag.contains(ADDRESS_TAG_NAME)) {
            address.set(tag.getLongOr(ADDRESS_TAG_NAME, 0L));
        }
    }

    ///////////////////////////////////////////////////////////////

    private boolean allocateDevice(final VMContext context) {
        if (!context.getMemoryAllocator().claimMemory(Constants.PAGE_SIZE)) {
            return false;
        }

        try {
            device = createFrameBufferDevice();
        } catch (final IOException e) {
            return false;
        }

        return true;
    }

    private SimpleFramebufferDevice createFrameBufferDevice() throws IOException {
        blobHandle = BlobStorage.validateHandle(blobHandle);
        final FileChannel channel = BlobStorage.getOrOpen(blobHandle);
        final MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, WIDTH * HEIGHT * SimpleFramebufferDevice.STRIDE);
        return new SimpleFramebufferDevice(WIDTH, HEIGHT, buffer);
    }
}
