package com.diaithir.addons.newage.content.motor.variants;

import com.diaithir.addons.newage.config.CNAConfig;

public class BasicMotorVariant implements IMotorVariant {
    @Override
    public long getMaxCapacity() {
        return CNAConfig.getServer().basicMotorCapacity.get();
    }

    @Override
    public float getSpeed() {
        return CNAConfig.getServer().basicMotorSpeed.get().floatValue();
    }

    @Override
    public float getStress() {
        return CNAConfig.getServer().basicMotorStress.get().floatValue();
    }
}
