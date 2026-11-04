package net.diaithir.combat.survey;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Mounted charge (survey part 2, mounted combat; Jousting is all rights reserved, so this is clean-room). A rider holding a lance,
 * spear or polearm adds damage in proportion to the mount's speed along the line to the target. Walking pace adds nothing; a full gallop
 * (about 0.3 blocks per tick) roughly triples the hit.
 */
public final class Lance {
    /** Extra damage multiplier per block per tick of closing speed. */
    public static final float PER_SPEED = 6.0F;
    public static final float MAX_BONUS = 2.5F;

    public static boolean isLance(ItemStack s) {
        if (s.isEmpty()) return false;
        String p = BuiltInRegistries.ITEM.getKey(s.getItem()).getPath();
        return p.contains("lance") || p.contains("spear") || p.contains("halberd") || p.contains("pike") || p.contains("polearm");
    }

    /** Bonus factor (0 = none) for a given closing speed in blocks per tick. */
    public static float bonus(double closing) { return (float) Math.max(0.0, Math.min(MAX_BONUS, (closing - 0.1) * PER_SPEED)); }

    @SubscribeEvent
    public void incoming(LivingIncomingDamageEvent e) {
        if (!(e.getSource().getEntity() instanceof ServerPlayer p) || p.getVehicle() == null || !isLance(p.getMainHandItem())) return;
        Entity mount = p.getVehicle();
        Vec3 v = mount.getDeltaMovement();
        Vec3 to = e.getEntity().position().subtract(mount.position()).multiply(1, 0, 1);
        if (to.lengthSqr() < 1e-6) return;
        double closing = v.multiply(1, 0, 1).dot(to.normalize());
        float b = bonus(closing);
        if (b > 0) e.setAmount(e.getAmount() * (1 + b));
    }
}
