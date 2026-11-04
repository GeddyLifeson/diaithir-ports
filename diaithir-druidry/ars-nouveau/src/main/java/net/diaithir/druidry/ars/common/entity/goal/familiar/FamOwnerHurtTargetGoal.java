package net.diaithir.druidry.ars.common.entity.goal.familiar;

import net.diaithir.druidry.ars.api.familiar.IFamiliar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.lang.reflect.Method;
import java.util.EnumSet;

public class FamOwnerHurtTargetGoal extends TargetGoal {
    IFamiliar familiar;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public FamOwnerHurtTargetGoal(IFamiliar familiar) {
        super((Mob) familiar.getThisEntity(), false);
        this.familiar = familiar;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    private static Entity tryGetOwner(Object source) {
        if (source == null) {
            return null;
        }
        try {
            Method m = source.getClass().getMethod("getOwner");
            Object result = m.invoke(source);
            if (result instanceof Entity entity) {
                return entity;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Entity getOwnerServerside() {
        Entity owner = tryGetOwner(this.familiar);
        if (owner == null) {
            owner = tryGetOwner(this.familiar.getThisEntity());
        }
        return owner;
    }

    private boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target == null) {
            return false;
        }
        return target != owner && target != this.familiar.getThisEntity();
    }

    @Override
    public boolean canUse() {
        Entity owner = this.getOwnerServerside();
        LivingEntity livingentity = owner instanceof LivingEntity ? (LivingEntity) owner : null;
        if (livingentity == null) {
            return false;
        } else {
            this.ownerLastHurt = livingentity.getLastHurtMob();
            int i = livingentity.getLastHurtMobTimestamp();
            return i != this.timestamp && this.ownerLastHurt != null && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT) && this.wantsToAttack(this.ownerLastHurt, livingentity);
        }
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurt);
        Entity owner = this.getOwnerServerside();
        if (owner instanceof LivingEntity livingentity) {
            this.timestamp = livingentity.getLastHurtMobTimestamp();
        }

        super.start();
    }
}
