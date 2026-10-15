/*
 * This file ("RenderDisplayStand.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.blocks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityDisplayStand;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderDisplayStand implements BlockEntityRenderer<TileEntityDisplayStand, AssetUtil.FloatingItemState> {
    private final ItemModelResolver itemModelResolver;

    public RenderDisplayStand(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public AssetUtil.FloatingItemState createRenderState() {
        return new AssetUtil.FloatingItemState();
    }

    @Override
    public void extractRenderState(TileEntityDisplayStand tile, AssetUtil.FloatingItemState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.extract(this.itemModelResolver, tile.inv.getStackInSlot(0), tile);
    }

    @Override
    public void submit(AssetUtil.FloatingItemState state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera) {
        state.submit(matrices, collector);
    }
}
