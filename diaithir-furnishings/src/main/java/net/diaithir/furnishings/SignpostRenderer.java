package net.diaithir.furnishings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Draws the sign arms with the vanilla sign board model, so every wood looks like its own sign, and writes the two lines on both faces. */
public final class SignpostRenderer implements BlockEntityRenderer<SignpostEntity, SignpostRenderer.State> {
    private static final int TEXT_COLOR = 0xFF000000;
    private static final float TEXT_SCALE = 0.010416667F;
    private static final int MAX_TEXT_WIDTH = 78;
    private static final int LINE_HEIGHT = 10;

    public static final class ArmState {
        WoodType wood = WoodType.OAK;
        int dir;
        int slot;
        FormattedCharSequence[] lines = {FormattedCharSequence.EMPTY, FormattedCharSequence.EMPTY};
    }

    public static final class State extends BlockEntityRenderState {
        final ArmState[] arms = new ArmState[SignpostEntity.ARMS];
    }

    private final Font font;
    private final SpriteGetter sprites;
    private final Map<WoodType, Model.Simple> models = new HashMap<>();

    public SignpostRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
        this.sprites = context.sprites();
        WoodType.values().forEach(type -> models.put(type,
            new Model.Simple(context.entityModelSet().bakeLayer(ModelLayers.createWallSignModelName(type)), RenderTypes::entityCutout)));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SignpostEntity entity, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        for (int i = 0; i < SignpostEntity.ARMS; i++) {
            SignpostEntity.Arm arm = entity.arm(i);
            if (arm == null) {
                state.arms[i] = null;
                continue;
            }
            ArmState s = new ArmState();
            String wood = arm.wood();
            s.wood = WoodType.values().filter(w -> w.name().equals(wood)).findFirst().orElse(WoodType.OAK);
            s.dir = arm.dir();
            s.slot = i;
            s.lines = new FormattedCharSequence[] {
                Component.literal(arm.line1()).getVisualOrderText(), Component.literal(arm.line2()).getVisualOrderText()};
            state.arms[i] = s;
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        for (ArmState arm : state.arms) {
            if (arm != null) {
                submitArm(arm, state.lightCoords, state.breakProgress, pose, collector);
            }
        }
    }

    private void submitArm(ArmState arm, int light, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress,
                           PoseStack pose, SubmitNodeCollector collector) {
        float bottom = SignpostBlock.ARM_Y[arm.slot] / 16.0F;
        // The board's face looks a quarter turn to the left of the way it points, so the text runs along the arm.
        float faceYaw = arm.dir * 45.0F + 90.0F;
        float centre = (float) ((SignpostBlock.REACH_START + SignpostBlock.REACH_END) / 2.0);
        float face = (float) SignpostBlock.FACE;

        pose.pushPose();
        pose.translate(0.5F, bottom, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(-faceYaw));
        pose.translate(centre, 0.0F, face);
        pose.pushPose();
        pose.scale(0.6666667F, -0.5F, -0.5F);
        collector.submitModel(models.get(arm.wood), Unit.INSTANCE, pose, light, OverlayTexture.NO_OVERLAY, -1,
            Sheets.getSignSprite(arm.wood), sprites, 0, breakProgress);
        pose.popPose();
        float surface = (float) SignpostBlock.HALF_THICK + 0.004F;
        submitText(arm, light, pose, collector, surface, false);
        submitText(arm, light, pose, collector, surface, true);
        pose.popPose();
    }

    private void submitText(ArmState arm, int light, PoseStack pose, SubmitNodeCollector collector, float surface, boolean back) {
        pose.pushPose();
        if (back) {
            pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        }
        pose.translate(0.0F, 0.25F, surface);
        pose.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        for (int i = 0; i < 2; i++) {
            FormattedCharSequence line = arm.lines[i];
            int width = font.width(line);
            if (width == 0) {
                continue;
            }
            pose.pushPose();
            float fit = Math.min(1.0F, MAX_TEXT_WIDTH / (float) width);
            float y = i * LINE_HEIGHT - LINE_HEIGHT;
            pose.translate(0.0F, y + LINE_HEIGHT / 2.0F, 0.0F);
            pose.scale(fit, fit, 1.0F);
            pose.translate(0.0F, -LINE_HEIGHT / 2.0F, 0.0F);
            collector.submitText(pose, -width / 2.0F, 0.0F, line, false, Font.DisplayMode.POLYGON_OFFSET, light, TEXT_COLOR, 0, 0);
            pose.popPose();
        }
        pose.popPose();
    }

    @Override
    public int getViewDistance() {
        return 48;
    }
}
