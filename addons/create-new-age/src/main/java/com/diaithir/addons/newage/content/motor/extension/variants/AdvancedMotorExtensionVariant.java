package com.diaithir.addons.newage.content.motor.extension.variants;

import com.diaithir.addons.newage.config.CNAConfig;

public class AdvancedMotorExtensionVariant implements IMotorExtensionVariant {
    @Override
    public float getMultiplier() {
        return CNAConfig.getServer().advancedMotorExtensionMultiplier.get().floatValue();
    }

    @Override
    public long getExtraCapacity() {
        return CNAConfig.getServer().advancedMotorExtensionExtraCapacity.get();
    }

    @Override
    public int getScrollStep() {
        return CNAConfig.getServer().advancedMotorExtensionScrollStep.get();
    }
}
