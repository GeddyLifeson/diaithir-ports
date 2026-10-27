package com.github.alexmodguy.alexscaves.server.item;

import net.minecraft.world.item.Item;
import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.NeoForgeMod;

import javax.annotation.Nullable;

public class DivingArmorItem extends Item {
    private final ACArmorMaterial acMaterial;

    public DivingArmorItem(ACArmorMaterial armorMaterial, net.minecraft.world.item.equipment.ArmorType slot) {
        super(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().humanoidArmor(armorMaterial.material(), slot).attributes(createDivingAttributes(armorMaterial, slot)));
        this.acMaterial = armorMaterial;
    }

    private static ItemAttributeModifiers createDivingAttributes(ACArmorMaterial armorMaterial, net.minecraft.world.item.equipment.ArmorType slot) {
        Identifier id = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "armor_diving_" + slot.getName());
        EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(slot.getSlot());
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        builder.add(Attributes.ARMOR, new AttributeModifier(id, armorMaterial.getDefenseForType(slot), AttributeModifier.Operation.ADD_VALUE), slotGroup);
        if (slot == net.minecraft.world.item.equipment.ArmorType.LEGGINGS) {
            builder.add(NeoForgeMod.SWIM_SPEED, new AttributeModifier(id, 0.5D, AttributeModifier.Operation.ADD_VALUE), slotGroup);
        } else if (slot == net.minecraft.world.item.equipment.ArmorType.CHESTPLATE) {
            builder.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id, armorMaterial.getToughness(), AttributeModifier.Operation.ADD_VALUE), slotGroup);
        }
        float knockbackResist = armorMaterial.getKnockbackResistance();
        if (knockbackResist > 0) {
            builder.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id, knockbackResist, AttributeModifier.Operation.ADD_VALUE), slotGroup);
        }
        return builder.build();
    }

    @Override
    public void initializeClient(java.util.function.Consumer<IClientItemExtensions> consumer) {
        consumer.accept((IClientItemExtensions) AlexsCaves.PROXY.getArmorProperties());
    }

    @Override
    @Nullable
    public Identifier getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        if (slot == EquipmentSlot.LEGS) {
            return Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/armor/diving_suit_1.png");
        } else {
            return Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/armor/diving_suit_0.png");
        }
    }
}
