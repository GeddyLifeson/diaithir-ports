package net.diaithir.druidry.ars.common.entity.familiar;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.event.SpellCostCalcEvent;
import net.diaithir.druidry.ars.api.spell.AbstractSpellPart;
import net.diaithir.druidry.ars.api.spell.Spell;
import net.diaithir.druidry.ars.api.spell.SpellSchools;
import net.diaithir.druidry.ars.common.entity.Whirlisprig;
import net.diaithir.druidry.ars.setup.registry.ModEntities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.diaithir.druidry.ars.anim.AnimationState;
import net.diaithir.druidry.ars.anim.PlayState;
import net.diaithir.druidry.ars.anim.RawAnimation;


public class FamiliarWhirlisprig extends FlyingFamiliarEntity implements ISpellCastListener {
    public FamiliarWhirlisprig(EntityType<? extends PathfinderMob> ent, Level world) {
        super(ent, world);
    }


    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || pPlayer.level().isClientSide())
            return InteractionResult.PASS;

        ItemStack stack = pPlayer.getItemInHand(hand);
        String color = Whirlisprig.getColorFromStack(stack);
        if (color != null && !getColor().equals(color)) {
            setColor(color);
            stack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(pPlayer, hand);
    }

    @Override
    public void onCostCalc(SpellCostCalcEvent event) {
        if (!isAlive())
            return;

        if (getOwner() != null && getOwner().equals(event.context.getUnwrappedCaster())) {
            int discount = 0;
            Spell spell = event.context.getSpell();
            for (AbstractSpellPart part : spell.recipe()) {
                if (SpellSchools.ELEMENTAL_EARTH.isPartOfSchool(part)) {
                    discount += part.getCastingCost() * .5;
                }
            }
            event.currentCost -= discount;
        }
    }

    public void eatEvent(LivingEntityUseItemEvent.Finish event) {
        if (!isAlive())
            return;

        if (!event.getEntity().level().isClientSide() && getOwner() != null && getOwner().equals(event.getEntity())) {
            FoodProperties food = event.getItem().get(DataComponents.FOOD);
            if (food != null) {
                float saturationModifier = food.saturation();
                int nutrition = food.nutrition();
                float satAmount = nutrition * saturationModifier * 2.0f;
                if (event.getEntity() instanceof Player) {
                    FoodData stats = ((Player) event.getEntity()).getFoodData();
                    stats.setSaturation(stats.getSaturationLevel() + satAmount * .4f);
                }
            }
        }
    }

    @Override
    public PlayState walkPredicate(AnimationState event) {
        if (event.isMoving()) {
            event.getController().setAnimation(RawAnimation.begin().thenPlay("fly"));
        } else {
            event.getController().setAnimation(RawAnimation.begin().thenPlay("idle"));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public Identifier getTexture(FamiliarEntity entity) {
        return ArsNouveau.prefix( "textures/entity/whirlisprig_" + (getColor().isEmpty() ? "summer" : getColor().toLowerCase()) + ".png");
    }
}
