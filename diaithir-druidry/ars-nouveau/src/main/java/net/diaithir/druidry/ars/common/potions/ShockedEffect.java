package net.diaithir.druidry.ars.common.potions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class ShockedEffect extends MobEffect {

    public ShockedEffect() {
        super(MobEffectCategory.HARMFUL, 2039587);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(net.minecraft.server.level.ServerLevel serverLevel, LivingEntity entity, int amp) {
        int multiplier = 0;
        for (ItemStack i : net.diaithir.druidry.ars.common.util.PortUtil.armorSlots(entity)) {
            IEnergyStorage energyStorage = i.getCapability(Capabilities.EnergyStorage.ITEM);
            if (energyStorage != null) {
                multiplier++;
            }
        }

        IEnergyStorage energyStorage = entity.getMainHandItem().getCapability(Capabilities.EnergyStorage.ITEM);
        if (energyStorage != null)
            multiplier++;
        energyStorage = entity.getOffhandItem().getCapability(Capabilities.EnergyStorage.ITEM);
        if (energyStorage != null)
            multiplier++;
        if (multiplier > 0) {
            int numTicks = 0;
            if (entity instanceof Player) {
                CompoundTag tag = entity.getPersistentData().getCompoundOrEmpty(Player.PERSISTED_NBT_TAG);
            }
            entity.hurt(entity.level().damageSources().lightningBolt(), 20 * multiplier * (amp + 1));

        }
        return true;
    }
}
