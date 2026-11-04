package net.diaithir.druidry.ars.common.potions;

import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ImmolateEffect extends MobEffect {
    public ImmolateEffect() {
        super(MobEffectCategory.BENEFICIAL, new ParticleColor(250, 0, 0).getColor());
    }

}
