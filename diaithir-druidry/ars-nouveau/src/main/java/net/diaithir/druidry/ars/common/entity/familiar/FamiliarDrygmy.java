package net.diaithir.druidry.ars.common.entity.familiar;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.event.SpellModifierEvent;
import net.diaithir.druidry.ars.api.spell.SpellSchools;
import net.diaithir.druidry.ars.common.entity.EntityDrygmy;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import net.diaithir.druidry.ars.anim.AnimationState;
import net.diaithir.druidry.ars.anim.PlayState;
import net.diaithir.druidry.ars.anim.RawAnimation;

import java.util.Arrays;

public class FamiliarDrygmy extends FamiliarEntity implements ISpellCastListener {

    public FamiliarDrygmy(EntityType<? extends PathfinderMob> ent, Level world) {
        super(ent, world);
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (level().isClientSide() || hand != InteractionHand.MAIN_HAND)
            return InteractionResult.SUCCESS;

        ItemStack stack = player.getItemInHand(hand);

        if (player.getMainHandItem().is(Tags.Items.DYES)) {
            DyeColor color = DyeColor.getColor(stack);
            if (color == null || this.entityData.get(COLOR).equals(color.getName()) || !Arrays.asList(EntityDrygmy.COLORS).contains(color.getName()))
                return InteractionResult.SUCCESS;
            setColor(color);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void onModifier(SpellModifierEvent event) {
        if (isAlive() && getOwner() != null && getOwner().equals(event.caster) && SpellSchools.ELEMENTAL_EARTH.isPartOfSchool(event.spellPart)) {
            event.builder.addDamageModifier(2.0f);
        }
    }
// TODO: restore looting event
//    public void onLootingEvent(LootingLevelEvent event) {
//        if (event.getDamageSource() != null && isAlive() && getOwner() != null && event.getDamageSource().getEntity() != null && getOwner().equals(event.getDamageSource().getEntity())) {
//            if (level.random.nextFloat() > 0.4) {
//                event.setLootingLevel(event.getLootingLevel() + 1 + random.nextInt(3));
//            }
//        }
//    }

    @Override
    public PlayState walkPredicate(AnimationState event) {
        if (event.isMoving()) {
            event.getController().setAnimation(RawAnimation.begin().thenPlay("run"));
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    @Override
    public Identifier getTexture(FamiliarEntity entity) {
        String color = getColor().toLowerCase();
        if (color.isEmpty()) color = "brown";
        return ArsNouveau.prefix("textures/entity/drygmy_" + color + ".png");
    }
}
