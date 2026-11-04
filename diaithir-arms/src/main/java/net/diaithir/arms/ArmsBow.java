package net.diaithir.arms;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

/** A bow whose arrows hit harder (or softer) than the vanilla bow's by a fixed multiplier. */
public class ArmsBow extends BowItem {
    private final float mult;

    public ArmsBow(Properties properties, float mult) {
        super(properties);
        this.mult = mult;
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack projectileStack, ItemStack weaponStack) {
        arrow.setBaseDamage(2.0 * mult);
        return arrow;
    }
}
