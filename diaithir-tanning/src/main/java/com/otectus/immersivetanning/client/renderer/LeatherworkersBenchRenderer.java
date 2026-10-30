package com.otectus.immersivetanning.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.otectus.immersivetanning.block.LeatherworkersBenchBlock;
import com.otectus.immersivetanning.blockentity.LeatherworkersBenchBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** While a piece is being stitched, its cut leather and the piece itself lie on the bench's mat. */
public class LeatherworkersBenchRenderer implements BlockEntityRenderer<LeatherworkersBenchBlockEntity, LeatherworkersBenchRenderer.BenchRenderState> {
    private static final float PX = 1F / 16F;
    private final ItemModelResolver itemModelResolver;

    public LeatherworkersBenchRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    /** What the bench draws, copied from the block entity at extraction time. */
    public static class BenchRenderState extends BlockEntityRenderState {
        public boolean visible;
        public Direction facing = Direction.NORTH;
        public int leatherColor;
        public final ItemStackRenderState working = new ItemStackRenderState();
    }

    @Override
    public BenchRenderState createRenderState() {
        return new BenchRenderState();
    }

    @Override
    public void extractRenderState(LeatherworkersBenchBlockEntity bench, BenchRenderState state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(bench, state, partialTick, cameraPos, crumbling);
        ItemStack working = bench.working();
        state.visible = !working.isEmpty();
        if (!state.visible) {
            state.working.clear();
            return;
        }
        BlockState blockState = bench.getBlockState();
        state.facing = blockState.getBlock() instanceof LeatherworkersBenchBlock ? blockState.getValue(LeatherworkersBenchBlock.FACING) : Direction.NORTH;
        state.leatherColor = bench.leatherColor();
        itemModelResolver.updateForTopItem(state.working, working, ItemDisplayContext.FIXED, bench.getLevel(), null, (int) bench.getBlockPos().asLong());
    }

    @Override
    public void submit(BenchRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.visible) return;
        pose.pushPose();
        pose.translate(0.5, 0, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-((state.facing.get2DDataValue() + 2) % 4) * 90F));
        pose.translate(-0.5, 0, -0.5);
        // The cut leather panel.
        float[][][] panel = new float[2][2][];
        panel[0][0] = new float[]{4F * PX, 15.55F * PX, 4.5F * PX};
        panel[0][1] = new float[]{12F * PX, 15.55F * PX, 4.5F * PX};
        panel[1][0] = new float[]{4F * PX, 15.55F * PX, 11.5F * PX};
        panel[1][1] = new float[]{12F * PX, 15.55F * PX, 11.5F * PX};
        int light = state.lightCoords;
        int color = 0xFF000000 | state.leatherColor;
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TannersRackRenderer.LEATHER),
                (last, buffer) -> MeshUtil.sheet(last, buffer, panel, color, light));
        // The piece taking shape.
        pose.translate(0.5, 15.7F * PX, 0.5);
        pose.mulPose(Axis.XP.rotationDegrees(90F));
        pose.scale(0.45F, 0.45F, 0.45F);
        state.working.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }
}
