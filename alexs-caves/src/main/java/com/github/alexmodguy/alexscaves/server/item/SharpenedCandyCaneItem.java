package com.github.alexmodguy.alexscaves.server.item;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.Vec3;

public class SharpenedCandyCaneItem extends Item {

    public SharpenedCandyCaneItem(Properties properties) {
        super(properties.attributes(createCandyCaneAttributes()));
    }

    private static ItemAttributeModifiers createCandyCaneAttributes() {
        //In 1.21+, Items can accept new ItemAttributeModifiers now when registered, which is done via .attributes() I didn't do that myself for this fix, but it is available
        //To have proper in-game UI for items with damage & speed attributes, use BASE_ATTACK_DAMAGE_ID & BASE_ATTACK_SPEED_ID, which are public and from the base Item.class
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 3.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, 4.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity hurtEntity, LivingEntity player) {
        if (player instanceof Player player1 && !player1.isCreative()) {
            stack.shrink(1);
            player1.playSound(SoundEvents.ITEM_BREAK.value());
        }
        hurtEntity.knockback(0.15F, hurtEntity.getX() - player.getX(), hurtEntity.getZ() - player.getZ());
        if(!hurtEntity.level().isClientSide() && hurtEntity.level() instanceof ServerLevel serverLevel){
            ItemParticleOption itemParticleOption = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ACItemRegistry.SHARPENED_CANDY_CANE.get()));
            Vec3 hurtCenter = hurtEntity.position().add(0, hurtEntity.getBbHeight() * 0.5F, 0);
            Vec3 playerCenter = player.position().add(0, player.getBbHeight() * 0.5F, 0);
            Vec3 particlePos = playerCenter.subtract(hurtCenter).normalize().scale(hurtEntity.getBbWidth() * 0.6F).add(hurtCenter);
            serverLevel.sendParticles(itemParticleOption, particlePos.x, particlePos.y, particlePos.z, 15, 0.3D, hurtEntity.getRandom().nextFloat() * 0.1F - 0.05F, 0.2F, hurtEntity.getRandom().nextFloat() * 0.1F - 0.05F);
        }
        return true;
    }

}
