package dev.shadowsoffire.apothic_enchanting.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/**
 * Mixin to tridents to enable Piercing to work.
 *
 * NOTE (26.1 port): The earlier implementation tracked pierced targets via
 * {@code AbstractArrow#piercingIgnoreEntityIds} directly. That manual tracking has been removed; vanilla
 * {@link net.minecraft.world.entity.projectile.arrow.AbstractArrow#onHitEntity} now honors
 * {@code piercingIgnoreEntityIds} without needing the trident to manage it, so this mixin only needs to
 * seed the pierce level at spawn time from the attached piercing enchant.
 */
@Mixin(value = ThrownTrident.class, remap = false)
public abstract class ThrownTridentMixin extends AbstractArrow {

    protected ThrownTridentMixin(
        EntityType<? extends AbstractArrow> entityType,
        double x, double y, double z,
        Level level,
        ItemStack pickupItemStack,
        ItemStack firedFromWeapon) {
        super(entityType, x, y, z, level, pickupItemStack, firedFromWeapon);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"), require = 0, remap = false)
    private void init(CallbackInfo ci) {
        if (!this.level().isClientSide()) {
            int pierce = EnchantmentHelper.getPiercingCount((ServerLevel) this.level(), this.getPickupItem(), this.getPickupItem());
            this.setPierceLevel((byte) pierce);
        }
    }
}
