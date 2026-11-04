/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.item;

import li.cil.oc2.api.bus.device.data.Firmware;
import li.cil.oc2.common.bus.device.data.FirmwareRegistry;
import li.cil.oc2.common.util.ItemStackUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;

public final class FlashMemoryWithExternalDataItem extends ModItem {
    public static final String FIRMWARE_TAG_NAME = "firmware";

    ///////////////////////////////////////////////////////////////////

    private final Identifier defaultData;
    @Nullable private String descriptionId;

    ///////////////////////////////////////////////////////////////////

    public FlashMemoryWithExternalDataItem(final Identifier defaultData) {
        super(createProperties().stacksTo(1));
        this.defaultData = defaultData;
    }

    ///////////////////////////////////////////////////////////////////

    @Nullable
    public Firmware getFirmware(final ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != this) {
            return null;
        }

        final String registryName = ItemStackUtils.getModDataTag(stack).getStringOr(FIRMWARE_TAG_NAME, "");

        Identifier location = defaultData;
        if (!StringUtil.isNullOrEmpty(registryName)) {
            final Identifier parsed = Identifier.tryParse(registryName);
            if (parsed != null) {
                location = parsed;
            }
        }

        return FirmwareRegistry.getValue(location);
    }

    public ItemStack withFirmware(final ItemStack stack, final Firmware firmware) {
        if (stack.isEmpty() || stack.getItem() != this) {
            return ItemStack.EMPTY;
        }

        final Identifier key = FirmwareRegistry.getKey(firmware);

        CustomData.update(DataComponents.CUSTOM_DATA, stack, (nbt) -> {
            ItemStackUtils.getOrCreateModDataTag(nbt).putString(FIRMWARE_TAG_NAME, key.toString());
        });

        return stack;
    }

    @SuppressWarnings("unused")
    public ItemStack withFirmware(final Firmware firmware) {
        return withFirmware(new ItemStack(this), firmware);
    }

    @Override
    public Component getName(final ItemStack stack) {
        final Firmware firmware = getFirmware(stack);
        final Component baseName = Component.translatable(getOrCreateDescriptionId());
        if (firmware != null) {
            return Component.literal("")
                .append(baseName)
                .append(" (")
                .append(firmware.getDisplayName())
                .append(")");
        } else {
            return baseName;
        }
    }

    ///////////////////////////////////////////////////////////////////

    private String getOrCreateDescriptionId() {
        if (descriptionId == null) {
            descriptionId = Util.makeDescriptionId("item", Items.FLASH_MEMORY.getId());
        }
        return descriptionId;
    }
}
