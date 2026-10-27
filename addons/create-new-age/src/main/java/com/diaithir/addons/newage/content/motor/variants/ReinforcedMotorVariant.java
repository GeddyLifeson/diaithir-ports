package com.diaithir.addons.newage.content.motor.variants;

import com.diaithir.addons.newage.config.CNAConfig;

public class ReinforcedMotorVariant implements IMotorVariant {
    @Override
    public long getMaxCapacity() {
        return CNAConfig.getServer().reinforcedMotorCapacity.get();
    }

    @Override
    public float getSpeed() {
        return CNAConfig.getServer().reinforcedMotorSpeed.get().floatValue();
    }

    @Override
    public float getStress() {
        return CNAConfig.getServer().reinforcedMotorStress.get().floatValue();
    }
}
