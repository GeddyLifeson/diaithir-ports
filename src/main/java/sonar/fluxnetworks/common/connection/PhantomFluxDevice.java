package sonar.fluxnetworks.common.connection;

import net.minecraft.util.Util;

import net.minecraft.core.UUIDUtil;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import sonar.fluxnetworks.api.FluxConstants;
import sonar.fluxnetworks.api.FluxDataComponents;
import sonar.fluxnetworks.api.device.FluxDeviceType;
import sonar.fluxnetworks.api.device.IFluxDevice;
import sonar.fluxnetworks.common.data.FluxDeviceConfigComponent;
import sonar.fluxnetworks.common.device.TileFluxDevice;
import sonar.fluxnetworks.common.util.FluxUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * This class represents a non-entity flux device (e.g. in Network Connections tab).
 * These devices may not exist on client world, so there's no entity instance on the client.
 * They just are loaded on the server world, or used to record unloaded flux devices on server.
 * Logical operations are not allowed here.
 *
 * @see TileFluxDevice
 */
public class PhantomFluxDevice implements IFluxDevice {

    private int mNetworkID;
    private String mCustomName;
    private int mPriority;
    private long mLimit;
    private UUID mOwnerUUID;
    private FluxDeviceType mDeviceType;
    private GlobalPos mGlobalPos;
    private boolean mSurgeMode;
    private boolean mDisableLimit;
    private boolean mChunkLoaded;
    private boolean mForcedLoading;
    private long mBuffer;
    private long mChange;
    private ItemStack mDisplayStack;

    public PhantomFluxDevice() {
    }

    /**
     * Copy data from TileFluxDevice on server.
     *
     * @param device the loaded device entity
     */
    @Nonnull
    public static PhantomFluxDevice makeUnloaded(@Nonnull TileFluxDevice device) {
        PhantomFluxDevice t = new PhantomFluxDevice();
        t.mNetworkID = device.getNetworkID();
        t.mCustomName = device.getCustomName();
        t.mPriority = device.getRawPriority();
        t.mLimit = device.getRawLimit();
        t.mOwnerUUID = device.getOwnerUUID();
        t.mDeviceType = device.getDeviceType();
        t.mGlobalPos = device.getGlobalPos();
        t.mSurgeMode = device.getSurgeMode();
        t.mDisableLimit = device.getDisableLimit();
        t.mBuffer = device.getTransferBuffer();
        t.mDisplayStack = device.getDisplayStack();
        return t;
    }

    @Nonnull
    public static PhantomFluxDevice makeUpdated(@Nonnull GlobalPos pos, @Nonnull CompoundTag tag) {
        PhantomFluxDevice t = new PhantomFluxDevice();
        t.mGlobalPos = pos;
        t.readCustomTag(tag, FluxConstants.NBT_PHANTOM_UPDATE);
        return t;
    }

    @Nonnull
    public static PhantomFluxDevice make(@Nonnull CompoundTag tag) {
        PhantomFluxDevice t = new PhantomFluxDevice();
        t.readCustomTag(tag, FluxConstants.NBT_SAVE_ALL);
        return t;
    }

    @Override
    public void writeCustomTag(@Nonnull CompoundTag tag, byte type) {
        if (type == FluxConstants.NBT_SAVE_ALL || type == FluxConstants.NBT_PHANTOM_UPDATE) {
            FluxUtils.writeGlobalPos(tag, mGlobalPos);
            tag.putByte(FluxConstants.DEVICE_TYPE, mDeviceType.getId());
            tag.putInt(FluxConstants.NETWORK_ID, mNetworkID);
            tag.putString(FluxConstants.CUSTOM_NAME, mCustomName);
            tag.putInt(FluxConstants.PRIORITY, mPriority);
            tag.putLong(FluxConstants.LIMIT, mLimit);
            tag.putBoolean(FluxConstants.SURGE_MODE, mSurgeMode);
            tag.putBoolean(FluxConstants.DISABLE_LIMIT, mDisableLimit);
            tag.store(FluxConstants.PLAYER_UUID, UUIDUtil.CODEC, mOwnerUUID);
            tag.putLong(FluxConstants.ENERGY, mBuffer);

            tag.putString(FluxConstants.DEVICE_ITEM, mDisplayStack.typeHolder().getRegisteredName());
        }
    }

