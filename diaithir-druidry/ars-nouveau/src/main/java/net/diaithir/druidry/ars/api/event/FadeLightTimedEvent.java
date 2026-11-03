package net.diaithir.druidry.ars.api.event;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Port stub: dynamic-light integration (LambDynamicLights) removed; only keeps the timer. */
public class FadeLightTimedEvent implements ITimedEvent {
    public final Vec3 targetPos;
    public int ticksLeft;
    int starterTicks;
    int startLuminance;
    Level level;

    public FadeLightTimedEvent(Level level, Vec3 pos, int duration, int startLuminance) {
        this.targetPos = pos;
        this.ticksLeft = duration;
        this.starterTicks = duration;
        this.startLuminance = startLuminance;
        this.level = level;
    }

    @Override
    public void tick(boolean serverSide) {
        if (serverSide) return;
        ticksLeft--;
    }

    @Override
    public boolean isExpired() {
        return ticksLeft <= 0;
    }
}
