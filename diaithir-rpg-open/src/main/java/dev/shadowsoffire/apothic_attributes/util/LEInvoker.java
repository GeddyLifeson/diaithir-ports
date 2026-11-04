package dev.shadowsoffire.apothic_attributes.util;

import net.minecraft.world.entity.LivingEntity;

public interface LEInvoker {

    /**
     * Calls {@link LivingEntity#internalSetAbsorptionAmount} with the specified value.
     * <p>
     * This bypasses the value of {@link LivingEntity#getMaxAbsorption()} and direclty updates the number of absorption hearts.
     */
    void apoth_setInternalAbsorption(float absorptionAmount);

}
