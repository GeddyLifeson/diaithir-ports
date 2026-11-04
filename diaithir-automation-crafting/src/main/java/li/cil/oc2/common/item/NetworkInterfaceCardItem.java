/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.item;

import li.cil.oc2.common.Constants;
import li.cil.oc2.common.util.ItemStackUtils;
import li.cil.oc2.common.util.NBTTagIds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.Consumer;

import static li.cil.oc2.common.util.TextFormatUtils.withFormat;
import static li.cil.oc2.common.util.TranslationUtils.text;

public final class NetworkInterfaceCardItem extends ModItem {
    private static final String SIDE_CONFIGURATION_TAG_NAME = "sides";
    private static final Component IS_CONFIGURED_TEXT = withFormat(text("item.{mod}.network_interface_card.is_configured"), ChatFormatting.GREEN);

    ///////////////////////////////////////////////////////////////////

    public static void setSideConfiguration(final ItemStack stack, final Direction side, final boolean enabled) {
        final int index = side.get3DDataValue();

        CustomData.update(DataComponents.CUSTOM_DATA, stack, (nbt) -> {
            final CompoundTag tag = ItemStackUtils.getOrCreateModDataTag(nbt);
            final byte[] values;
            if (tag.contains(SIDE_CONFIGURATION_TAG_NAME) &&
                tag.getByteArray(SIDE_CONFIGURATION_TAG_NAME).orElse(new byte[0]).length == Constants.BLOCK_FACE_COUNT) {
                values = tag.getByteArray(SIDE_CONFIGURATION_TAG_NAME).orElse(new byte[0]);
            } else {
                values = new byte[Constants.BLOCK_FACE_COUNT];
                Arrays.fill(values, (byte) 1);
            }

            values[index] = (byte) (enabled ? 1 : 0);

            tag.putByteArray(SIDE_CONFIGURATION_TAG_NAME, values);
        });
    }

    public static boolean getSideConfiguration(final ItemStack stack, @Nullable final Direction side) {
        if (side == null) {
            return false;
        }

        final int index = side.get3DDataValue();

        final CompoundTag tag = ItemStackUtils.getModDataTag(stack);
        if (tag.contains(SIDE_CONFIGURATION_TAG_NAME)) {
            final byte[] values = tag.getByteArray(SIDE_CONFIGURATION_TAG_NAME).orElse(new byte[0]);
            if (index < values.length) {
                return values[index] != 0;
            }
        }

        return true;
    }

    public static boolean hasConfiguration(final ItemStack stack) {
        final byte[] values = ItemStackUtils.getModDataTag(stack).getByteArray(SIDE_CONFIGURATION_TAG_NAME).orElse(new byte[0]);
        for (final byte value : values) {
            if (value == 0) {
                return true;
            }
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////


    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final TooltipDisplay display, final Consumer<Component> components, final TooltipFlag flag) {
        super.appendHoverText(stack, context, display, components, flag);
        if (NetworkInterfaceCardItem.hasConfiguration(stack)) {
            components.accept(IS_CONFIGURED_TEXT);
        }
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack itemStack = player.getItemInHand(hand);

        if (player.level().isClientSide()) {
            if (itemStack.is(Items.NETWORK_INTERFACE_CARD.get())) {
                openConfigurationScreen(player, hand);
            }
        }

        return player.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    ///////////////////////////////////////////////////////////////////

    @OnlyIn(Dist.CLIENT)
    private void openConfigurationScreen(final Player player, final InteractionHand hand) {
        // TODO(oc2-restore): open NetworkInterfaceCardScreen(player, hand) once li.cil.oc2.client is compiled again.
    }
}
