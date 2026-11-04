package net.diaithir.aurorian;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** Cerulean and crystal arrows. */
public class AbilityArrowItem extends ArrowItem {
    private final boolean cerulean;

    public AbilityArrowItem(boolean cerulean, Properties p) {
        super(p);
        this.cerulean = cerulean;
    }

    private EntityType<AurorianProjectiles.Arrow> type() {
        return cerulean ? AurorianProjectiles.CERULEAN.get() : AurorianProjectiles.CRYSTAL.get();
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity owner, @Nullable ItemStack weapon) {
        return new AurorianProjectiles.Arrow(type(), level, owner, stack.copyWithCount(1), weapon);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction dir) {
        AurorianProjectiles.Arrow a = new AurorianProjectiles.Arrow(type(), level, null, stack.copyWithCount(1), null);
        a.setPos(pos.x(), pos.y(), pos.z());
        a.pickup = AbstractArrow.Pickup.ALLOWED;
        return a;
    }
}
