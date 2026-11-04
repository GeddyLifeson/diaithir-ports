package net.diaithir.druidry.ars.api.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MathUtil {
    /** Returns the closest pickable entity the given entity is looking at within range, if any. */
    public static @Nullable EntityHitResult getLookedAtEntity(Entity entity, double range) {
        Vec3 eye = entity.getEyePosition();
        Vec3 look = entity.getLookAngle().scale(range);
        Vec3 end = eye.add(look);
        AABB box = entity.getBoundingBox().expandTowards(look).inflate(1.0);
        return ProjectileUtil.getEntityHitResult(entity, eye, end, box, e -> !e.isSpectator() && e.isPickable(), range * range);
    }
}
