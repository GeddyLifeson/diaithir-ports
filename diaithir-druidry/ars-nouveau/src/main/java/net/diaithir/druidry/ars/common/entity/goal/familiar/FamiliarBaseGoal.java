package net.diaithir.druidry.ars.common.entity.goal.familiar;

import net.diaithir.druidry.ars.common.entity.familiar.FamiliarEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class FamiliarBaseGoal extends Goal {
    public FamiliarEntity entity;

    public FamiliarBaseGoal(FamiliarEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean canUse() {
        return false;
    }
}
