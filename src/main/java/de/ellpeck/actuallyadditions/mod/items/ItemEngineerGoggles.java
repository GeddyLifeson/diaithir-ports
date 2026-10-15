/*
 * This file ("ItemInfraredGoggles.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items;

import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;

import net.minecraft.core.component.DataComponents;

import net.minecraft.world.item.equipment.Equippable;

import net.minecraft.world.item.equipment.EquipmentAssets;

import net.minecraft.world.item.equipment.EquipmentAsset;

import net.minecraft.resources.ResourceKey;

import net.minecraft.world.item.Item;

import net.minecraft.world.entity.EquipmentSlot;


import de.ellpeck.actuallyadditions.api.misc.IGoggles;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemEngineerGoggles extends Item implements IGoggles {
    // worn on the head for its model only: no armour, no durability
    private static final ResourceKey<EquipmentAsset> ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, ActuallyAdditions.modLoc("goggles"));

    private final boolean displayMobs;

    public ItemEngineerGoggles(boolean displayMobs) {
        super(ActuallyItems.defaultProps().stacksTo(1).component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setAsset(ASSET).build()));
        this.displayMobs = displayMobs;
    }

    public static boolean isWearing(Player player) {
        ItemStack face = player.getItemBySlot(EquipmentSlot.HEAD);
        return !face.isEmpty() && face.getItem() instanceof IGoggles;
    }

    @Override
    public boolean displaySpectralMobs() {
        return this.displayMobs;
    }
}
