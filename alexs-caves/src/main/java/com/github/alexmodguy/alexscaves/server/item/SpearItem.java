package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class SpearItem extends Item {

    private final double attackDamage;

    public SpearItem(Properties properties, double damage) {
        super(properties.attributes(createSpearAttributes(damage)));
        this.attackDamage = damage;
    }

    private static ItemAttributeModifiers createSpearAttributes(double damage) {
        //In 1.21+, Items can accept new ItemAttributeModifiers now when registered, which is done via .attributes() I didn't do that myself for this fix, but it is available
        //To have proper in-game UI for items with damage & speed attributes, use BASE_ATTACK_DAMAGE_ID & BASE_ATTACK_SPEED_ID, which are public and from the base Item.class
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity hurtEntity, LivingEntity player) {
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        return true;
    }

    public boolean mineBlock(ItemStack itemStack, Level level, BlockState state, BlockPos blockPos, LivingEntity livingEntity) {
        if ((double) state.getDestroySpeed(level, blockPos) != 0.0D) {
            itemStack.hurtAndBreak(2, livingEntity, EquipmentSlot.MAINHAND);
        }

        return true;
    }


    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        if(!itemstack.isDamageableItem() || itemstack.getDamageValue() < itemstack.getMaxDamage() - 1){
            player.startUsingItem(interactionHand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public float getPowerForTime(int i) {
        float f = (float) i / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }
}
