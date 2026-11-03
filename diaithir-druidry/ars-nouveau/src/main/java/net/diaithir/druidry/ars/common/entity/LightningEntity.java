package net.diaithir.druidry.ars.common.entity;

import net.diaithir.druidry.ars.setup.registry.ModEntities;
import net.diaithir.druidry.ars.setup.registry.ModPotions;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LightningEntity extends LightningBolt {
    private int lightningState;
    public long boltVertex;
    private int boltLivingTime;
    private boolean effectOnly;
    List<Integer> hitEntities = new ArrayList<>();
    @Nullable
    private ServerPlayer caster;

    public float amps;
    public int extendTimes;

    public float ampScalar;
    public float wetBonus;

    public LightningEntity(EntityType<? extends LightningBolt> p_i231491_1_, Level world) {
        super(p_i231491_1_, world);
        this.lightningState = 2;
        this.boltVertex = this.random.nextLong();
        this.boltLivingTime = this.random.nextInt(3) + 1;
    }

    public void setVisualOnly(boolean effectOnly) {
        this.effectOnly = effectOnly;
    }

    public SoundSource getSoundSource() {
        return SoundSource.WEATHER;
    }

    public void setCause(@Nullable ServerPlayer casterIn) {
        this.caster = casterIn;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void tick() {
        this.baseTick();
        if (this.lightningState == 2) {
            Difficulty difficulty = this.level().getDifficulty();

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 1.0f, 0.8F + this.random.nextFloat() * 0.2F);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER, 1.0F, 0.5F + this.random.nextFloat() * 0.2F);
        }

        --this.lightningState;
        if (this.lightningState < 0) {
            if (this.boltLivingTime == 0) {
                this.remove(RemovalReason.DISCARDED);
            } else if (this.lightningState < -this.random.nextInt(10)) {
                --this.boltLivingTime;
                this.lightningState = 1;
                this.boltVertex = this.random.nextLong();
            }
        }

        if (this.lightningState >= 0) {
            if (!(this.level() instanceof ServerLevel)) {
            } else if (!this.effectOnly) {
                List<Entity> list = this.level().getEntities(this, new AABB(this.getX() - 3.0D, this.getY() - 3.0D, this.getZ() - 3.0D, this.getX() + 3.0D, this.getY() + 6.0D + 3.0D, this.getZ() + 3.0D), Entity::isAlive);
                for (Entity entity : list) {
                    if (!net.neoforged.neoforge.event.EventHooks.onEntityStruckByLightning(entity, this)) {
                        float origDamage = this.getDamage();
                        this.setDamage(this.getDamage(entity));
                        EntityStruckByLightningEvent event = new EntityStruckByLightningEvent(entity, this);
                        NeoForge.EVENT_BUS.post(event);
                        if (event.isCanceled())
                            continue;
                        entity.thunderHit((ServerLevel) this.level(), this);
                        this.setDamage(origDamage);
                        if (!level().isClientSide() && !hitEntities.contains(entity.getId()) && entity instanceof LivingEntity) {
                            MobEffectInstance effectInstance = ((LivingEntity) entity).getEffect(ModPotions.SHOCKED_EFFECT);
                            int amp = effectInstance != null ? effectInstance.getAmplifier() : -1;
                            ((LivingEntity) entity).addEffect(new MobEffectInstance(ModPotions.SHOCKED_EFFECT, 200 + 10 * 20 * extendTimes, Math.min(2, amp + 1)));
                        }
                        if (!level().isClientSide() && !hitEntities.contains(entity.getId()))
                            hitEntities.add(entity.getId());

                    }
                }

                if (this.caster != null) {
                    CriteriaTriggers.CHANNELED_LIGHTNING.trigger(this.caster, list);
                }
            }
        }
    }

    public float getDamage(Entity entity) {
        float baseDamage = getDamage() + ampScalar * amps + (entity.isInWaterOrRain() ? wetBonus : 0.0f);
        int multiplier = 1;

        if (entity instanceof LivingEntity livingEntity) {
            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                ItemStack i = livingEntity.getItemBySlot(slot);
                EnergyHandler energyStorage = i.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(i));
                if (energyStorage != null) {
                    multiplier++;
                }
            }
            ItemStack mainHand = ((LivingEntity) entity).getMainHandItem();
            EnergyHandler energyStorage = mainHand.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(mainHand));
            if (energyStorage != null)
                multiplier++;
            ItemStack offHand = ((LivingEntity) entity).getOffhandItem();
            energyStorage = offHand.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(offHand));
            if (energyStorage != null)
                multiplier++;
        }
        return baseDamage * multiplier;
    }

    /**
     * Checks if the entity is in range to render.
     */
    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = 64.0D * getViewScale();
        return distance < d0 * d0;
    }

    @Override
    public EntityType<?> getType() {
        return ModEntities.LIGHTNING_ENTITY.get();
    }
}
