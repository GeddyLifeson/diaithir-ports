package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.compat.client.ItemColor;

import com.gregtechceu.gtceu.compat.ArmorItem;

import net.minecraft.world.item.equipment.ArmorType;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ArmorProperty;
import com.gregtechceu.gtceu.client.renderer.item.ArmorItemRenderer;


import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;

import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

public class GTArmorItem extends ArmorItem {

    public final Material material;
    public final ArmorProperty armorProperty;

    public GTArmorItem(ArmorType type, Properties properties, Material material, ArmorProperty armorProperty) {
        super(armorProperty.getArmorMaterial(), type, properties);
        this.material = material;
        this.armorProperty = armorProperty;
        if (GTCEu.isClientSide()) {
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static ItemColor tintColor(Material material) {
        return (itemStack, index) -> material.getLayerARGB(index);
    }

    


    
    public Component getDescription() {
        return Component.translatable(getDescriptionId(), material.getLocalizedName());
    }

    @Override
    public Component getName(ItemStack stack) {
        return this.getDescription();
    }/* ported: removed */
}
