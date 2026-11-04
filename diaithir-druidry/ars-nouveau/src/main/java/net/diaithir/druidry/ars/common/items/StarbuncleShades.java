package net.diaithir.druidry.ars.common.items;

import net.diaithir.druidry.ars.api.entity.IDecoratable;
import net.diaithir.druidry.ars.api.item.ICosmeticItem;
import net.diaithir.druidry.ars.common.entity.Starbuncle;
import net.diaithir.druidry.ars.common.entity.familiar.FamiliarStarbuncle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class StarbuncleShades extends AnimModItem implements ICosmeticItem {

    public StarbuncleShades() {
        super();
        withTooltip(Component.translatable("tooltip.starbuncle_shades"));
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack pStack, @NotNull Player pPlayer, @NotNull LivingEntity pInteractionTarget, @NotNull InteractionHand pUsedHand) {
        if (pInteractionTarget instanceof IDecoratable starbuncle && canWear(pInteractionTarget)) {
            starbuncle.setCosmeticItem(pStack.split(1));
            return InteractionResult.SUCCESS;
        }

        return super.interactLivingEntity(pStack, pPlayer, pInteractionTarget, pUsedHand);
    }

    @Override
    public boolean canWear(LivingEntity entity) {
        return entity instanceof Starbuncle || entity instanceof FamiliarStarbuncle;
    }

    //translation applied to the renderer
    @Override
    public Vec3 getTranslations() {
        return new Vec3(0, -.259, .165);
    }

    //scaling applied to the renderer
    @Override
    public Vec3 getScaling() {
        return new Vec3(1.0, 1.0, 1.0);
    }

}
