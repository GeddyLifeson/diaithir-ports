package sonar.fluxnetworks.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.common.device.TileFluxStorage;

import javax.annotation.Nonnull;

/**
 * Renders the energy level inside the flux storage block.
 */
public class FluxStorageEntityRenderer implements BlockEntityRenderer<TileFluxStorage, FluxStorageEntityRenderer.State> {

    public static final BlockEntityRendererProvider<TileFluxStorage, State> PROVIDER = context -> new FluxStorageEntityRenderer();

    private static final RenderType ENERGY = RenderTypes.entityTranslucent(
            FluxNetworks.location("textures/block/flux_storage_energy.png"));

    private static final float START = 2.0f / 16.0f;
    private static final float END = 14.0f / 16.0f;
    private static final float OFFSET = 1.0f / 16.0f;
    private static final float WIDTH = 12.0f / 16.0f;
    private static final float HEIGHT = 13.0f / 16.0f;

    private static final int ALPHA = 150;

    public static class State extends BlockEntityRenderState {
        int color;
        long energy;
        long capacity;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileFluxStorage entity, State state, float partialTick, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTick, cameraPosition, breakProgress);
        state.color = entity.mClientColor;
        state.energy = entity.getTransferBuffer();
        state.capacity = entity.getMaxTransferLimit();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.energy <= 0 || state.capacity <= 0) {
            return;
        }
        float renderHeight = Math.min(HEIGHT * state.energy / state.capacity, HEIGHT);

        int r = state.color >> 16 & 0xFF;
        int g = state.color >> 8 & 0xFF;
        int b = state.color & 0xFF;

        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            renderSide(poseStack, collector, dir, START, OFFSET, END, WIDTH, renderHeight, r, g, b, -1);
        }
        if (renderHeight < HEIGHT) {
            renderSide(poseStack, collector, Direction.UP, OFFSET, START + renderHeight, OFFSET,
                    END, -END, r, g, b, 1);
        }
    }

    private static void renderSide(@Nonnull PoseStack stack, SubmitNodeCollector collector, @Nonnull Direction dir,
                                   float x, float y, float z, float width, float height,
                                   int r, int g, int b, int normalY) {
        stack.pushPose();
        stack.translate(0.5, 0.5, 0.5);
        stack.mulPose(dir.getRotation());
        stack.translate(-0.5, -0.5, -0.5);
        collector.submitCustomGeometry(stack, ENERGY, (pose, consumer) -> {
            vertex(pose, consumer, x, y, z - height, r, g, b, normalY);
            vertex(pose, consumer, x + width, y, z - height, r, g, b, normalY);
            vertex(pose, consumer, x + width, y, z, r, g, b, normalY);
            vertex(pose, consumer, x, y, z, r, g, b, normalY);
        });
        stack.popPose();
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float z,
                               int r, int g, int b, int normalY) {
        consumer.addVertex(pose, x, y, z).setColor(r, g, b, ALPHA).setUv(x, z)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, 0, normalY, 0);
    }
}
