/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.blockentity;

import li.cil.oc2.api.API;
import li.cil.oc2.common.Constants;
import li.cil.oc2.common.block.Blocks;
import li.cil.oc2.common.block.FlashMemoryFlasherBlock;
import li.cil.oc2.common.bus.device.vm.block.FlashMemoryFlasherContainer;
import li.cil.oc2.common.bus.device.vm.block.FlashMemoryFlasherDevice;
import li.cil.oc2.common.capabilities.Capabilities;
import li.cil.oc2.common.container.TypedItemStackHandler;
import li.cil.oc2.common.network.Network;
import li.cil.oc2.common.network.message.FirmwareFlasherMessage;
import li.cil.oc2.common.tags.ItemTags;
import li.cil.oc2.common.util.ItemStackUtils;
import li.cil.oc2.common.util.LocationSupplierUtils;
import li.cil.oc2.common.util.SoundEvents;
import li.cil.oc2.common.util.ThrottledSoundEmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;

import static li.cil.oc2.common.item.AbstractBlockDeviceItem.DATA_TAG_NAME;

@EventBusSubscriber(modid = API.MOD_ID)
public final class FlashMemoryFlasherBlockEntity extends ModBlockEntity implements FlashMemoryFlasherContainer {
    private final FlashMemoryItemStackHandler itemHandler = new FlashMemoryItemStackHandler();
    private final FlashMemoryFlasherDevice<FlashMemoryFlasherBlockEntity> device = new FlashMemoryFlasherDevice<>(this);
    private final ThrottledSoundEmitter insertSoundEmitter;
    private final ThrottledSoundEmitter ejectSoundEmitter;

    ///////////////////////////////////////////////////////////////////

    public FlashMemoryFlasherBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.FLASH_MEMORY_FLASHER.get(), pos, state);

        this.insertSoundEmitter = new ThrottledSoundEmitter(LocationSupplierUtils.of(this),
            SoundEvents.FLOPPY_INSERT.get()).withMinInterval(Duration.ofMillis(100));
        this.ejectSoundEmitter = new ThrottledSoundEmitter(LocationSupplierUtils.of(this),
            SoundEvents.FLOPPY_EJECT.get()).withMinInterval(Duration.ofMillis(100));
    }

    ///////////////////////////////////////////////////////////////////

    public boolean canInsert(final ItemStack stack) {
        return !stack.isEmpty() && stack.is(ItemTags.DEVICES_FLASH_MEMORY);
    }

    public ItemStack insert(final ItemStack stack, @Nullable final Player player) {
        if (!canInsert(stack)) {
            return stack;
        }

        eject(player);

        insertSoundEmitter.play();
        return itemHandler.insertItem(0, stack, false);
    }

    public boolean canEject() {
        return !itemHandler.extractItem(0, 1, true).isEmpty();
    }

    public void eject(@Nullable final Player player) {
        if (level == null) {
            return;
        }

        final ItemStack stack = itemHandler.extractItem(0, 1, false);
        if (!stack.isEmpty()) {
            final Direction facing = getBlockState().getValue(FlashMemoryFlasherBlock.FACING);
            ejectSoundEmitter.play();
            ItemStackUtils.spawnAsEntity(level, getBlockPos().relative(facing), stack, facing).ifPresent(entity -> {
                if (player != null) {
                    entity.setNoPickUpDelay();
                    entity.setThrower(player);
                }
            });
        }
    }

    public ItemStack getFloppy() {
        return itemHandler.getStackInSlot(0);
    }

    @OnlyIn(Dist.CLIENT)
    public void setFlashMemory(final ItemStack stack) {
        itemHandler.setStackInSlot(0, stack);
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(
            Capabilities.ItemHandler.BLOCK,
            (level, pos, state, be, side) -> {
                if (be instanceof final FlashMemoryFlasherBlockEntity self) {
                    return self.itemHandler;
                }
                return null;
            },
            Blocks.FLASH_MEMORY_FLASHER.get()
        );
        event.registerBlock(
            Capabilities.Device.BLOCK,
            (level, pos, state, be, side) -> {
                if (be instanceof final FlashMemoryFlasherBlockEntity self) {
                    if (side == self.getBlockState().getValue(FlashMemoryFlasherBlock.FACING).getOpposite()) {
                        return self.device;
                    }
                }
                return null;
            },
            Blocks.FLASH_MEMORY_FLASHER.get()
        );
    }

    // The update tag (getUpdateTag/handleUpdateTag) is built from saveAdditional/loadAdditional by default.

    @Override
    protected void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);

        itemHandler.serialize(output.child(Constants.ITEMS_TAG_NAME));
    }

    @Override
    protected void loadAdditional(final ValueInput input) {
        super.loadAdditional(input);

        itemHandler.deserialize(input.childOrEmpty(Constants.ITEMS_TAG_NAME));
    }

    @Override
    public ItemStack getDiskItemStack() {
        return itemHandler.getStackInSlotRaw(0);
    }

    @Override
    public void handleDataAccess() {
        // DO NOTHING
    }

    ///////////////////////////////////////////////////////////////////

    private final class FlashMemoryItemStackHandler extends TypedItemStackHandler {
        public FlashMemoryItemStackHandler() {
            super(1, ItemTags.DEVICES_FLASH_MEMORY);
        }

        public ItemStack getStackInSlotRaw(final int slot) {
            return super.getStackInSlot(slot);
        }

        @Override
        @Nonnull
        public ItemStack getStackInSlot(final int slot) {
            final ItemStack stack = getStackInSlotRaw(slot);
            exportDeviceDataToItemStack(stack);
            return stack;
        }

        @Override
        @Nonnull
        public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
            if (slot == 0 && !simulate && amount > 0) {
                exportDeviceDataToItemStack(getStackInSlotRaw(0));
            }

            return super.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(final int slot) {
            return 1;
        }

        @Override
        public void serialize(final ValueOutput output) {
            exportDeviceDataToItemStack(getStackInSlotRaw(0));
            super.serialize(output);
        }

        @Override
        protected void onContentsChanged(final int slot) {
            super.onContentsChanged(slot);

            if (level == null || level.isClientSide()) {
                return;
            }

            final ItemStack stack = getStackInSlotRaw(slot);
            if (stack.isEmpty()) {
                device.removeBlockDevice();
            } else {
                CustomData.update(DataComponents.CUSTOM_DATA, stack, (nbt) -> {
                    final CompoundTag tag = ItemStackUtils.getOrCreateModDataTag(nbt).getCompoundOrEmpty(DATA_TAG_NAME);
                    device.updateBlockDevice(tag);
                });
            }

            Network.sendToClientsTrackingBlockEntity(new FirmwareFlasherMessage(FlashMemoryFlasherBlockEntity.this), FlashMemoryFlasherBlockEntity.this);

            setChanged();
        }

        private void exportDeviceDataToItemStack(final ItemStack stack) {
            if (stack.isEmpty()) {
                return;
            }

            if (level == null || level.isClientSide()) {
                return;
            }

            final CompoundTag tag = new CompoundTag();
            device.exportToItemStack(tag);
            CustomData.update(DataComponents.CUSTOM_DATA, stack, (nbt) -> {
                ItemStackUtils.getOrCreateModDataTag(nbt).put(DATA_TAG_NAME, tag);
            });
        }
    }
}
