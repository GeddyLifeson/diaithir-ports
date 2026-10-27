package net.id.paradise_lost.datafixer;

import net.id.paradise_lost.ModConstants;
import net.id.paradise_lost.util.MinecartFloatingHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public final class MinecartFloatDataFix {
    private MinecartFloatDataFix() {
    }

    public static boolean migrateCardinal(AbstractMinecart cart, CompoundTag entityTag) {
        CompoundTag legacy = LegacyCardinalComponents.get(entityTag, LegacyCardinalComponents.MINECART_FLOAT);
        if (legacy == null || legacy.isEmpty()) {
            return false;
        }
        // Prefer already-loaded modern / interim state if present.
        if (MinecartFloatingHelper.getFloatTime(cart) > 0) {
            LegacyCardinalComponents.remove(entityTag, LegacyCardinalComponents.MINECART_FLOAT);
            return false;
        }
        int time = legacy.contains("floatTime") ? legacy.getIntOr("floatTime", 0) : 0;
        boolean floating = legacy.getBooleanOr("floating", false);
        MinecartFloatingHelper.setFloatTime(cart, floating ? Math.max(time, 1) : time);
        LegacyCardinalComponents.remove(entityTag, LegacyCardinalComponents.MINECART_FLOAT);
        ModConstants.LOGGER.debug("Migrated CCA minecart_float (floating={}, floatTime={})", floating, time);
        return true;
    }
}
