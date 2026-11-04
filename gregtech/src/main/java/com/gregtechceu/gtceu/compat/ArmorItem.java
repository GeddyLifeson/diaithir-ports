package com.gregtechceu.gtceu.compat;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

/** Replacement for the removed 1.21 ArmorItem: armor behaviour now lives in item components. */
public class ArmorItem extends Item {
    protected final ArmorType type;
    protected final ArmorMaterial material;

    public ArmorItem(ArmorMaterial material, ArmorType type, Properties properties) {
        super(com.gregtechceu.gtceu.common.item.armor.GTArmorMaterials.apply(properties, material, type));
        this.type = type;
        this.material = material;
    }

    public ArmorType getType() {
        return type;
    }

    public ArmorMaterial getMaterial() {
        return material;
    }

    public EquipmentSlot getEquipmentSlot() {
        return type.getSlot();
    }
}
