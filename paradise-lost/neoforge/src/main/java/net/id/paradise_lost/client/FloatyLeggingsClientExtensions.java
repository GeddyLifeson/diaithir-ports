package net.id.paradise_lost.client;

import net.id.paradise_lost.ModConstants;
import net.id.paradise_lost.mixin.client.render.HumanoidArmorLayerMixin;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * Swaps in the "anchored" leggings texture when the render copy of the stack was tagged by HumanoidArmorLayerMixin.
 */
public final class FloatyLeggingsClientExtensions implements IClientItemExtensions {
    private static final Identifier ANCHORED = ModConstants.locate("textures/entity/equipment/humanoid_leggings/floaty_on.png");

    @Override
    public Identifier getArmorTexture(ItemStack stack, EquipmentClientInfo.LayerType type, EquipmentClientInfo.Layer layer, Identifier _default) {
        if (type == EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS) {
            CustomData data = stack.get(DataComponents.CUSTOM_DATA);
            if (data != null && data.contains(HumanoidArmorLayerMixin.ANCHORED_TAG)) {
                return ANCHORED;
            }
        }
        return _default;
    }
}
