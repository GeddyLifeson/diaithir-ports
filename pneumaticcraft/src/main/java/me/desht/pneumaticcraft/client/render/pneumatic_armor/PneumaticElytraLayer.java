package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;

/**
 * Draws elytra wings on a player wearing a Pneumatic Chestplate with a usable Elytra upgrade. The wings come from
 * the separate equipment asset "pneumatic_wings" so that vanilla's own wings layer never draws them unconditionally.
 */
public class PneumaticElytraLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final ResourceKey<EquipmentAsset> WINGS_ASSET =
            ResourceKey.create(net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID, PneumaticRegistry.RL("pneumatic_wings"));

    private final ElytraModel model;
    private final EquipmentLayerRenderer equipmentRenderer;

    public PneumaticElytraLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent, EntityModelSet modelSet, EquipmentLayerRenderer equipmentRenderer) {
        super(parent);
        this.model = new ElytraModel(modelSet.bakeLayer(ModelLayers.ELYTRA));
        this.equipmentRenderer = equipmentRenderer;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
        ItemStack chest = state.chestEquipment;
        if (!(chest.getItem() instanceof PneumaticArmorItem) || Minecraft.getInstance().level == null) return;
        Player player = Minecraft.getInstance().level.getNearestPlayer(state.x, state.y, state.z, 1.0, false);
        if (player == null || !(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof PneumaticArmorItem)) return;
        if (!CommonArmorHandler.getHandlerForPlayer(player).upgradeUsable(CommonUpgradeHandlers.elytraHandler, true)) return;
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.125F);
        equipmentRenderer.renderLayers(EquipmentClientInfo.LayerType.WINGS, WINGS_ASSET, model, state, chest, poseStack, collector, light, null, state.outlineColor, 0);
        poseStack.popPose();
    }
}
