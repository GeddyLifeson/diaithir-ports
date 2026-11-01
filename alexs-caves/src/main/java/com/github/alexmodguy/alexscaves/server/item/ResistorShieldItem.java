package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentHelper;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.List;

public class ResistorShieldItem extends ShieldItem implements ClientTickingItem {

    public ResistorShieldItem() {
        super(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().stacksTo(1).durability(1000).rarity(Rarity.UNCOMMON));
    }


    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        player.startUsingItem(interactionHand);
        if (player.isShiftKeyDown()) {
            setPolarity(itemstack, !isScarlet(itemstack));
        }
        player.playSound(ACSoundRegistry.RESITOR_SHIELD_SPIN.get());
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flagIn) {
        tooltip.accept(Component.translatable("item.alexscaves.resistor_shield.desc").withStyle(ChatFormatting.GRAY));
    }



    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int timeUsing) {
        super.onUseTick(level, living, stack, timeUsing);
        int i = getUseDuration(stack, living) - timeUsing;
        boolean scarlet = isScarlet(stack);
        boolean firstHit = i >= 10 && i <= 12;
        int slamEnchantAmount = ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.HEAVY_SLAM, stack);
        float range = 5F;
        if (level.isClientSide()) {
            setUseTime(stack, i);
            if(i == 10){
                living.playSound(ACSoundRegistry.RESITOR_SHIELD_SLAM.get());
            }
            if (i >= 10 && i % 5 == 0) {
                AlexsCaves.PROXY.playWorldSound(living, (byte) (scarlet ? 9 : 10));
                Vec3 particlesFrom = living.position().add(0, 0.2, 0);
                float particleMax = 5 + living.getRandom().nextInt(5);
                for (int particles = 0; particles < particleMax; particles++) {
                    Vec3 vec3 = new Vec3((living.getRandom().nextFloat() - 0.5) * 0.3F, (living.getRandom().nextFloat() - 0.5) * 0.3F, range * 0.5F + range * 0.5F * living.getRandom().nextFloat()).yRot((float) ((particles / particleMax) * Math.PI * 2)).add(particlesFrom);
                    if (scarlet) {
                        level.addParticle(ACParticleRegistry.SCARLET_SHIELD_LIGHTNING.get(), vec3.x, vec3.y, vec3.z, particlesFrom.x, particlesFrom.y, particlesFrom.z);
                    } else {
                        level.addParticle(ACParticleRegistry.AZURE_SHIELD_LIGHTNING.get(), particlesFrom.x, particlesFrom.y, particlesFrom.z, vec3.x, vec3.y, vec3.z);
                    }
                }
            }
        }
        if (i >= 10 && i % 5 == 0) {
            AABB bashBox = living.getBoundingBox().inflate(5, 1, 5);
            for (LivingEntity entity : living.level().getEntitiesOfClass(LivingEntity.class, bashBox)) {
                if (!living.isAlliedTo(entity) && !entity.equals(living) && entity.distanceTo(living) <= range) {
                    com.github.alexmodguy.alexscaves.server.misc.ACSide.hurt(entity, living.damageSources().mobAttack(living), firstHit ? 6 + (slamEnchantAmount * 3) : 2);
                    if (scarlet) {
                        entity.knockback(firstHit ? 0.5D : 0.2D, entity.getX() - living.getX(), entity.getZ() - living.getZ());
                    } else {
                        entity.knockback(firstHit ? 0.5D : 0.2D, living.getX() - living.getX(), living.getZ() - entity.getZ());
                    }
                }
            }
        }
        if (i == 10 && !level.isClientSide()) {
            stack.hurtAndBreak(1, living, EquipmentSlot.MAINHAND);
        }
    }


    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity player, int useTimeLeft) {
        releaseUsingImpl(stack, level, player, useTimeLeft);
        return true;
    }

    private void releaseUsingImpl(ItemStack stack, Level level, LivingEntity player, int useTimeLeft) {
        super.releaseUsing(stack, level, player, useTimeLeft);
        AlexsCaves.PROXY.clearSoundCacheFor(player);
    }

    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity, @javax.annotation.Nullable net.minecraft.world.entity.EquipmentSlot slotArg) {
        super.inventoryTick(stack, level, entity, slotArg);
        tickBoth(stack, level, entity);
    }

    @Override
    public void clientInventoryTick(ItemStack stack, net.minecraft.world.level.Level level, Entity entity) {
        tickBoth(stack, level, entity);
    }

    private void tickBoth(ItemStack stack, net.minecraft.world.level.Level level, Entity entity) {
        if (getUseTime(stack) != 0 && entity instanceof LivingEntity living && !(living.isUsingItem() && living.getItemInHand(living.getUsedItemHand()) == stack)) {
            setUseTime(stack, 0);
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.putInt("PrevUseTime", 0);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        if (level.isClientSide()) {
            boolean scarlet = isScarlet(stack);
            int switchTime = getSwitchTime(stack);
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (tag.getIntOr("PrevSwitchTime", 0) != tag.getIntOr("SwitchTime", 0)) {
                tag.putInt("PrevSwitchTime", getSwitchTime(stack));
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
            if (scarlet && switchTime < 5.0F) {
                setSwitchTime(stack, switchTime + 1);
            }
            if (!scarlet && switchTime > 0.0F) {
                setSwitchTime(stack, switchTime - 1);
            }
        }
    }

    public static void setUseTime(ItemStack stack, int useTime) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("PrevUseTime", getUseTime(stack));
        tag.putInt("UseTime", useTime);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void setSwitchTime(ItemStack stack, int useTime) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("PrevSwitchTime", getSwitchTime(stack));
        tag.putInt("SwitchTime", useTime);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }


    public static void setPolarity(ItemStack stack, boolean scarlet) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean("Polarity", scarlet);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static int getUseTime(ItemStack stack) {
        CompoundTag compoundtag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return compoundtag.getIntOr("UseTime", 0);
    }

    public static float getLerpedUseTime(ItemStack stack, float f) {
        CompoundTag compoundtag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        float prev = (float) compoundtag.getIntOr("PrevUseTime", 0);
        float current = (float) compoundtag.getIntOr("UseTime", 0);
        return prev + f * (current - prev);
    }

    public static int getSwitchTime(ItemStack stack) {
        CompoundTag compoundtag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return compoundtag.getIntOr("SwitchTime", 0);
    }

    public static float getLerpedSwitchTime(ItemStack stack, float f) {
        CompoundTag compoundtag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        float prev = (float) compoundtag.getIntOr("PrevSwitchTime", 0);
        float current = (float) compoundtag.getIntOr("SwitchTime", 0);
        return prev + f * (current - prev);
    }

    public static boolean isScarlet(ItemStack stack) {
        CompoundTag compoundtag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return compoundtag.getBooleanOr("Polarity", false);
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.is(ACItemRegistry.RESISTOR_SHIELD.get()) || !newStack.is(ACItemRegistry.RESISTOR_SHIELD.get());
    }
}
