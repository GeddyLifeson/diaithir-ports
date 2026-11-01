package net.diaithir.arms;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

/** A crossbow whose bolts hit harder than the vanilla crossbow's by a fixed multiplier. */
public class ArmsCrossbow extends CrossbowItem {
    private final float mult;

    public ArmsCrossbow(Properties properties, float mult) {
        super(properties);
        this.mult = mult;
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack projectileStack, ItemStack weaponStack) {
        arrow.setBaseDamage(2.0 * mult);
        return arrow;
    }
}
