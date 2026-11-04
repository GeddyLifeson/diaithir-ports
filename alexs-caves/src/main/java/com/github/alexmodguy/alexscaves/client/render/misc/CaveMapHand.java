package com.github.alexmodguy.alexscaves.client.render.misc;

import com.github.alexmodguy.alexscaves.server.item.CaveMapItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderHandEvent;

/**
 * First-person hold poses for the cave map: one-handed in the off hand or with a full hand, two-handed when the off hand is empty,
 * the same arm transforms as a vanilla map, with the cave map texture drawn instead of the saved map data.
 */
public class CaveMapHand {
    private static final Identifier MAP_BACKGROUND = Identifier.withDefaultNamespace("textures/map/map_background.png");

    public static void onRenderHand(RenderHandEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof CaveMapItem)) return;
        Minecraft mc = Minecraft.getInstance();
        AbstractClientPlayer player = mc.player;
        if (player == null) return;
        event.setCanceled(true);
        PoseStack ps = event.getPoseStack();
        SubmitNodeCollector c = event.getSubmitNodeCollector();
        int light = event.getPackedLight();
        boolean main = event.getHand() == InteractionHand.MAIN_HAND;
        HumanoidArm arm = main ? player.getMainArm() : player.getMainArm().getOpposite();
        ps.pushPose();
        if (main && player.getOffhandItem().isEmpty()) {
            twoHanded(ps, c, light, player, event.getInterpolatedPitch(), event.getEquipProgress(), event.getSwingProgress(), stack);
        } else {
            oneHanded(ps, c, light, player, event.getEquipProgress(), arm, event.getSwingProgress(), stack);
        }
        ps.popPose();
    }

    private static float mapTilt(float xRot) {
        float tilt = Mth.clamp(1.0F - xRot / 45.0F + 0.1F, 0.0F, 1.0F);
        return -Mth.cos(tilt * (float) Math.PI) * 0.5F + 0.5F;
    }

    private static void twoHanded(PoseStack ps, SubmitNodeCollector c, int light, AbstractClientPlayer player, float xRot, float equip, float attack, ItemStack stack) {
        float sqrtAttack = Mth.sqrt(attack);
        float ySwing = -0.2F * Mth.sin(attack * (float) Math.PI);
        float zSwing = -0.4F * Mth.sin(sqrtAttack * (float) Math.PI);
        ps.translate(0.0F, -ySwing / 2.0F, zSwing);
        float tilt = mapTilt(xRot);
        ps.translate(0.0F, 0.04F + equip * -1.2F + tilt * -0.5F, -0.72F);
        ps.mulPose(Axis.XP.rotationDegrees(tilt * -85.0F));
        if (!player.isInvisible()) {
            ps.pushPose();
            ps.mulPose(Axis.YP.rotationDegrees(90.0F));
            mapHand(ps, c, light, player, HumanoidArm.RIGHT);
            mapHand(ps, c, light, player, HumanoidArm.LEFT);
            ps.popPose();
        }
        ps.mulPose(Axis.XP.rotationDegrees(Mth.sin(sqrtAttack * (float) Math.PI) * 20.0F));
        ps.scale(2.0F, 2.0F, 2.0F);
        renderMap(ps, c, light, stack);
    }

    private static void oneHanded(PoseStack ps, SubmitNodeCollector c, int light, AbstractClientPlayer player, float equip, HumanoidArm arm, float attack, ItemStack stack) {
        float invert = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        ps.translate(invert * 0.125F, -0.125F, 0.0F);
        if (!player.isInvisible()) {
            ps.pushPose();
            ps.mulPose(Axis.ZP.rotationDegrees(invert * 10.0F));
            playerArm(ps, c, light, player, equip, attack, arm);
            ps.popPose();
        }
        ps.pushPose();
        ps.translate(invert * 0.51F, -0.08F + equip * -1.2F, -0.75F);
        float sqrtAttack = Mth.sqrt(attack);
        float xSwing = Mth.sin(sqrtAttack * (float) Math.PI);
        ps.translate(invert * -0.5F * xSwing, 0.4F * Mth.sin(sqrtAttack * (float) (Math.PI * 2)) - 0.3F * xSwing, -0.3F * Mth.sin(attack * (float) Math.PI));
        ps.mulPose(Axis.XP.rotationDegrees(xSwing * -45.0F));
        ps.mulPose(Axis.YP.rotationDegrees(invert * xSwing * -30.0F));
        renderMap(ps, c, light, stack);
        ps.popPose();
    }

    private static void mapHand(PoseStack ps, SubmitNodeCollector c, int light, AbstractClientPlayer player, HumanoidArm arm) {
        var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getPlayerRenderer(player);
        ps.pushPose();
        float invert = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        ps.mulPose(Axis.YP.rotationDegrees(92.0F));
        ps.mulPose(Axis.XP.rotationDegrees(45.0F));
        ps.mulPose(Axis.ZP.rotationDegrees(invert * -41.0F));
        ps.translate(invert * 0.3F, -1.1F, 0.45F);
        Identifier skin = player.getSkin().body().texturePath();
        if (arm == HumanoidArm.RIGHT) {
            renderer.renderRightHand(ps, c, light, skin, player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE), player);
        } else {
            renderer.renderLeftHand(ps, c, light, skin, player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE), player);
        }
        ps.popPose();
    }

    private static void playerArm(PoseStack ps, SubmitNodeCollector c, int light, AbstractClientPlayer player, float equip, float attack, HumanoidArm arm) {
        boolean right = arm != HumanoidArm.LEFT;
        float invert = right ? 1.0F : -1.0F;
        float sqrtAttack = Mth.sqrt(attack);
        float xSwing = -0.3F * Mth.sin(sqrtAttack * (float) Math.PI);
        float ySwing = 0.4F * Mth.sin(sqrtAttack * (float) (Math.PI * 2));
        float zSwing = -0.4F * Mth.sin(attack * (float) Math.PI);
        ps.translate(invert * (xSwing + 0.64000005F), ySwing + -0.6F + equip * -0.6F, zSwing + -0.71999997F);
        ps.mulPose(Axis.YP.rotationDegrees(invert * 45.0F));
        ps.mulPose(Axis.YP.rotationDegrees(invert * Mth.sin(sqrtAttack * (float) Math.PI) * 70.0F));
        ps.mulPose(Axis.ZP.rotationDegrees(invert * Mth.sin(attack * attack * (float) Math.PI) * -20.0F));
        ps.translate(invert * -1.0F, 3.6F, 3.5F);
        ps.mulPose(Axis.ZP.rotationDegrees(invert * 120.0F));
        ps.mulPose(Axis.XP.rotationDegrees(200.0F));
        ps.mulPose(Axis.YP.rotationDegrees(invert * -135.0F));
        ps.translate(invert * 5.6F, 0.0F, 0.0F);
        var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getPlayerRenderer(player);
        Identifier skin = player.getSkin().body().texturePath();
        if (right) {
            renderer.renderRightHand(ps, c, light, skin, player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE), player);
        } else {
            renderer.renderLeftHand(ps, c, light, skin, player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE), player);
        }
    }

    private static void renderMap(PoseStack ps, SubmitNodeCollector c, int light, ItemStack stack) {
        ps.mulPose(Axis.YP.rotationDegrees(180.0F));
        ps.mulPose(Axis.ZP.rotationDegrees(180.0F));
        ps.scale(0.38F, 0.38F, 0.38F);
        ps.translate(-0.5F, -0.5F, 0.0F);
        ps.scale(0.0078125F, 0.0078125F, 0.0078125F);
        c.order(0).submitCustomGeometry(ps, RenderTypes.text(MAP_BACKGROUND), (pose, buffer) -> {
            buffer.addVertex(pose, -7.0F, 135.0F, 0.0F).setColor(-1).setUv(0.0F, 1.0F).setLight(light);
            buffer.addVertex(pose, 135.0F, 135.0F, 0.0F).setColor(-1).setUv(1.0F, 1.0F).setLight(light);
            buffer.addVertex(pose, 135.0F, -7.0F, 0.0F).setColor(-1).setUv(1.0F, 0.0F).setLight(light);
            buffer.addVertex(pose, -7.0F, -7.0F, 0.0F).setColor(-1).setUv(0.0F, 0.0F).setLight(light);
        });
        if (CaveMapItem.isFilled(stack) && !CaveMapItem.isLoading(stack)) {
            ps.pushPose();
            ps.translate(0.0F, 128.0F, 0.0F);
            ps.scale(128.0F, -128.0F, -0.6F);
            CaveMapRenderer.getMapFor(stack).submit(ps, c.order(1), light);
            ps.popPose();
        }
    }
}
