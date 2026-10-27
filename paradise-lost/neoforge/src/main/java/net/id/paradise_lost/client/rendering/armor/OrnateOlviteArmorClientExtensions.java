package net.id.paradise_lost.client.rendering.armor;

import net.id.paradise_lost.client.model.ParadiseLostModelLayers;
import net.id.paradise_lost.client.model.armor.OrnateOlviteArmorModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public final class OrnateOlviteArmorClientExtensions implements IClientItemExtensions {
    private OrnateOlviteArmorModel armorModel;

    @Override
    public Model getHumanoidArmorModel(ItemStack itemStack, EquipmentClientInfo.LayerType layerType, Model original) {
        if (armorModel == null) {
            armorModel = new OrnateOlviteArmorModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(ParadiseLostModelLayers.ORNATE_OLVITE_ARMOR));
        }
        return armorModel;
    }
}
