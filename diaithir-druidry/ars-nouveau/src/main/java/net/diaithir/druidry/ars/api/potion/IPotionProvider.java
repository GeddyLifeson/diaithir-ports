package net.diaithir.druidry.ars.api.potion;

import net.diaithir.druidry.ars.common.util.PotionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public interface IPotionProvider {

    @NotNull PotionContents getPotionData(ItemStack stack);

    int usesRemaining(ItemStack stack);

    int maxUses(ItemStack stack);

    default int roomLeft(ItemStack stack){
        return maxUses(stack) - usesRemaining(stack);
    }

    void consumeUses(ItemStack stack, int amount, @Nullable LivingEntity player);

    void addUse(ItemStack stack, int amount, @Nullable LivingEntity player);

    void setData(PotionContents contents, int usesRemaining, int maxUses, ItemStack stack);

    /**
     * Modify the effect instance before applying it to the target
     */
    default MobEffectInstance getEffectInstance(MobEffectInstance effectinstance) {
        return effectinstance;
    }

    default void applyEffects(ItemStack stack, Entity source, Entity inDirectSource, LivingEntity target) {
        for (MobEffectInstance effectinstance : getPotionData(stack).getAllEffects()) {
            effectinstance = getEffectInstance(effectinstance);
            if (effectinstance.getEffect().value().isInstantenous()) {
                if (target.level() instanceof net.minecraft.server.level.ServerLevel sl) effectinstance.getEffect().value().applyInstantenousEffect(sl, source, inDirectSource, target, effectinstance.getAmplifier(), 1.0D);
            } else {
                target.addEffect(new MobEffectInstance(effectinstance), source);
            }
        }
    }

    default void addTooltip(ItemStack stack, List<Component> tooltips){
        PotionContents potionStack = getPotionData(stack);
        PotionContents.addPotionTooltip(potionStack.getAllEffects(), tooltips::add, 1.0F, 20.0f);
    }

    default boolean isEmpty(ItemStack stack){
        return PotionUtil.isEmpty(getPotionData(stack)) || usesRemaining(stack) <= 0;
    }
}
