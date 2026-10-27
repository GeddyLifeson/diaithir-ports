package net.diaithir.goety.entity;

import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/** Owner, lifetime and friend-or-foe rules shared by every servant, held by composition. */
public final class ServantCore {
    private final PathfinderMob mob;
    private @Nullable UUID owner;
    private int life;

    public ServantCore(PathfinderMob mob) {
        this.mob = mob;
    }

    public @Nullable UUID ownerId() {
        return owner;
    }

    public void setOwner(@Nullable LivingEntity e) {
        this.owner = e == null ? null : e.getUUID();
    }

    /** Ticks until the servant crumbles; 0 means it stays until killed. */
    public void setLife(int ticks) {
        this.life = ticks;
    }

    public @Nullable LivingEntity owner() {
        if (owner == null || !(mob.level() instanceof ServerLevel sl)) return null;
        return sl.getEntity(owner) instanceof LivingEntity le ? le : null;
    }

    /** True for the owner and for every other servant of the same owner. */
    public boolean isFriend(@Nullable Entity e) {
        if (e == null || owner == null) return false;
        if (e.getUUID().equals(owner)) return true;
        return e instanceof Servant s && owner.equals(s.core().owner);
    }

    /** Replaces the vanilla target goals with owner defence and adds the follow goal. */
    public void bindGoals(GoalSelector goals, GoalSelector targets) {
        targets.removeAllGoals(g -> true);
        targets.addGoal(1, new ServantGoals.DefendOwner(mob, this, false));
        targets.addGoal(2, new ServantGoals.DefendOwner(mob, this, true));
        targets.addGoal(3, new HurtByTargetGoal(mob));
        goals.addGoal(5, new ServantGoals.Follow(mob, this, 1.0, 8.0F, 3.0F));
    }

    public void tick() {
        if (life > 0 && --life == 0 && mob.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SOUL, mob.getX(), mob.getY() + mob.getBbHeight() / 2, mob.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
            mob.discard();
        }
    }

    public void save(ValueOutput out) {
        if (owner != null) out.putString("Owner", owner.toString());
        out.putInt("Life", life);
    }

    public void load(ValueInput in) {
        owner = in.getString("Owner").map(s -> {
            try { return UUID.fromString(s); } catch (IllegalArgumentException ex) { return null; }
        }).orElse(null);
        life = in.getIntOr("Life", 0);
    }
}
