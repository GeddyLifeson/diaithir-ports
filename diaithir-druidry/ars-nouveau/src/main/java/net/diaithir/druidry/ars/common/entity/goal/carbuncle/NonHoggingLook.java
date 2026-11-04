package net.diaithir.druidry.ars.common.entity.goal.carbuncle;

import net.diaithir.druidry.ars.common.entity.Starbuncle;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;

public class NonHoggingLook extends LookAtPlayerGoal {
    Starbuncle carbuncle;

    public NonHoggingLook(Starbuncle entityIn, Class<? extends LivingEntity> watchTargetClass, float maxDistance, float chanceIn) {
        super(entityIn, watchTargetClass, maxDistance, chanceIn);
        carbuncle = entityIn;
    }

}
