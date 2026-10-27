package com.diaithir.addons.newage.content.motor.variants;

import com.diaithir.addons.newage.config.CNAConfig;

public class AdvancedMotorVariant implements IMotorVariant {
    @Override
    public long getMaxCapacity() {
        return CNAConfig.getServer().advancedMotorCapacity.get();
    }

    @Override
    public float getSpeed() {
        return CNAConfig.getServer().advancedMotorSpeed.get().floatValue();
    }

    @Override
    public float getStress() {
        return CNAConfig.getServer().advancedMotorStress.get().floatValue();
    }
}
