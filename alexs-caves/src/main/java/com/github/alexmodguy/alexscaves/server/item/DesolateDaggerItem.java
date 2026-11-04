package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentHelper;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import com.github.alexmodguy.alexscaves.server.entity.ACEntityRegistry;
import com.github.alexmodguy.alexscaves.server.entity.item.DesolateDaggerEntity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class DesolateDaggerItem extends Item {
    public DesolateDaggerItem() {
        super(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().rarity(ACItemRegistry.getRarityDemonic()).sword(ToolMaterial.DIAMOND, 4F, -2F).durability(360));
    }



    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity hurt, LivingEntity player) {
        hurtEnemyImpl(stack, hurt, player);
    }

    private boolean hurtEnemyImpl(ItemStack stack, LivingEntity hurt, LivingEntity player) {
        super.hurtEnemy(stack, hurt, player);
        if (true) {
            int delayedLevel = ACEnchantmentHelper.getEnchantmentLevel(player.level(), ACEnchantmentRegistry.IMPENDING_STAB, stack);
            for(int i = 0; i < 1 + ACEnchantmentHelper.getEnchantmentLevel(player.level(), ACEnchantmentRegistry.DOUBLE_STAB, stack); i++){
                DesolateDaggerEntity daggerEntity = ACEntityRegistry.DESOLATE_DAGGER.get().create(player.level(), net.minecraft.world.entity.EntitySpawnReason.EVENT);
                daggerEntity.setTargetId(hurt.getId());
                daggerEntity.copyPosition(player);
                daggerEntity.setItemStack(stack);
                daggerEntity.orbitFor = (delayedLevel > 0 ? 40 : 20) + player.getRandom().nextInt(10);
                player.level().addFreshEntity(daggerEntity);
            }
            return true;
        } else {
            return false;
        }
    }


}
