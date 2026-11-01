package net.diaithir.furnishings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Shows what is in the jar: one, two or three copies of the item, piled higher the fuller the jar is. */
public final class JarRenderer implements BlockEntityRenderer<JarEntity, JarRenderer.State> {
    public static final class State extends BlockEntityRenderState {
        @Nullable ItemStackRenderState item;
        int piles;
        float yaw;
        boolean open;
    }

    private final ItemModelResolver resolver;

    public JarRenderer(BlockEntityRendererProvider.Context context) {
        this.resolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(JarEntity jar, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(jar, state, partialTicks, cameraPosition, breakProgress);
        state.item = null;
        if (jar.count() > 0) {
            ItemStackRenderState item = new ItemStackRenderState();
            resolver.updateForTopItem(item, jar.kind(), ItemDisplayContext.FIXED, jar.getLevel(), null, HashCommon.long2int(jar.getBlockPos().asLong()));
            state.item = item;
            state.piles = 1 + (jar.count() * 3 > jar.capacity() ? 1 : 0) + (jar.count() * 3 > jar.capacity() * 2 ? 1 : 0);
            state.yaw = (HashCommon.long2int(jar.getBlockPos().asLong()) & 7) * 45.0F;
            state.open = jar.getBlockState().getBlock() == DiaithirFurnishings.CLAY_JAR.get();
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.item == null) {
            return;
        }
        AABB box = state.item.getModelBoundingBox();
        for (int i = 0; i < state.piles; i++) {
            pose.pushPose();
            float y = (state.open ? 0.42F : 0.24F) + i * 0.15F;
            pose.translate(0.5F, y, 0.5F);
            pose.mulPose(Axis.YP.rotationDegrees(state.yaw + i * 55.0F));
            pose.scale(0.42F, 0.42F, 0.42F);
            pose.translate(0.0, -(box.minY + box.maxY) / 2.0, 0.0);
            state.item.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }
}
