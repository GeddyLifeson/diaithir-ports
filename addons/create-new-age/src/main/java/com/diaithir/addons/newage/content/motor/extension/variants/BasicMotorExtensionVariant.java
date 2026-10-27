package com.diaithir.addons.newage.content.motor.extension.variants;

import com.diaithir.addons.newage.config.CNAConfig;

public class BasicMotorExtensionVariant implements IMotorExtensionVariant {
    @Override
    public float getMultiplier() {
        return CNAConfig.getServer().basicMotorExtensionMultiplier.get().floatValue();
    }

    @Override
    public long getExtraCapacity() {
        return CNAConfig.getServer().basicMotorExtensionExtraCapacity.get();
    }

    @Override
    public int getScrollStep() {
        return CNAConfig.getServer().basicMotorExtensionScrollStep.get();
    }
}
