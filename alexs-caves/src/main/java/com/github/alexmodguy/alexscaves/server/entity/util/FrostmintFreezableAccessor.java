package com.github.alexmodguy.alexscaves.server.entity.util;

public interface FrostmintFreezableAccessor {

    void setFrostmintFreezing(boolean frostmintFreezingFlag);
    boolean isFreezingFromFrostmint();

    static void freeze(net.minecraft.world.entity.Entity e) {
        if (e instanceof FrostmintFreezableAccessor a) {
            a.setFrostmintFreezing(true);
        } else if (e instanceof net.minecraft.world.entity.LivingEntity l) {
            l.setTicksFrozen(l.getTicksRequiredToFreeze() + 200);
        }
    }
}
