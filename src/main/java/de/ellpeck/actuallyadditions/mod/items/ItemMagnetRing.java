/*
 * This file ("ItemMagnetRing.java") is part of the Actually Additions mod for Minecraft.
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

import de.ellpeck.actuallyadditions.mod.items.base.ItemEnergy;
import de.ellpeck.actuallyadditions.mod.util.ItemUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemMagnetRing extends ItemEnergy {

    public ItemMagnetRing() {
        super(200000, 1000);
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return !ItemUtil.isEnabled(stack);
    }

    @Override
    public void inventoryTick(@Nonnull ItemStack stack, ServerLevel world, @Nonnull Entity entity, EquipmentSlot slot) {
        if (entity instanceof Player player && !world.isClientSide() && !ItemUtil.isEnabled(stack)) {
	        if (player.isCreative() || player.isSpectator()) {
                return;
            }
            if (!entity.isShiftKeyDown()) {
                //Get all the Items in the area
                int range = 5;
                List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, new AABB(entity.getX() - range, entity.getY() - range, entity.getZ() - range, entity.getX() + range, entity.getY() + range, entity.getZ() + range));
                if (!items.isEmpty()) {
                    for (ItemEntity item : items) {
                        if (item.getPersistentData().getBooleanOr("PreventRemoteMovement", false)) {
                            continue;
                        }
                        if (item.isAlive() && !item.hasPickUpDelay()) { // TODO config
                            int energyForItem = 50 * item.getItem().getCount();

                            if (this.getEnergyStored(stack) >= energyForItem) {
                                ItemStack oldItem = item.getItem().copy();

                                item.playerTouch(player);

                                if (!player.isCreative()) {
                                    if (!item.isAlive() || !ItemStack.matches(item.getItem(), oldItem)) {
                                        this.extractEnergy(stack, energyForItem, false);
                                    }
                                }
                            }
                        }
                    }
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
}
