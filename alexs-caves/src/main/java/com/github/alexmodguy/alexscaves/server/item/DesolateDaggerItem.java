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
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class DesolateDaggerItem extends SwordItem {
    public DesolateDaggerItem() {
        super(Tiers.DIAMOND, (new Item.Properties()).rarity(ACItemRegistry.getRarityDemonic()).attributes(createDaggerAttributes()));
    }

    private static ItemAttributeModifiers createDaggerAttributes() {
        //In 1.21+, Items can accept new ItemAttributeModifiers now when registered, which is done via .attributes() I didn't do that myself for this fix, but it is available
        //To have proper in-game UI for items with damage & speed attributes, use BASE_ATTACK_DAMAGE_ID & BASE_ATTACK_SPEED_ID, which are public and from the base Item.class
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    public int getMaxDamage(ItemStack stack) {
        return 360;
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity hurt, LivingEntity player) {
        if (super.hurtEnemy(stack, hurt, player)) {
            int delayedLevel = ACEnchantmentHelper.getEnchantmentLevel(player.level(), ACEnchantmentRegistry.IMPENDING_STAB, stack);
            for(int i = 0; i < 1 + ACEnchantmentHelper.getEnchantmentLevel(player.level(), ACEnchantmentRegistry.DOUBLE_STAB, stack); i++){
                DesolateDaggerEntity daggerEntity = ACEntityRegistry.DESOLATE_DAGGER.get().create(player.level());
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

    public boolean isValidRepairItem(ItemStack itemStack, ItemStack repairWith) {
        return repairWith.is(ACItemRegistry.PURE_DARKNESS.get());
    }

}
