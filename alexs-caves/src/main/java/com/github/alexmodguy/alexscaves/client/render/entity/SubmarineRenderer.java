package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.SubmarineModel;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.server.entity.item.SubmarineEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import com.github.alexthe666.citadel.client.render.legacy.LegacyEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import static net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

public class SubmarineRenderer extends LegacyEntityRenderer<SubmarineEntity> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/submarine/submarine.png");
    private static final Identifier TEXTURE_EXPOSED = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/submarine/submarine_exposed.png");
    private static final Identifier TEXTURE_WEATHERED = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/submarine/submarine_weathered.png");
    private static final Identifier TEXTURE_OXIDIZED = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/submarine/submarine_oxidized.png");
    private static final Identifier TEXTURE_NEW = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/submarine/submarine_new.png");
    private static final Identifier TEXTURE_LOW = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/submarine/submarine_low.png");
    private static final Identifier TEXTURE_MEDIUM = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/submarine/submarine_medium.png");
    private static final Identifier TEXTURE_HIGH = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/submarine/submarine_high.png");
    private static final Identifier TEXTURE_CRITICAL = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/submarine/submarine_critical.png");
    private static final Identifier TEXTURE_BUTTONS = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/submarine/submarine_buttons.png");
    private static final Identifier TEXTURE_GLOW = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/submarine/submarine_glow.png");
    private static SubmarineModel MODEL = new SubmarineModel();
    private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0D) / 2.0D);

    public SubmarineRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.0F;
    }

    public void render(SubmarineEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource source, int lightIn) {
        if (!isFirstPersonFloodlightsMode(entity)) {
            renderSubmarine(entity, partialTicks, poseStack, source, lightIn, true);
            super.render(entity, entityYaw, partialTicks, poseStack, source, lightIn);
        }
    }

    public static boolean isFirstPersonFloodlightsMode(SubmarineEntity entity) {
        Entity player = Minecraft.getInstance().getCameraEntity();
        return player.isPassengerOfSameVehicle(entity) && Minecraft.getInstance().options.getCameraType().isFirstPerson() && entity.areLightsOn();
    }

    public static void renderSubFirstPerson(SubmarineEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource source) {
        renderSubmarine(entity, partialTicks, poseStack, source, LevelRenderer.getLightCoords(entity.level(), entity.blockPosition()), false);
    }

    public static void renderSubmarine(SubmarineEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource source, int lightIn, boolean maskWater) {
        Player player = Minecraft.getInstance().player;
        float ageInTicks = entity.tickCount + partialTicks;
        float submarineYaw = entity.getViewYRot(partialTicks);
        float submarinePitch = entity.getViewXRot(partialTicks);
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.5D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - submarineYaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.XN.rotationDegrees(submarinePitch));
        poseStack.translate(0, 0, 0);
        if (entity.getWaterHeight() > 0 && entity.getWaterHeight() < 1.6F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (Math.sin(ageInTicks * 0.1F) * 0.5F)));
            poseStack.mulPose(Axis.XP.rotationDegrees((float) (Math.sin(ageInTicks * 0.1F + 1.3F) * 0.5F)));
        }
        poseStack.pushPose();
        MODEL.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
        for (Entity passenger : entity.getPassengers()) {
            if (passenger == player && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                continue;
            }
            AlexsCaves.PROXY.releaseRenderingEntity(passenger.getUUID());
            poseStack.pushPose();
            poseStack.translate(0, 0.65F, -0.75F);
            poseStack.mulPose(Axis.XN.rotationDegrees(180F));
            poseStack.mulPose(Axis.YN.rotationDegrees(360 - submarineYaw));
            renderPassenger(passenger, 0, 0, 0, 0, partialTicks, poseStack, source, lightIn);
            poseStack.popPose();
            AlexsCaves.PROXY.blockRenderingEntity(passenger.getUUID());
        }
        VertexConsumer textureBuffer = source.getBuffer(RenderTypes.entityCutout(getSubmarineBaseTexture(entity)));
        MODEL.renderToBuffer(poseStack, textureBuffer, lightIn, OverlayTexture.NO_OVERLAY, -1);
        VertexConsumer damageBuffer = source.getBuffer(RenderTypes.entityTranslucent(getSubmarineDamageTexture(entity)));
        MODEL.renderToBuffer(poseStack, damageBuffer, lightIn, OverlayTexture.NO_OVERLAY, -1);
        if (entity.getDamageLevel() <= 3) {
            VertexConsumer buttonsBuffer = source.getBuffer(ACRenderTypes.getEyesAlphaEnabled(TEXTURE_BUTTONS));
            MODEL.renderToBuffer(poseStack, buttonsBuffer, lightIn, OverlayTexture.NO_OVERLAY, ColorUtil.packColor(1.0F, 1.0F, 1.0F, entity.getSonarFlashAmount(partialTicks)));
            if (entity.areLightsOn() && entity.isVehicle()) {
                VertexConsumer glowBuffer = source.getBuffer(RenderTypes.eyes(TEXTURE_GLOW));
                MODEL.renderToBuffer(poseStack, glowBuffer, lightIn, OverlayTexture.NO_OVERLAY, -1);
            }
        }
        if (maskWater) {
            VertexConsumer waterMask = source.getBuffer(ACRenderTypes.getSubmarineMask());
            MODEL.setupWaterMask(entity, partialTicks);
            MODEL.getWaterMask().render(poseStack, waterMask, lightIn, OverlayTexture.NO_OVERLAY, -1);
        }
        if (!isFirstPersonFloodlightsMode(entity) && entity.areLightsOn() && entity.isVehicle()) {
            Entity first = entity.getFirstPassenger();
            float xRot = 0;
            float yRot = 0;
            if (first instanceof Player firstPlayer) {
                float headYaw = (firstPlayer.yHeadRotO + (firstPlayer.getYHeadRot() - firstPlayer.yHeadRotO) * partialTicks);
                xRot = firstPlayer.getViewXRot(partialTicks) + submarinePitch;
                yRot = Mth.approachDegrees(submarineYaw, headYaw, 60);
            }
            float length = 4.5F;
            float width = 0.45F;
            poseStack.pushPose();
            poseStack.translate(0, 0.75F, -2.4F);
            poseStack.mulPose(Axis.YN.rotationDegrees(submarineYaw));
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
            poseStack.mulPose(Axis.XN.rotationDegrees(90 - xRot));
            poseStack.scale(3, 1, 1);
            poseStack.translate(0, -1, 0F);
            PoseStack.Pose posestack$pose = poseStack.last();
            Matrix4f matrix4f1 = posestack$pose.pose();
            VertexConsumer lightConsumer = source.getBuffer(ACRenderTypes.getSubmarineLights());
            shineOriginVertex(lightConsumer, matrix4f1, 0, 0);
            shineLeftCornerVertex(lightConsumer, matrix4f1, length, width, 0, 0);
            shineRightCornerVertex(lightConsumer, matrix4f1, length, width, 0, 0);
            shineLeftCornerVertex(lightConsumer, matrix4f1, length, width, 0, 0);
            poseStack.popPose();
        }

        poseStack.popPose();
        poseStack.popPose();
    }

    private static Identifier getSubmarineDamageTexture(SubmarineEntity entity) {
        switch (entity.getDamageLevel()) {
            case 0:
                return TEXTURE_NEW;
            case 1:
                return TEXTURE_LOW;
            case 2:
                return TEXTURE_MEDIUM;
            case 3:
                return TEXTURE_HIGH;
            case 4:
                return TEXTURE_CRITICAL;
        }
        return TEXTURE_NEW;
    }

    public static <E extends Entity> void renderPassenger(E entityIn, double x, double y, double z, float yaw, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLight) {
        com.github.alexthe666.citadel.client.render.legacy.LegacyRenderContext.renderEntity(entityIn, x, y, z, yaw, partialTicks, matrixStack, packedLight);
    }

    private static void shineOriginVertex(VertexConsumer p_114220_, Matrix4f p_114221_, float xOffset, float yOffset) {
        p_114220_.addVertex(p_114221_, 0.0F, 0.0F, 0.0F).setColor(255, 255, 255, 255);
    }

    private static void shineLeftCornerVertex(VertexConsumer p_114215_, Matrix4f p_114216_, float p_114217_, float p_114218_, float xOffset, float yOffset) {
        p_114215_.addVertex(p_114216_, -HALF_SQRT_3 * p_114218_, p_114217_, 0).setColor(200, 235, 255, 0);
    }

    private static void shineRightCornerVertex(VertexConsumer p_114224_, Matrix4f p_114225_, float p_114226_, float p_114227_, float xOffset, float yOffset) {
        p_114224_.addVertex(p_114225_, HALF_SQRT_3 * p_114227_, p_114226_, 0).setColor(200, 235, 255, 0);
    }

    private static Identifier getSubmarineBaseTexture(SubmarineEntity entity) {
        switch (entity.getOxidizationLevel()) {
            case 0:
                return TEXTURE;
            case 1:
                return TEXTURE_EXPOSED;
            case 2:
                return TEXTURE_WEATHERED;
            case 3:
                return TEXTURE_OXIDIZED;
        }
        return TEXTURE;
    }

    public Identifier getTextureLocation(SubmarineEntity entity) {
        return TEXTURE;
    }
}

