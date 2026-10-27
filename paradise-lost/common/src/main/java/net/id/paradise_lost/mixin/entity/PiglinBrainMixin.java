package net.id.paradise_lost.mixin.entity;

import net.id.paradise_lost.item.armor.ParadiseLostArmorMaterials;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinAi.class)
public class PiglinBrainMixin {

    @Inject(method = "isWearingSafeArmor", at = @At("TAIL"), cancellable = true)
    private static void wearsGoldArmor(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[] {net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST, net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET}) {
            ItemStack itemStack = entity.getItemBySlot(slot);
            var equippable = itemStack.get(DataComponents.EQUIPPABLE);
            if (equippable == null || equippable.assetId().isEmpty() || !equippable.assetId().get().equals(ParadiseLostArmorMaterials.GLAZED_GOLD.assetId())) continue;
            cir.setReturnValue(true);
        }
    }

}
