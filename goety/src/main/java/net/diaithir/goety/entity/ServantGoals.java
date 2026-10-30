package net.diaithir.goety.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

/** Goals that make a servant walk with, and fight for, its owner. */
public final class ServantGoals {
    private ServantGoals() {}

    public static class Follow extends Goal {
        private final Mob mob;
        private final ServantCore core;
        private final double speed;
        private final float start, stop;
        private int recalc;

        public Follow(Mob mob, ServantCore core, double speed, float start, float stop) {
            this.mob = mob; this.core = core; this.speed = speed; this.start = start; this.stop = stop;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override public boolean canUse() {
            LivingEntity o = core.owner();
            return o != null && !o.isSpectator() && mob.distanceToSqr(o) > start * start;
        }

        @Override public boolean canContinueToUse() {
            LivingEntity o = core.owner();
            return o != null && !mob.getNavigation().isDone() && mob.distanceToSqr(o) > stop * stop;
        }

        @Override public void start() { recalc = 0; }
        @Override public void stop() { mob.getNavigation().stop(); }

        @Override public void tick() {
            LivingEntity o = core.owner();
            if (o == null) return;
            mob.getLookControl().setLookAt(o, 10.0F, mob.getMaxHeadXRot());
            if (--recalc <= 0) {
                recalc = adjustedTickDelay(10);
                if (mob.distanceToSqr(o) >= 400.0) {
                    mob.teleportTo(o.getX(), o.getY(), o.getZ());
                    mob.getNavigation().stop();
                } else {
                    mob.getNavigation().moveTo(o, speed);
                }
            }
        }
    }

    /** Attack whoever hurt the owner, or whoever the owner hurt. */
    public static class DefendOwner extends TargetGoal {
        private final ServantCore core;
        private final boolean byOwner;
        private LivingEntity candidate;
        private int seen;

        public DefendOwner(Mob mob, ServantCore core, boolean attackedByOwner) {
            super(mob, false);
            this.core = core; this.byOwner = attackedByOwner;
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override public boolean canUse() {
            LivingEntity o = core.owner();
            if (o == null) return false;
            candidate = byOwner ? o.getLastHurtMob() : o.getLastHurtByMob();
            int ts = byOwner ? o.getLastHurtMobTimestamp() : o.getLastHurtByMobTimestamp();
            if (candidate == null || ts == seen || core.isFriend(candidate)) return false;
            return canAttack(candidate, TargetingConditions.DEFAULT);
        }

        @Override public void start() {
            LivingEntity o = core.owner();
            if (o != null) seen = byOwner ? o.getLastHurtMobTimestamp() : o.getLastHurtByMobTimestamp();
            mob.setTarget(candidate);
            super.start();
        }
    }
}
