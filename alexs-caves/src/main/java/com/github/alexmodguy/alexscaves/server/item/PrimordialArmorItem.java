package com.github.alexmodguy.alexscaves.server.item;

import net.minecraft.world.item.Item;
import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;

public class PrimordialArmorItem extends Item {

    private final ACArmorMaterial acMaterial;

    public PrimordialArmorItem(ACArmorMaterial armorMaterial, net.minecraft.world.item.equipment.ArmorType slot) {
        super(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().humanoidArmor(armorMaterial.material(), slot));
        this.acMaterial = armorMaterial;
    }



    public static int getExtraSaturationFromArmor(LivingEntity entity) {
        int i = 0;
        if (entity.getItemBySlot(EquipmentSlot.HEAD).is(ACItemRegistry.PRIMORDIAL_HELMET.get())) {
            i++;
        }
        if (entity.getItemBySlot(EquipmentSlot.CHEST).is(ACItemRegistry.PRIMORDIAL_TUNIC.get())) {
            i++;
        }
        if (entity.getItemBySlot(EquipmentSlot.LEGS).is(ACItemRegistry.PRIMORDIAL_PANTS.get())) {
            i++;
        }
        return i;
    }
}
