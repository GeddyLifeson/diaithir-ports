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

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class GingerbreadArmorItem extends Item {

    private static final double MIN_SPEED_BOOST = 0.1D;
    private static final double MAX_SPEED_BOOST = 1.0D;
    private final Map<Integer, ItemAttributeModifiers> gingerbreadDurabilityDependentAttributes = new HashMap<>();
    private final ItemAttributeModifiers defaultItemAttributes;
    private final ACArmorMaterial acMaterial;
    private final net.minecraft.world.item.equipment.ArmorType armorType;

    public GingerbreadArmorItem(ACArmorMaterial armorMaterial, net.minecraft.world.item.equipment.ArmorType slot) {
        super(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().humanoidArmor(armorMaterial.material(), slot));
        this.acMaterial = armorMaterial;
        this.armorType = slot;
        this.armorType = slot;
        this.defaultItemAttributes = createGingerbreadAttributes(armorMaterial, slot, MIN_SPEED_BOOST);
    }

    private static ItemAttributeModifiers createGingerbreadAttributes(ACArmorMaterial armorMaterial, net.minecraft.world.item.equipment.ArmorType slot, double speedBoost) {
        Identifier id = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "armor_gingerbread_" + slot.getName());
        EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(slot.getSlot());
        return ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(id, armorMaterial.getDefenseForType(slot), AttributeModifier.Operation.ADD_VALUE), slotGroup)
                .add(Attributes.MOVEMENT_SPEED, new AttributeModifier(id, speedBoost, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slotGroup)
                .build();
    }

    private ItemAttributeModifiers getOrCreateDurabilityAttributes(int durabilityIn, int maxDurability) {
        if (gingerbreadDurabilityDependentAttributes.containsKey(durabilityIn)) {
            return gingerbreadDurabilityDependentAttributes.get(durabilityIn);
        } else {
            float scaledDurability = durabilityIn / (float) maxDurability;
            double speed = MIN_SPEED_BOOST + (MAX_SPEED_BOOST - MIN_SPEED_BOOST) * scaledDurability;
            Identifier id = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "armor_gingerbread_" + armorType.getName() + "_durability_" + durabilityIn);
            EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(armorType.getSlot());
            ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
                    .add(Attributes.ARMOR, new AttributeModifier(id, acMaterial.getDefenseForType(armorType), AttributeModifier.Operation.ADD_VALUE), slotGroup)
                    .add(Attributes.MOVEMENT_SPEED, new AttributeModifier(id, speed, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slotGroup)
                    .build();
            gingerbreadDurabilityDependentAttributes.put(durabilityIn, attributes);
            return attributes;
        }
    }


    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        if (stack.getDamageValue() > 0) {
            return getOrCreateDurabilityAttributes(stack.getDamageValue(), stack.getMaxDamage());
        }
        return defaultItemAttributes;
    }

}
