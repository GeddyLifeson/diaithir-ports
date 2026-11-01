package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import ewewukek.musketmod.GunItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mixin(Entity.class)
abstract class EntityMixin {
    @ModifyVariable(method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At("HEAD"), argsOnly = true)
    private ItemStack spawnAtLocation(ItemStack stack) {
        Object object = this;
        if (stack.getItem() == Items.ARROW
        && (object instanceof AbstractSkeleton entity) && GunItem.isHoldingGun(entity)) {
            return new ItemStack(ewewukek.musketmod.Items.CARTRIDGE, stack.getCount());
        }
        return stack;
    }
}
