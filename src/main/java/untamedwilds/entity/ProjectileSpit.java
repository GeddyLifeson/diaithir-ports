package untamedwilds.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import untamedwilds.init.ModEntity;

import javax.annotation.Nullable;

public class ProjectileSpit extends Projectile {

    private MobEffectInstance mobEffect;

    public <T extends Mob> ProjectileSpit(EntityType<? extends ProjectileSpit> p_37224_, Level p_37225_) {
        super(p_37224_, p_37225_);
    }

    public ProjectileSpit(Level p_37235_, ComplexMob p_37236_) {
        this(p_37235_, p_37236_, null);
    }

    public ProjectileSpit(Level p_37235_, ComplexMob p_37236_, @Nullable MobEffectInstance effect) {
        this(ModEntity.SPIT.get(), p_37235_);
        this.setOwner(p_37236_);
        this.mobEffect = effect;
        this.setPos(p_37236_.getX() - (double)(p_37236_.getBbWidth() + 1.0F) * 0.5D * (double) Mth.sin(p_37236_.yBodyRot * ((float)Math.PI / 180F)), p_37236_.getEyeY() - (double)0.1F, p_37236_.getZ() + (double)(p_37236_.getBbWidth() + 1.0F) * 0.5D * (double)Mth.cos(p_37236_.yBodyRot * ((float)Math.PI / 180F)));
    }

    public void tick() {
        super.tick();
        Vec3 vec3 = this.getDeltaMovement();
        Vec3 start = this.position();
        Vec3 end = start.add(vec3);
        BlockHitResult blockHit = this.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
    EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(this, start, end, this.getBoundingBox().expandTowards(vec3).inflate(1.0D, 1.0D, 1.0D), this::canHitEntity, vec3.length());
        HitResult hitresult;
        if (entityHit != null && (blockHit == null || start.distanceTo(entityHit.getLocation()) <= start.distanceTo(blockHit.getLocation()))) {
            hitresult = entityHit;
        } else {
            hitresult = blockHit;
        }
        if (hitresult != null && hitresult.getType() != HitResult.Type.MISS) {
            this.onHit(hitresult);
        }
        double d0 = this.getX() + vec3.x;
        double d1 = this.getY() + vec3.y;
        double d2 = this.getZ() + vec3.z;
        this.updateRotation();
        if (this.level().getBlockStates(this.getBoundingBox()).noneMatch(BlockBehaviour.BlockStateBase::isAir)) {
            this.discard();
        } else if (this.isInWater()) {
            this.discard();
        } else {
            this.setDeltaMovement(vec3.scale(0.99F));
            if (!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, (double)-0.06F, 0.0D));
            }

            this.setPos(d0, d1, d2);
        }
    }

    protected void onHitEntity(EntityHitResult p_37241_) {
        super.onHitEntity(p_37241_);
        if (this.getOwner() == null || !this.getOwner().getClass().equals(p_37241_.getEntity().getClass())) {
            if (this.getOwner() instanceof LivingEntity owner) {
                p_37241_.getEntity().hurt(this.level().damageSources().mobProjectile(this, owner), 1);
            } else {
                p_37241_.getEntity().hurt(this.level().damageSources().generic(), 1);
            }
        }
        if (this.mobEffect != null && p_37241_.getEntity() instanceof LivingEntity living)
            living.addEffect(this.mobEffect);
    }

    protected void onHitBlock(BlockHitResult p_37239_) {
        super.onHitBlock(p_37239_);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No synched data to define for this projectile
    }

    public void recreateFromPacket(ClientboundAddEntityPacket p_150162_) {
        super.recreateFromPacket(p_150162_);
        net.minecraft.world.phys.Vec3 mv_ = p_150162_.getMovement();
        double d0 = mv_.x;
        double d1 = mv_.y;
        double d2 = mv_.z;

        for(int i = 0; i < 7; ++i) {
            double d3 = 0.4D + 0.1D * (double)i;
            this.level().addParticle(ParticleTypes.SPIT, this.getX(), this.getY(), this.getZ(), d0 * d3, d1, d2 * d3);
        }

        this.setDeltaMovement(d0, d1, d2);
    }
}