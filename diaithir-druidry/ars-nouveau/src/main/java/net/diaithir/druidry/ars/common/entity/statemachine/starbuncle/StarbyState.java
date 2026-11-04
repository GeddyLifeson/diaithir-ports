package net.diaithir.druidry.ars.common.entity.statemachine.starbuncle;

import net.diaithir.druidry.ars.common.entity.Starbuncle;
import net.diaithir.druidry.ars.common.entity.goal.carbuncle.StarbyTransportBehavior;
import net.diaithir.druidry.ars.common.entity.statemachine.IState;
import net.diaithir.druidry.ars.common.entity.statemachine.IStateEvent;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class StarbyState implements IState<StarbyState> {
    public Starbuncle starbuncle;
    public StarbyTransportBehavior behavior;
    public int ticksRunning;
    public Level level;
    public StarbyState(Starbuncle starbuncle, StarbyTransportBehavior behavior) {
        this.starbuncle = starbuncle;
        this.behavior = behavior;
        this.level = starbuncle.level();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onEnd() {

    }

    @Nullable
    @Override
    public StarbyState tick() {
        ticksRunning++;
        return null;
    }

    @Nullable
    @Override
    public StarbyState onEvent(IStateEvent event) {
        return null;
    }
}
