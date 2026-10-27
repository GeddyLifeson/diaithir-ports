package net.id.paradise_lost.mixin.client.render;

import net.id.paradise_lost.entity.ParadiseLostEntityExtensions;
import net.id.paradise_lost.item.armor.FloatyLeggingsItem;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Marks the (copied) render-state leggings stack of an anchored floaty wearer so the client item extension can swap
 * in the "anchored" armor texture. Replaces the pre-26.1 ArmorMaterial.Layer texture wrap.
 */
@Mixin(HumanoidMobRenderer.class)
public class HumanoidArmorLayerMixin {
    public static final String ANCHORED_TAG = "paradise_lost_floaty_anchored";

    @Inject(method = "extractHumanoidRenderState", at = @At("TAIL"), require = 0)
    private static void paradiseLost$markAnchoredFloaty(LivingEntity entity, HumanoidRenderState state, float partialTicks, ItemModelResolver resolver, CallbackInfo ci) {
        if (state.legsEquipment.getItem() instanceof FloatyLeggingsItem
                && FloatyLeggingsItem.isFloatyEnabled(state.legsEquipment)
                && entity instanceof ParadiseLostEntityExtensions extensions
                && extensions.isFloatyAnchored()) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean(ANCHORED_TAG, true);
            state.legsEquipment.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }
}
