package net.diaithir.entityanimation.skinlayers3d.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class VoxelLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final boolean slim;

    private VoxelLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent, boolean slim) {
        super(parent);
        this.slim = slim;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void attach(EntityRenderersEvent.AddLayers e) {
        for (var skin : e.getSkins()) {
            var r = e.getPlayerRenderer(skin);
            if (r instanceof LivingEntityRenderer lr) {
                lr.addLayer(new VoxelLayer(lr, "slim".equals(String.valueOf(skin).toLowerCase())));
            }
        }
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state,
                       float yRot, float xRot) {
        if (!SkinLayersConfig.ENABLED.get()) return;
        if (state.isInvisible || state.isSpectator || state.skin == null) return;
        if (state.distanceToCameraSq > sq(SkinLayersConfig.LOD_DISTANCE.get())) return;
        Minecraft mc = Minecraft.getInstance();
        boolean self = mc.player != null && state.id == mc.player.getId();
        if (!self && !SkinLayersConfig.OTHER_PLAYERS.get()) return;

        var tex = state.skin.body().texturePath();
        MeshCache.Meshes meshes = MeshCache.get(tex, slim);
        if (meshes == null || meshes.isLegacy()) return;

        PlayerModel model = getParentModel();
        float scale = SkinLayersConfig.VOXEL_SCALE.get().floatValue() * 1.0f;
        int packedLight = light;
        int overlay = OverlayTexture.NO_OVERLAY;
        boolean hideArmor = SkinLayersConfig.HIDE_UNDER_ARMOR.get();

        List<Runnable> none = new ArrayList<>();
        draw(pose, collector, state, tex, packedLight, overlay, scale, model, meshes, hideArmor);
    }

    private static double sq(double d) {
        return d * d;
    }

    private void draw(PoseStack pose, SubmitNodeCollector collector, AvatarRenderState state,
                      net.minecraft.resources.Identifier tex, int light, int overlay, float scale,
                      PlayerModel model, MeshCache.Meshes meshes, boolean hideArmor) {
        var rt = RenderTypes.entityTranslucent(tex);
        if (SkinLayersConfig.HEAD.get() && state.showHat && !(hideArmor && state.headEquipment != null && !state.headEquipment.isEmpty()))
            part(pose, collector, rt, light, overlay, model.head, meshes.get(MeshCache.Part.HAT), -4, -8, -4, 8, 8, 8, scale);
        if (SkinLayersConfig.BODY.get() && state.showJacket && !(hideArmor && !state.chestEquipment.isEmpty()))
            part(pose, collector, rt, light, overlay, model.body, meshes.get(MeshCache.Part.JACKET), -4, 0, -2, 8, 12, 4, scale);
        int aw = slim ? 3 : 4;
        if (SkinLayersConfig.ARMS.get() && !(hideArmor && !state.chestEquipment.isEmpty())) {
            if (state.showLeftSleeve)
                part(pose, collector, rt, light, overlay, model.leftArm, meshes.get(MeshCache.Part.LEFT_SLEEVE), -1, -2, -2, aw, 12, 4, scale);
            if (state.showRightSleeve)
                part(pose, collector, rt, light, overlay, model.rightArm, meshes.get(MeshCache.Part.RIGHT_SLEEVE), slim ? -2 : -3, -2, -2, aw, 12, 4, scale);
        }
        if (SkinLayersConfig.LEGS.get() && !(hideArmor && !state.legsEquipment.isEmpty())) {
            if (state.showLeftPants)
                part(pose, collector, rt, light, overlay, model.leftLeg, meshes.get(MeshCache.Part.LEFT_PANTS), -2, 0, -2, 4, 12, 4, scale);
            if (state.showRightPants)
                part(pose, collector, rt, light, overlay, model.rightLeg, meshes.get(MeshCache.Part.RIGHT_PANTS), -2, 0, -2, 4, 12, 4, scale);
        }
    }

    private void part(PoseStack pose, SubmitNodeCollector collector, net.minecraft.client.renderer.rendertype.RenderType rt,
                      int light, int overlay, ModelPart mp, VoxelMesh mesh,
                      float ox, float oy, float oz, float w, float h, float d, float scale) {
        if (mesh == null || mesh.isEmpty()) return;
        pose.pushPose();
        mp.translateAndRotate(pose);
        final float fox = ox, foy = oy, foz = oz, fw = w, fh = h, fd = d;
        collector.submitCustomGeometry(pose, rt, (p, vc) -> {
            PoseStack ps = new PoseStack();
            ps.last().set(p);
            mesh.render(ps, vc, light, overlay, -1, fox, foy, foz, fw, fh, fd, scale + 0.0f);
        });
        pose.popPose();
    }
}
