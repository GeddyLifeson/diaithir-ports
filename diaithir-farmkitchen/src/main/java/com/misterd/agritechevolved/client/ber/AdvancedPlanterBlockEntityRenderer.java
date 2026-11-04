package com.misterd.agritechevolved.client.ber;

import com.misterd.agritechevolved.block.custom.AdvancedPlanterBlock;
import com.misterd.agritechevolved.blockentity.custom.AdvancedPlanterBlockEntity;
import com.misterd.agritechevolved.util.RegistryHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class AdvancedPlanterBlockEntityRenderer
        implements BlockEntityRenderer<AdvancedPlanterBlockEntity, AdvancedPlanterBlockEntityRenderer.RenderState> {

    private final ItemModelResolver itemModelResolver;
    private final BlockModelResolver blockModelResolver;

    public AdvancedPlanterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        this.blockModelResolver = context.blockModelResolver();
    }

    public static class RenderState extends PlanterBlockEntityRenderer.RenderState {}

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(AdvancedPlanterBlockEntity be, RenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPos, crumblingOverlay);

        state.cloched = be.getBlockState().getValue(AdvancedPlanterBlock.CLOCHED);
        state.soilStack = be.getStack(1).copy();
        state.plantStack = be.getStack(0).copy();
        state.growthProgress = be.getGrowthProgress();
        state.growthStage = be.getGrowthStage();
        state.isTree = PlanterBlockEntityRenderer.isTreePlant(state.plantStack);
        state.distanceSq = cameraPos.distanceToSqr(Vec3.atCenterOf(be.getBlockPos()));
        state.soilIsWater = !state.soilStack.isEmpty() && RegistryHelper.getItemId(state.soilStack).equals("minecraft:water_bucket");

        PlanterBlockEntityRenderer.populateRenderState(state,
                state.soilStack, state.soilIsWater, state.plantStack,
                state.isTree, state.growthStage, be.getLevel(),
                itemModelResolver, blockModelResolver);
    }

    @Override
    public void submit(RenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        PlanterBlockEntityRenderer.submitShared(
                state.cloched,
                state.distanceSq,
                state.soilStack,
                state.soilIsWater,
                state.soilRenderState,
                state.plantStack,
                state.isTree,
                state.plantModel,
                state.growthProgress,
                state.lightCoords,
                poseStack,
                collector);
    }
}