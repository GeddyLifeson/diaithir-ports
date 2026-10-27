package com.mrh0.createaddition.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BrassAmuletItem extends Item {
    public BrassAmuletItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity, net.minecraft.world.entity.EquipmentSlot slot) {
        if(!(entity instanceof Player player)) return;
        if(player.getFoodData().getSaturationLevel() > 1f) {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 3, 0, true, true, true));
        }
    }
}
