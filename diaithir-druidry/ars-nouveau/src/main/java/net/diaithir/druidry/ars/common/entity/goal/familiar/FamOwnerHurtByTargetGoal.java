package net.diaithir.druidry.ars.common.entity.goal.familiar;

import net.diaithir.druidry.ars.api.familiar.IFamiliar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class FamOwnerHurtByTargetGoal extends TargetGoal {

    IFamiliar familiar;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public FamOwnerHurtByTargetGoal(IFamiliar familiar) {
        super((Mob) familiar.getThisEntity(), false);
        this.familiar = familiar;
    }

    @Override
    public boolean canUse() {
        Entity owner = this.familiar.getOwner();
        LivingEntity livingentity = owner instanceof LivingEntity ? (LivingEntity) owner : null;

        if (livingentity == null) {
            return false;
        } else {
            this.ownerLastHurtBy = livingentity.getLastHurtByMob();
            int i = livingentity.getLastHurtByMobTimestamp();
            return i != this.timestamp && this.ownerLastHurtBy != null && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT);
        }
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy);
        Entity owner = this.familiar.getOwner();
        LivingEntity livingentity = owner instanceof LivingEntity ? (LivingEntity) owner : null;
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtByMobTimestamp();
        }

        super.start();
    }
}
