package com.github.alexmodguy.alexscaves.server.potion;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SugarRushEffect extends MobEffect {

    protected SugarRushEffect() {
        super(MobEffectCategory.BENEFICIAL, 0XFFA4EB);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, Identifier.parse("alexscaves:sugar_rush_speed"), 0.7F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, Identifier.parse("alexscaves:sugar_rush_attack"), 0.5F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration > 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity entity, int amplifier) {
        return true;
    }

    /** The original slowed the server tick rate for everyone else through Citadel; the port gives the drinker speed instead. */
    public static void enterSlowMotion(Player entity, Level level, int duration, float speed) {
    }

    public static void leaveSlowMotion(Player entity, Level level) {
    }
}
