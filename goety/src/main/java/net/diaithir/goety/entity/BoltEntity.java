package net.diaithir.goety.entity;

import net.diaithir.goety.GoetyEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** The soul bolt and magic bolt of the dark wand: a small unseen charge that trails light. */
public class BoltEntity extends AbstractHurtingProjectile {
    private float damage = 4.0F;

    public BoltEntity(EntityType<? extends BoltEntity> type, Level level) {
        super(type, level);
    }

    public BoltEntity(EntityType<? extends BoltEntity> type, LivingEntity owner, Vec3 dir, Level level) {
        super(type, owner, dir, level);
    }

    public void setDamage(float d) {
        this.damage = d;
    }

    private boolean soul() {
        return getType() == GoetyEntities.PROJECTILES.get("soul_bolt").get();
    }

    @Override protected @Nullable ParticleOptions getTrailParticle() {
        return soul() ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.WITCH;
    }

    @Override protected boolean shouldBurn() { return false; }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && tickCount > 80) discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (level() instanceof ServerLevel sl) {
            Entity owner = getOwner();
            Entity target = hit.getEntity();
            target.hurtServer(sl, sl.damageSources().indirectMagic(this, owner), damage);
            sl.sendParticles(soul() ? ParticleTypes.SOUL : ParticleTypes.WITCH, getX(), getY(), getZ(), 8, 0.15, 0.15, 0.15, 0.03);
            discard();
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!level().isClientSide() && hit.getType() == HitResult.Type.BLOCK) discard();
    }
}
