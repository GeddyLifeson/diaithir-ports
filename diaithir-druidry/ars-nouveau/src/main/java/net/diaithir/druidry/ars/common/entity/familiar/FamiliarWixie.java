package net.diaithir.druidry.ars.common.entity.familiar;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.diaithir.druidry.ars.common.block.tile.IAnimationListener;
import net.diaithir.druidry.ars.common.entity.EntityWixie;
import net.diaithir.druidry.ars.common.network.Networking;
import net.diaithir.druidry.ars.common.network.PacketAnimEntity;
import net.diaithir.druidry.ars.common.util.PortUtil;
import net.diaithir.druidry.ars.common.util.PotionUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import org.jetbrains.annotations.NotNull;
import net.diaithir.druidry.ars.anim.AnimatableManager;
import net.diaithir.druidry.ars.anim.RawAnimation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class FamiliarWixie extends FlyingFamiliarEntity implements IAnimationListener {
    public int debuffCooldown;

    public FamiliarWixie(EntityType<? extends PathfinderMob> ent, Level world) {
        super(ent, world);
    }


    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (level().isClientSide() || hand != InteractionHand.MAIN_HAND)
            return InteractionResult.SUCCESS;

        ItemStack stack = player.getItemInHand(hand);

        if (player.getMainHandItem().is(Tags.Items.DYES)) {
            DyeColor color = DyeColor.getColor(stack);
            if (color == null || this.entityData.get(COLOR).equals(color.getName()) || !Arrays.asList(EntityWixie.COLORS).contains(color.getName()))
                return InteractionResult.SUCCESS;
            setColor(color);
            return InteractionResult.SUCCESS;
        } else {
            PotionBrewing brewing = level().potionBrewing();
            ItemStack water = PotionUtil.getPotion(Potions.WATER);
            ItemStack awkward = PotionUtil.getPotion(Potions.AWKWARD);
            ItemStack[] bases = new ItemStack[]{awkward, water};
            for (ItemStack base : bases) {
                ItemStack output = brewing.mix(stack, base);
                if (output.isEmpty() || ItemStack.matches(output, base))
                    continue;
                PotionContents contents = PotionUtil.getContents(output);

                if (!PotionUtil.isEmpty(contents)) {
                    PotionUtil.applyContents(contents, player, player, player);
                    ItemStack potionStack = new ItemStack(Items.POTION);
                    potionStack.set(DataComponents.POTION_CONTENTS, contents);
                    PortUtil.sendMessage(player, Component.translatable("ars_nouveau.wixie_familiar.applied", potionStack.getHoverName().getString()));
                    Networking.sendToNearbyClient(level(), this, new PacketAnimEntity(this.getId(), EntityWixie.Animations.CAST.ordinal()));
                    ParticleUtil.spawnPoof((ServerLevel) level(), player.blockPosition().above());
                    stack.shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.mobInteract(player, hand);
    }

    public void potionEvent(MobEffectEvent.Added event) {
        if (!isAlive() || event.getEffectInstance() == null)
            return;
        Entity target = event.getEntity();
        Entity applier = event.getEffectSource();

        boolean isBeneficialOwner = target.equals(getOwner()) && event.getEffectInstance().getEffect().value().isBeneficial();
        boolean isApplierOwner = applier != null && applier.equals(this.getOwner());
        if (isBeneficialOwner || isApplierOwner) {
            //event.getEffectInstance().getCures(); why is this here Jarva
            event.getEffectInstance().mapDuration(duration -> duration + duration * 2);
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new DebuffTargetGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && debuffCooldown > 0)
            debuffCooldown--;
    }

    @Override
    public void startAnimation(int arg) {
        if (controller == null)
            return;
        if (arg == EntityWixie.Animations.CAST.ordinal()) {
            controller.setAnimation(RawAnimation.begin().thenPlay("cast"));
        }
    }

    @Override
    public Identifier getTexture(FamiliarEntity entity) {
        String color = getColor().toLowerCase();
        if (color.isEmpty())
            color = "blue";
        return ArsNouveau.prefix("textures/entity/wixie_" + color + ".png");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    public static class DebuffTargetGoal extends Goal {
        FamiliarWixie wixie;

        public static ArrayList<Holder<MobEffect>> effectTable = new ArrayList<Holder<MobEffect>>(Arrays.asList(
                MobEffects.SLOWNESS, MobEffects.WEAKNESS, MobEffects.LEVITATION, MobEffects.POISON
        ));

        public DebuffTargetGoal(FamiliarWixie wixie) {
            this.wixie = wixie;
        }

        @Override
        public void tick() {
            super.tick();
            if (wixie.getTarget() == null)
                return;
            Holder<MobEffect> effect = effectTable.get(new Random().nextInt(effectTable.size()));
            if (effect == MobEffects.POISON) {
                if (wixie.getTarget().isInvertedHealAndHarm())
                    effect = MobEffects.REGENERATION;
            }
            Networking.sendToNearbyClient(wixie.level(), wixie, new PacketAnimEntity(wixie.getId(), EntityWixie.Animations.CAST.ordinal()));
            wixie.getTarget().addEffect(new MobEffectInstance(effect, 7 * 20, new Random().nextInt(2)));
            wixie.debuffCooldown = 150;
        }

        @Override
        public boolean canUse() {
            return wixie.debuffCooldown <= 0 && wixie.getTarget() != null;
        }
    }
}