    @Override
    public void readCustomTag(@Nonnull CompoundTag tag, byte type) {
        if (type == FluxConstants.NBT_SAVE_ALL) {
            mGlobalPos = FluxUtils.readGlobalPos(tag);
        }
        if (type == FluxConstants.NBT_SAVE_ALL || type == FluxConstants.NBT_PHANTOM_UPDATE) {
            mDeviceType = FluxDeviceType.fromId(tag.getByteOr(FluxConstants.DEVICE_TYPE, (byte) 0));
            mNetworkID = tag.getIntOr(FluxConstants.NETWORK_ID, 0);
            mCustomName = tag.getStringOr(FluxConstants.CUSTOM_NAME, "");
            mPriority = tag.getIntOr(FluxConstants.PRIORITY, 0);
            mLimit = tag.getLongOr(FluxConstants.LIMIT, 0L);
            mSurgeMode = tag.getBooleanOr(FluxConstants.SURGE_MODE, false);
            mDisableLimit = tag.getBooleanOr(FluxConstants.DISABLE_LIMIT, false);
            mOwnerUUID = tag.read(FluxConstants.PLAYER_UUID, UUIDUtil.CODEC).orElse(Util.NIL_UUID);
            mBuffer = tag.getLongOr(FluxConstants.ENERGY, 0L);
            mDisplayStack = createDisplayItemStack(tag);
        }
        if (type == FluxConstants.NBT_PHANTOM_UPDATE) {
            mForcedLoading = tag.getBooleanOr(FluxConstants.FORCED_LOADING, false);
            mChunkLoaded = tag.getBooleanOr(FluxConstants.CHUNK_LOADED, false);
            mChange = tag.getLongOr(FluxConstants.CHANGE, 0L);
        }
    }

    /**
     * Constructs an item stack to display in the UI
     * @param tag NBT compound
     * @return display item stack
     */
    private static ItemStack createDisplayItemStack(CompoundTag tag) {
        ItemStack stack = BuiltInRegistries.ITEM.getValue(Identifier.parse(tag.getStringOr(FluxConstants.DEVICE_ITEM, ""))).getDefaultInstance();
        stack.set(FluxDataComponents.FLUX_CONFIG, FluxDeviceConfigComponent.EMPTY.withNetwork(tag.getIntOr(FluxConstants.NETWORK_ID, 0)));
        stack.set(FluxDataComponents.STORED_ENERGY, tag.getLongOr(FluxConstants.ENERGY, 0L));
        return stack;
    }

    @Override
    public int getNetworkID() {
        return mNetworkID;
    }

    @Override
    public void onPlayerOpened(@Nonnull Player player) {
        throw new IllegalStateException("Logic method cannot be invoked on phantom device");
    }

    @Override
    public void onPlayerClosed(@Nonnull Player player) {
        throw new IllegalStateException("Logic method cannot be invoked on phantom device");
    }

    @Override
    public int getRawPriority() {
        return mPriority;
    }

    @Nonnull
    @Override
    public UUID getOwnerUUID() {
        return mOwnerUUID;
    }

    @Nonnull
    @Override
    public FluxDeviceType getDeviceType() {
        return mDeviceType;
    }

    @Override
    public boolean isChunkLoaded() {
        return mChunkLoaded;
    }

    @Override
    public boolean isForcedLoading() {
        return mForcedLoading;
    }

    @Override
    public long getRawLimit() {
        return mLimit;
    }

    @Override
    public long getMaxTransferLimit() {
        throw new IllegalStateException("Logic method cannot be invoked on phantom device");
    }

    @Nonnull
    @Override
    public GlobalPos getGlobalPos() {
        return mGlobalPos;
    }

    @Nonnull
    @Override
    public String getCustomName() {
        return mCustomName;
    }

    @Override
    public boolean getDisableLimit() {
        return mDisableLimit;
    }

    @Override
    public boolean getSurgeMode() {
        return mSurgeMode;
    }

    @Nonnull
    @Override
    public ItemStack getDisplayStack() {
        return mDisplayStack;
    }

    @Override
    public long getTransferBuffer() {
        return mBuffer;
    }

    @Override
    public long getTransferChange() {
        return mChange;
    }

    @Nullable
    @Override
    public FluxMenu createMenu(int containerId, @Nonnull Inventory inventory, @Nonnull Player player) {
        throw new IllegalStateException("Logic method cannot be invoked on phantom device");
    }
}
