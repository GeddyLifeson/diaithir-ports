package net.diaithir.druidry.ars.common.potions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;

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
            EnergyHandler energyStorage = i.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(i));
            if (energyStorage != null) {
                multiplier++;
            }
        }

        ItemStack st = entity.getMainHandItem(); EnergyHandler energyStorage = st.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(st));
        if (energyStorage != null)
            multiplier++;
        ItemStack st2 = entity.getOffhandItem(); energyStorage = st2.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(st2));
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
