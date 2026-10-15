/*
 * This file ("ItemBattery.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items;

import net.minecraft.world.entity.EquipmentSlot;

import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.item.Item;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import de.ellpeck.actuallyadditions.mod.util.EnergyCaps;

import de.ellpeck.actuallyadditions.mod.items.base.ItemEnergy;
import de.ellpeck.actuallyadditions.mod.util.ItemUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public class ItemBattery extends ItemEnergy {

    public ItemBattery(int capacity, int transfer) {
        super(capacity, transfer);
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return ItemUtil.isEnabled(stack);
    }

    @Override
    public void inventoryTick(@Nonnull ItemStack stack, ServerLevel world, @Nonnull Entity entity, EquipmentSlot slot) {
        if (!world.isClientSide() && entity instanceof Player player && ItemUtil.isEnabled(stack) && !(slot == EquipmentSlot.MAINHAND)) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack inSlot = player.getInventory().getItem(i);
                if (!inSlot.isEmpty() && inSlot.getCount() == 1) {
                    Optional<IEnergyStorage> energy = Optional.ofNullable(EnergyCaps.item(inSlot));
                    energy.ifPresent(cap -> {
                        int extractable = this.extractEnergy(stack, Integer.MAX_VALUE, true);
                        int received = cap.receiveEnergy(extractable, false);

                        if (received > 0) {
                            this.extractEnergy(stack, received, false);
                        }
                    });
                }
            }
        }
    }

    @Nonnull
    @Override
    public InteractionResult use(Level worldIn, @Nonnull Player player, @Nonnull InteractionHand hand) {
        if (!worldIn.isClientSide() && player.isShiftKeyDown()) {
            ItemUtil.changeEnabled(player, hand);
            return InteractionResult.SUCCESS.heldItemTransformedTo(player.getItemInHand(hand));
        }
        return super.use(worldIn, player, hand);
    }

    
    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull Item.TooltipContext playerIn, TooltipDisplay display, @Nonnull Consumer<Component> list, @Nonnull TooltipFlag advanced) {
        super.appendHoverText(stack, playerIn, display, list, advanced);
        list.accept(Component.translatable("tooltip.actuallyadditions.battery." + (ItemUtil.isEnabled(stack)
            ? "discharge"
            : "noDischarge")));
        list.accept(Component.translatable("tooltip.actuallyadditions.battery.changeMode"));
    }
}
