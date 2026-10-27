package net.id.paradise_lost.item.armor;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;

public class FloatyBootsItem extends Item {
    /** Feather Falling IV equivalent: 3 blocks of fall protection per level. */
    public static final float FEATHER_FALLING_BLOCKS = 12.0F;

    public FloatyBootsItem(Properties settings) {
        super(settings);
    }

    public static boolean isWearing(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof FloatyBootsItem;
    }
}
