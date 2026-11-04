package net.diaithir.druidry.ars.common.items;

import net.diaithir.druidry.ars.api.entity.IDecoratable;
import net.diaithir.druidry.ars.api.item.ICosmeticItem;
import net.diaithir.druidry.ars.common.entity.Starbuncle;
import net.diaithir.druidry.ars.common.entity.familiar.FamiliarStarbuncle;
import net.diaithir.druidry.ars.common.entity.goal.carbuncle.StarbyPotionBehavior;
import net.diaithir.druidry.ars.common.util.PortUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class WixieHat extends AnimModItem implements ICosmeticItem {

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack pStack, @NotNull Player pPlayer, @NotNull LivingEntity pInteractionTarget, @NotNull InteractionHand pUsedHand) {
        if (pInteractionTarget instanceof Starbuncle starbuncle) {
            starbuncle.setBehavior(new StarbyPotionBehavior(starbuncle, new CompoundTag()));
            PortUtil.sendMessage(pPlayer, Component.translatable("ars_nouveau.starbuncle.potion_behavior_set"));
        }
        if (pInteractionTarget instanceof IDecoratable toWix && canWear(pInteractionTarget)) {
            toWix.setCosmeticItem(pStack.split(1));
            return InteractionResult.SUCCESS;
        }
        return super.interactLivingEntity(pStack, pPlayer, pInteractionTarget, pUsedHand);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
        return super.useOn(pContext);
    }

    @Override
    public Vec3 getTranslations() {
        return new Vec3(0, -.24, -.1);
    }


    @Override
    public Vec3 getScaling() {
        return new Vec3(1.0, 1.0, 1.0);
    }

    @Override
    public boolean canWear(LivingEntity entity) {
        return entity instanceof Starbuncle || entity instanceof FamiliarStarbuncle;
    }

    @Override
    public ItemDisplayContext getTransformType() {
        return ItemDisplayContext.NONE;
    }
}
