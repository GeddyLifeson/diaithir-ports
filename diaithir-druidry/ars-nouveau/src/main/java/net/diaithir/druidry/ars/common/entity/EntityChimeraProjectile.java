package net.diaithir.druidry.ars.common.entity;

import net.diaithir.druidry.ars.api.entity.ISummon;
import net.diaithir.druidry.ars.setup.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.diaithir.druidry.ars.anim.GeoEntity;
import net.diaithir.druidry.ars.anim.AnimatableInstanceCache;
import net.diaithir.druidry.ars.anim.AnimatableManager;
import net.diaithir.druidry.ars.anim.GeckoLibUtil;

import java.util.Collection;
import java.util.function.Supplier;

public class EntityChimeraProjectile extends AbstractArrow implements GeoEntity {
    int groundMax;

    @SuppressWarnings("unchecked")
    private static EntityType<? extends AbstractArrow> spikeType() {
        try {
            for (java.lang.reflect.Field f : ModEntities.class.getFields()) {
                String n = f.getName().toUpperCase();
                if (n.contains("CHIMERA") && (n.contains("SPIKE") || n.contains("PROJECTILE"))) {
                    Object o = f.get(null);
                    if (o instanceof Supplier<?> s) {
                        return (EntityType<? extends AbstractArrow>) s.get();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        throw new IllegalStateException("Chimera spike entity type is not registered");
    }

    public EntityChimeraProjectile(Level world) {
        super(spikeType(), world);
    }

    public EntityChimeraProjectile(EntityType<EntityChimeraProjectile> entityChimeraProjectileEntityType, Level world) {
        super(entityChimeraProjectileEntityType, world);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && this.inGroundTime >= 1) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected boolean tryPickup(Player pPlayer) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {}

    @Override
    protected void onHitEntity(EntityHitResult rayTraceResult) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Entity entity = rayTraceResult.getEntity();

        float damage = 7.5f;

        Entity entity1 = this.getOwner();
        DamageSource damagesource;
        if (entity1 == null) {
            damagesource = serverLevel.damageSources().indirectMagic(this, null);
        } else {
            damagesource = serverLevel.damageSources().magic();
            if (entity1 instanceof LivingEntity) {
                ((LivingEntity) entity1).setLastHurtMob(entity);
            }
        }

        boolean isEnderman = entity.getType() == EntityType.ENDERMAN;
        int k = entity.getRemainingFireTicks();
        if (this.isOnFire() && !isEnderman) {
            entity.igniteForSeconds(5);
        }

        if (entity.hurtServer(serverLevel, damagesource, damage)) {
            if (isEnderman) {
                return;
            }

            if (entity instanceof LivingEntity livingentity) {
                this.doPostHurtEffects(livingentity);
            }

            this.playSound(this.getDefaultHitGroundSoundEvent(), 1.0F, 1.2F / (serverLevel.getRandom().nextFloat() * 0.2F + 0.9F));
            this.remove(Entity.RemovalReason.DISCARDED);

        } else {
            entity.setRemainingFireTicks(k);
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.1D));
            this.setYRot(this.getYRot() + 180f);
            this.yRotO += 180.0F;
            if (!level().isClientSide() && this.getDeltaMovement().lengthSqr() < 1.0E-7D) {
                this.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    @Override
    protected void doPostHurtEffects(LivingEntity entity) {
        super.doPostHurtEffects(entity);
        if (!level().isClientSide()) {

            Collection<MobEffectInstance> effects = entity.getActiveEffects();
            MobEffectInstance[] array = effects.toArray(new MobEffectInstance[0]);
            for (MobEffectInstance e : array) {
                if (e.getEffect().value().isBeneficial())
                    entity.removeEffect(e.getEffect());
            }
            entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
        }

    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity instanceof EntityChimeraProjectile) {
            return false;
        }

        if (entity instanceof LivingEntity entity1) {
            // Omit our summoned sources that might aggro or accidentally hurt us
            if (entity1 instanceof WildenStalker || entity1 instanceof WildenGuardian || entity instanceof WildenHunter
                    || (entity instanceof ISummon summon && summon.getOwnerUUID() != null && summon.getOwnerUUID().equals(this.getUUID()))
                    || (entity1 instanceof SummonWolf && ((SummonWolf) entity1).isWildenSummon))
                return false;
        }
        return !(entity instanceof WildenChimera) && super.canHitEntity(entity);
    }

    AnimatableInstanceCache factory = GeckoLibUtil.createInstanceCache(this);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return factory;
    }

    @Override
    public EntityType<?> getType() {
        return spikeType();
    }
}
