package com.github.alexmodguy.alexscaves.client.render.blockentity;

import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.server.block.blockentity.HologramProjectorBlockEntity;
import com.github.alexmodguy.alexscaves.server.misc.ACMath;
import com.github.alexthe666.citadel.client.shader.PostEffectRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import com.github.alexthe666.citadel.client.render.legacy.LegacyBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.*;

import static com.github.alexmodguy.alexscaves.client.render.entity.NotorRenderer.renderEntityInHologram;

public class HologramProjectorBlockRenderer<T extends HologramProjectorBlockEntity> extends LegacyBlockEntityRenderer<T> {

    private static final Map<BlockPos, HologramProjectorBlockEntity> allOnScreen = new HashMap<>();
    private static final Map<UUID, PlayerInfo> playerInfo = new HashMap<>();


    public HologramProjectorBlockRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    public static void renderEntireBatch(LevelRenderer levelRenderer, PoseStack poseStack, int renderTick, Camera camera, float partialTick) {
    }

    

    @Override
    public void render(T hologram, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (!hologram.isRemoved()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.5F, 0.5F);
            renderAt(hologram, partialTicks, poseStack, bufferIn);
            poseStack.popPose();
        }
    }

    private static void renderAt(HologramProjectorBlockEntity projectorBlockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn) {
        PostEffectRegistry.renderEffectForNextTick(ClientProxy.HOLOGRAM_SHADER);
        Entity holoEntity = projectorBlockEntity.getDisplayEntity(Minecraft.getInstance().level);
        float amount = projectorBlockEntity.getSwitchAmount(partialTicks);
        float ticks = projectorBlockEntity.tickCount + partialTicks;
        float bob1 = (float) (Math.sin(ticks * 0.05F + amount) * 0.1F);
        float bob2 = (float) (Math.cos(ticks * 0.05F + amount) * 0.1F);
        float length = (1F + bob1) * amount;
        float width = ((holoEntity == null ? 0.8F : holoEntity.getBbWidth()) + bob2) * amount;
        if(holoEntity instanceof LivingEntity living){
            width *= living.getScale();
        }
        VertexConsumer lightConsumer = bufferIn.getBuffer(ACRenderTypes.getHologramLights());
        poseStack.pushPose();
        float padStart = 0.125F;
        float padEnd = 1.0F - padStart;
        poseStack.pushPose();
        poseStack.translate(-0.5F, -0.235F, -0.5F);
        float cameraY = com.github.alexthe666.citadel.client.render.legacy.LegacyRenderContext.camera().yRot;
        PoseStack.Pose posestack$pose = poseStack.last();
        Matrix4f matrix4f1 = posestack$pose.pose();
        lightConsumer.addVertex(matrix4f1, padStart, 0.0F, padEnd).setColor(220, 220, 255, (int) (amount * 150));
        lightConsumer.addVertex(matrix4f1, padEnd, 0.0F, padEnd).setColor(220, 220, 255, (int) (amount * 150));
        lightConsumer.addVertex(matrix4f1, padEnd, 0.0F, padStart).setColor(220, 220, 255, (int) (amount * 150));
        lightConsumer.addVertex(matrix4f1, padStart, 0.0F, padStart).setColor(220, 220, 255, (int) (amount * 150));
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0F, -0.2F, 0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - cameraY));
        PoseStack.Pose posestack$pose1 = poseStack.last();
        Matrix4f matrix4f2 = posestack$pose1.pose();
        shineOriginVertex(lightConsumer, matrix4f2, 0, 0);
        shineLeftCornerVertex(lightConsumer, matrix4f2, length, width, 0, 0);
        shineRightCornerVertex(lightConsumer, matrix4f2, length, width, 0, 0);
        shineLeftCornerVertex(lightConsumer, matrix4f2, length, width, 0, 0);
        if (projectorBlockEntity.isPlayerRender()) {
            poseStack.pushPose();
            poseStack.scale(1, amount, 1);
            poseStack.translate(0, length + 1.5F, 0);
            poseStack.mulPose(Axis.YN.rotationDegrees(180 - cameraY + projectorBlockEntity.getRotation(partialTicks)));
            renderPlayerHologram(projectorBlockEntity.getLastPlayerUUID(), partialTicks, poseStack, bufferIn, 240);
            poseStack.popPose();
        } else if (holoEntity != null) {
            poseStack.pushPose();
            poseStack.scale(1, amount, 1);
            poseStack.translate(0, length + 1.5F, 0);
            poseStack.mulPose(Axis.YN.rotationDegrees(180 - cameraY + projectorBlockEntity.getRotation(partialTicks)));
            renderEntityInHologram(holoEntity, 0, 0, 0, 0, partialTicks, poseStack, bufferIn, 240);
            poseStack.popPose();
        }
        poseStack.popPose();
        poseStack.popPose();

    }

    private static PlayerInfo getPlayerInfo(UUID uuid) {
        if (!playerInfo.containsKey(uuid)) {
            playerInfo.put(uuid, Minecraft.getInstance().getConnection().getPlayerInfo(uuid));
        }
        return playerInfo.get(uuid);
    }

    

    


    

    private static void renderPlayerHologram(UUID lastPlayerUUID, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int i) {
        // TODO 26.1: player hologram needs the new PlayerModel/skin pipeline
    }


    private static void shineOriginVertex(VertexConsumer p_114220_, Matrix4f p_114221_, float xOffset, float yOffset) {
        p_114220_.addVertex(p_114221_, 0.0F, 0.0F, 0.0F).setColor(255, 255, 255, 230);
    }

    private static void shineLeftCornerVertex(VertexConsumer p_114215_, Matrix4f p_114216_, float p_114217_, float p_114218_, float xOffset, float yOffset) {
        p_114215_.addVertex(p_114216_, -ACMath.HALF_SQRT_3 * p_114218_, p_114217_, 0).setColor(0, 0, 255, 0);
    }

    private static void shineRightCornerVertex(VertexConsumer p_114224_, Matrix4f p_114225_, float p_114226_, float p_114227_, float xOffset, float yOffset) {
        p_114224_.addVertex(p_114225_, ACMath.HALF_SQRT_3 * p_114227_, p_114226_, 0).setColor(0, 0, 255, 0);
    }

    public int getViewDistance() {
        return 128;
    }
}
