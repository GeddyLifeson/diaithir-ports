package net.id.paradise_lost.client.rendering.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.id.paradise_lost.ModConstants;
import net.id.paradise_lost.block.blockentity.PalaceDoorBlockEntity;
import net.id.paradise_lost.client.model.ParadiseLostModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class PalaceDoorBlockEntityRenderer implements BlockEntityRenderer<PalaceDoorBlockEntity, PalaceDoorBlockEntityRenderer.State> {

    public static final SpriteId DOORS = new SpriteId(TextureAtlas.LOCATION_BLOCKS, ModConstants.locate("block/palace_door"));

    public static class State extends BlockEntityRenderState {
        public float rotation;
        public float doorAngle;
    }

    private final SpriteGetter sprites;

    private final ModelPart door;

    public PalaceDoorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        ModelPart modelPart = context.bakeLayer(ParadiseLostModelLayers.PALACE_DOOR);
        this.door = modelPart.getChild("door");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();

        PartDefinition door = modelPartData.addOrReplaceChild("door", CubeListBuilder.create()
                        .texOffs(0, 116).addBox(0.0F, -51.0F, -0.5F, 24.0F, 32.0F, 1.0F, CubeDeformation.NONE)
                        .texOffs(0, 0).addBox(0.0F, -112.0F, -2.0F, 24.0F, 112.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(-24.0F, 24.0F, 0.0F));

        return LayerDefinition.create(modelData, 64, 160);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PalaceDoorBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.rotation = entity.getRotation();
        state.doorAngle = entity.getDoorAngle();
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera) {
        RenderType renderType = DOORS.renderType(RenderTypes::entityCutout);
        TextureAtlasSprite sprite = this.sprites.get(DOORS);
        int light = state.lightCoords;
        matrices.pushPose();
        matrices.rotateAround(Axis.XP.rotation(3.141592653589793F), 0.0F, 0.0F, 0.0F);
        matrices.translate(0.0, 0.0, 0.0);

        matrices.rotateAround(Axis.YP.rotation((float) Math.toRadians(state.rotation)), 0.5F, 0.0F, -0.5F);

        matrices.pushPose();
        matrices.rotateAround(Axis.YP.rotation(state.doorAngle), -1.0F, 0.0F, -0.5F);
        matrices.translate(0.5, 2.5, -0.5);
        collector.submitModelPart(door, matrices, renderType, light, OverlayTexture.NO_OVERLAY, sprite, false, false, -1, state.breakProgress, 0);
        matrices.popPose();

        matrices.pushPose();
        matrices.rotateAround(Axis.YP.rotation(-state.doorAngle + 3.141592653589793F), 2F, 0.0F, -0.5F);
        matrices.translate(3.5, 2.5, -0.5);
        collector.submitModelPart(door, matrices, renderType, light, OverlayTexture.NO_OVERLAY, sprite, false, false, -1, state.breakProgress, 0);
        matrices.popPose();

        matrices.popPose();
    }

}
