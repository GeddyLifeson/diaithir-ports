package com.otectus.immersivetanning.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.otectus.immersivetanning.blockentity.TannersVatBlockEntity;
import com.otectus.immersivetanning.hide.HideSize;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The vat's state is its liquor: clear water, darkening toward tannin brown as bark is added, with the hides visible
 * as folded shapes just under the surface. Tanned hides float up and turn the colour of wet leather.
 */
public class TannersVatRenderer implements BlockEntityRenderer<TannersVatBlockEntity, TannersVatRenderer.VatRenderState> {
    private static final SpriteId WATER = new SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier.fromNamespaceAndPath("minecraft", "block/water_still"));
    private static final int WATER_COLOR = 0x3F76E4;
    private static final int TANNIN_COLOR = 0x9A6230;
    private static final int SCRAPED_COLOR = 0xD9C2A0;
    private static final float PX = 1F / 16F;
    private final SpriteGetter sprites;

    public TannersVatRenderer(BlockEntityRendererProvider.Context context) {
        sprites = context.sprites();
    }

    /** One hide in the vat: its size and how far its tanning has come. */
    public record HideSlot(HideSize size, float progress) {}

    /** What the vat draws, copied from the block entity at extraction time. */
    public static class VatRenderState extends BlockEntityRenderState {
        public boolean water;
        public int tannin;
        public final List<HideSlot> slots = new ArrayList<>();
    }

    @Override
    public VatRenderState createRenderState() {
        return new VatRenderState();
    }

    @Override
    public void extractRenderState(TannersVatBlockEntity vat, VatRenderState state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(vat, state, partialTick, cameraPos, crumbling);
        long time = vat.getLevel() != null ? vat.getLevel().getGameTime() : 0L;
        state.water = vat.hasWater();
        state.tannin = vat.tannin();
        state.slots.clear();
        for (TannersVatBlockEntity.Slot slot : vat.slots()) {
            state.slots.add(new HideSlot(slot.hide().size(), slot.progress(time)));
        }
    }

    @Override
    public void submit(VatRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        boolean water = state.water;
        float surface = water ? 12.0F : 2.3F;
        int light = state.lightCoords;
        List<HideSlot> slots = state.slots;
        for (int i = 0; i < slots.size(); i++) {
            HideSlot slot = slots.get(i);
            float progress = slot.progress();
            int color = MeshUtil.mix(SCRAPED_COLOR, TannersRackRenderer.WET_COLOR, progress);
            float done = progress >= 1F ? 1F : 0F;
            float y = water ? surface - 1.4F + done * 1.0F - (i % 2) * 0.35F : 2.3F + i * 0.25F;
            pose.pushPose();
            pose.translate(0.5, y * PX, 0.5);
            pose.mulPose(Axis.YP.rotationDegrees(i * 67F + 20F));
            pose.mulPose(Axis.XP.rotationDegrees(8F + (i % 3) * 5F));
            float half = switch (slot.size()) {
                case SMALL -> 2.8F;
                case MEDIUM -> 3.6F;
                case LARGE -> 4.3F;
            } * PX;
            float[][][] pos = new float[3][3][];
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    float fold = (r == 1 && c == 1) ? 0.6F * PX : 0F;
                    pos[r][c] = new float[]{(c - 1) * half, fold, (r - 1) * half};
                }
            }
            collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TannersRackRenderer.LEATHER),
                    (last, buffer) -> MeshUtil.sheet(last, buffer, pos, 0xFF000000 | color, light));
            pose.popPose();
        }
        if (water) {
            float charge = Mth.clamp(state.tannin / 6F + (slots.isEmpty() ? 0F : 0.35F), 0F, 1F);
            int color = MeshUtil.mix(WATER_COLOR, TANNIN_COLOR, charge);
            int alpha = Math.round(Mth.lerp(charge, 180F, 215F));
            TextureAtlasSprite sprite = sprites.get(WATER);
            collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(WATER.atlasLocation()),
                    (last, buffer) -> horizontalQuad(last, buffer, sprite, 2.5F * PX, 2.5F * PX, 13.5F * PX, 13.5F * PX, surface * PX,
                            (alpha << 24) | (color & 0xFFFFFF), light));
        }
    }

    private static void horizontalQuad(PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite,
                                       float x0, float z0, float x1, float z1, float y, int argb, int light) {
        float u0 = sprite.getU(x0);
        float u1 = sprite.getU(x1);
        float v0 = sprite.getV(z0);
        float v1 = sprite.getV(z1);
        vertex(pose, buffer, x0, y, z0, u0, v0, argb, light);
        vertex(pose, buffer, x0, y, z1, u0, v1, argb, light);
        vertex(pose, buffer, x1, y, z1, u1, v1, argb, light);
        vertex(pose, buffer, x1, y, z0, u1, v0, argb, light);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u, float v, int argb, int light) {
        buffer.addVertex(pose, x, y, z).setColor(argb).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
    }
}
