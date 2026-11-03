package net.diaithir.druidry.ars.api.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Port helper replacing LivingEntity#getArmorSlots/getAllSlots, which no longer exist. */
public class EquipUtil {
    public static List<ItemStack> armor(LivingEntity entity) {
        List<ItemStack> list = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            if (slot.isArmor()) {
                list.add(entity.getItemBySlot(slot));
            }
        }
        return list;
    }

    public static List<ItemStack> all(LivingEntity entity) {
        List<ItemStack> list = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            list.add(entity.getItemBySlot(slot));
        }
        return list;
    }
}
