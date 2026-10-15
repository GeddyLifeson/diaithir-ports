/*
 * This file ("RenderReconstructorLens.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.blocks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.ellpeck.actuallyadditions.api.lens.ILensItem;
import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityAtomicReconstructor;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Shows the reconstructor's lens on the face it shoots from. */
public class ReconstructorRenderer implements BlockEntityRenderer<TileEntityAtomicReconstructor, ReconstructorRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public ReconstructorRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileEntityAtomicReconstructor tile, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        ItemStack stack = tile.inv.getStackInSlot(0);
        boolean show = tile.getBlockState().is(ActuallyBlocks.ATOMIC_RECONSTRUCTOR.get()) && !stack.isEmpty() && stack.getItem() instanceof ILensItem;
        this.itemModelResolver.updateForTopItem(state.lens, show ? stack : ItemStack.EMPTY, ItemDisplayContext.FIXED, tile.getLevel(), null, (int) tile.getBlockPos().asLong());
        if (!show) return;
        Direction direction = tile.getOrientation();
        state.yaw = 360.0f - direction.getOpposite().toYRot();
        state.pitch = direction == Direction.UP ? 90 : direction == Direction.DOWN ? -90 : 0;
        state.lensLight = LevelRenderer.getLightCoords(tile.getLevel(), tile.getBlockPos().relative(direction));
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.lens.isEmpty()) return;
        matrices.pushPose();
        matrices.translate(0.5F, 0.5F, 0.45F);
        matrices.mulPose(Axis.YP.rotationDegrees(state.yaw));
        matrices.mulPose(Axis.XP.rotationDegrees(state.pitch));
        matrices.translate(0.0F, 0.0F, -0.5F);
        matrices.scale(0.5F, 0.5F, 0.5F);
        state.lens.submit(matrices, collector, state.lensLight, OverlayTexture.NO_OVERLAY, 0);
        matrices.popPose();
    }

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState lens = new ItemStackRenderState();
        float yaw;
        float pitch;
        int lensLight;
    }
}
