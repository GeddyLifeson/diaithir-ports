/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.blockentity;

import li.cil.oc2.api.API;
import li.cil.oc2.common.block.Blocks;
import li.cil.oc2.common.config.Config;
import li.cil.oc2.common.block.ProjectorBlock;
import li.cil.oc2.common.bus.device.vm.block.ProjectorDevice;
import li.cil.oc2.common.capabilities.Capabilities;
import li.cil.oc2.common.energy.FixedEnergyStorage;
import li.cil.oc2.common.network.Network;
import li.cil.oc2.common.network.ProjectorLoadBalancer;
import li.cil.oc2.common.network.message.ProjectorRequestFramebufferMessage;
import li.cil.oc2.common.network.message.ProjectorStateMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

@EventBusSubscriber(modid = API.MOD_ID)
public final class ProjectorBlockEntity extends ModBlockEntity implements TickableBlockEntity {
    @FunctionalInterface
    public interface FrameConsumer {
        // TODO(oc2-restore): Picture parameter removed since the jcodec package is excluded.
        void processFrame();
    }

    ///////////////////////////////////////////////////////////////

    public static final int MAX_RENDER_DISTANCE = 16;
    public static final int MAX_GOOD_RENDER_DISTANCE = 12;
    public static final int MAX_WIDTH = MAX_GOOD_RENDER_DISTANCE + 1; // +1 To make it odd, so we can center.
    public static final int MAX_HEIGHT = (MAX_GOOD_RENDER_DISTANCE * ProjectorDevice.HEIGHT / ProjectorDevice.WIDTH) + 1; // + 1 To match horizontal margin.

    private static final String ENERGY_TAG_NAME = "energy";
    private static final String IS_PROJECTING_TAG_NAME = "projecting";
    private static final String HAS_ENERGY_TAG_NAME = "has_energy";

    private static final ExecutorService DECODER_WORKERS = Executors.newCachedThreadPool(r -> {
        final Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("Projector Frame Decoder");
        return thread;
    });

    ///////////////////////////////////////////////////////////////

    private final ProjectorDevice projectorDevice = new ProjectorDevice(this, this::handleMountedChanged);
    private boolean isMounted, hasEnergy;
    private final FixedEnergyStorage energy = new FixedEnergyStorage(Config.projectorEnergyStorage);
    // TODO(oc2-restore): Picture removed (jcodec excluded); this object only serves as the frame lock.
    private final Object picture = new Object();

    // Video encoding.
    // TODO(oc2-restore): H264Encoder removed (jcodec excluded).
    private boolean needsIDR; // Whether we need to send a keyframe next.

    // Video decoding.
    // TODO(oc2-restore): H264Decoder removed (jcodec excluded).
    @Nullable private CompletableFuture<?> runningDecode; // Current decoding operation, if any, to avoid race conditions.
    private final ByteBuffer decoderBuffer = ByteBuffer.allocateDirect(1024 * 1024); // Re-used decompression buffer.
    @Nullable private FrameConsumer frameConsumer; // Where to throw received frames.

    private AABB renderBounds; // Maximum possible render bounds, assuming we project on furthest away surface.
    private long lastKeepAliveSentAt;

    ///////////////////////////////////////////////////////////////

    public ProjectorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.PROJECTOR.get(), pos, state);

        // TODO(oc2-restore): encoder.setKeyInterval(100);

        updateRenderBounds();
    }

    ///////////////////////////////////////////////////////////////

    public boolean isProjecting() {
        if (!isMounted || level == null) {
            return false;
        }

        final Direction facing = getBlockState().getValue(ProjectorBlock.FACING);
        final BlockPos neighborPos = getBlockPos().relative(facing);
        final int neighborChunkX = SectionPos.blockToSectionCoord(neighborPos.getX());
        final int neighborChunkZ = SectionPos.blockToSectionCoord(neighborPos.getZ());
        if (!level.hasChunk(neighborChunkX, neighborChunkZ)) {
            return false;
        }

        final BlockState neighborBlockState = level.getBlockState(neighborPos);
        return !neighborBlockState.isSolidRender();
    }

    public boolean hasEnergy() {
        return hasEnergy;
    }

    public void setRequiresKeyframe() {
        needsIDR = true;
    }

    public void setFrameConsumer(@Nullable final FrameConsumer consumer) {
        if (consumer == frameConsumer) {
            return;
        }
        synchronized (picture) {
            this.frameConsumer = consumer;
            if (frameConsumer != null) {
                frameConsumer.processFrame();
            }
        }
    }

    public void onRendering() {
        final long now = System.currentTimeMillis();
        if (now - lastKeepAliveSentAt > 1000) {
            lastKeepAliveSentAt = now;
            Network.sendToServer(new ProjectorRequestFramebufferMessage(this));
        }
    }

    @Override
    public void serverTick() {
        if (!isMounted) {
            return;
        }

        final boolean isPowered;
        if (Config.projectorsUseEnergy()) {
            isPowered = energy.extractEnergy(Config.projectorEnergyPerTick, true) >= Config.projectorEnergyPerTick;
            if (isPowered) {
                energy.extractEnergy(Config.projectorEnergyPerTick, false);
            }
        } else {
            isPowered = true;
        }

        updateProjectorState(isMounted, isPowered);

        if (!hasEnergy || (!projectorDevice.hasChanges() && !needsIDR)) {
            return;
        }

        ProjectorLoadBalancer.offerFrame(this, this::encodeFrame);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        final CompoundTag tag = super.getUpdateTag(registries);

        tag.putBoolean(IS_PROJECTING_TAG_NAME, isMounted);
        tag.putBoolean(HAS_ENERGY_TAG_NAME, hasEnergy);

        return tag;
    }

    @Override
    public void handleUpdateTag(final ValueInput input) {
        super.handleUpdateTag(input);

        isMounted = input.getBooleanOr(IS_PROJECTING_TAG_NAME, false);
        hasEnergy = input.getBooleanOr(HAS_ENERGY_TAG_NAME, false);
    }

    @Override
    protected void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);

        energy.serialize(output.child(ENERGY_TAG_NAME));
    }

    @Override
    public void loadAdditional(final ValueInput input) {
        super.loadAdditional(input);

        energy.deserialize(input.childOrEmpty(ENERGY_TAG_NAME));
    }

    public AABB getRenderBoundingBox() {
        return renderBounds;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setBlockState(final BlockState state) {
        super.setBlockState(state);

        updateRenderBounds();
    }

    public void applyProjectorStateClient(final boolean isProjecting, final boolean hasEnergy) {
        if (level == null || !level.isClientSide()) {
            return;
        }

        this.isMounted = isProjecting;
        this.hasEnergy = hasEnergy;
    }

    public void applyNextFrameClient(final ByteBuffer frameData) {
        if (level == null || !level.isClientSide()) {
            return;
        }

        final CompletableFuture<?> lastDecode = runningDecode;
        runningDecode = CompletableFuture.runAsync(() -> {
            try {
                try {
                    if (lastDecode != null) lastDecode.join();
                } catch (final CompletionException ignored) {
                }

                final Inflater inflater = new Inflater();
                inflater.setInput(frameData);

                decoderBuffer.clear();
                inflater.inflate(decoderBuffer);
                decoderBuffer.flip();

                // TODO(oc2-restore): decoder.decodeFrame(decoderBuffer, picture.getData());

                synchronized (picture) {
                    if (frameConsumer != null) {
                        frameConsumer.processFrame();
                    }
                }
            } catch (final DataFormatException ignored) {
            }
        }, DECODER_WORKERS);
    }

    ///////////////////////////////////////////////////////////////

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        if (Config.projectorsUseEnergy()) {
            event.registerBlock(
                Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> {
                    if (be instanceof final ProjectorBlockEntity self) {
                        return self.energy;
                    }
                    return null;
                },
                Blocks.PROJECTOR.get()
            );
        }
        event.registerBlock(
            Capabilities.Device.BLOCK,
            (level, pos, state, be, side) -> {
                if (be instanceof final ProjectorBlockEntity self) {
                    if (side == self.getBlockState().getValue(ProjectorBlock.FACING).getOpposite()) {
                        return self.projectorDevice;
                    }
                }
                return null;
            },
            Blocks.PROJECTOR.get()
        );
    }

    ///////////////////////////////////////////////////////////////

    private void handleMountedChanged(final boolean value) {
        updateProjectorState(value, hasEnergy);
    }

    private void updateProjectorState(final boolean isMounted, final boolean hasEnergy) {
        if ((isMounted == this.isMounted && hasEnergy == this.hasEnergy) || !isValid()) {
            return;
        }

        // We may get called from unmount() of our device, which can be triggered due to chunk unload.
        // Hence, we need to check the loaded state here, lest we ghost load the chunk, breaking everything.
        if (level != null && !level.isClientSide() && level.isLoaded(getBlockPos())) {
            if (this.isMounted && !isMounted) {
                // TODO(oc2-restore): clear picture planes (Arrays.fill on plane data 0, 1, 2).
            }

            this.isMounted = isMounted;
            this.hasEnergy = hasEnergy;

            level.setBlock(getBlockPos(), getBlockState().setValue(ProjectorBlock.LIT, isMounted), Block.UPDATE_CLIENTS);

            Network.sendToClientsTrackingBlockEntity(new ProjectorStateMessage(this, isMounted, hasEnergy), this);
        }
    }

    @Nullable
    private ByteBuffer encodeFrame() {
        // TODO(oc2-restore): Frame encoding requires the jcodec H264 encoder and Picture (applyChanges, encodeIDRFrame, encodeFrame, deflate).
        return null;
    }

    private void updateRenderBounds() {
        final Direction blockFacing = getBlockState().getValue(ProjectorBlock.FACING);
        final Direction canvasUp = Direction.UP;
        final Direction canvasLeft = blockFacing.getCounterClockWise();

        final BlockPos projectorPos = getBlockPos();
        final BlockPos screenBasePos = projectorPos.relative(blockFacing, MAX_RENDER_DISTANCE);
        final BlockPos screenMinPos = screenBasePos.relative(canvasLeft.getOpposite(), MAX_WIDTH / 2);
        final BlockPos screenMaxPos = screenBasePos.relative(canvasLeft, MAX_WIDTH / 2)
            // -1 for the MAX_HEIGHT padding, -1 for auto-expansion of AABB constructor
            .relative(canvasUp, MAX_HEIGHT - 2);

        renderBounds = new AABB(getBlockPos()).minmax(new AABB(screenMinPos)).minmax(new AABB(screenMaxPos));
    }
}
