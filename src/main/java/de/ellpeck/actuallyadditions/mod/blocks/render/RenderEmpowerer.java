/*
 * This file ("RenderEmpowerer.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.blocks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.ellpeck.actuallyadditions.mod.crafting.EmpowererRecipe;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityEmpowerer;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderEmpowerer implements BlockEntityRenderer<TileEntityEmpowerer, RenderEmpowerer.State> {
    private static final Vec3 CENTRE = new Vec3(0, 0, 0);
    private final ItemModelResolver itemModelResolver;

    public RenderEmpowerer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileEntityEmpowerer tile, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.extract(this.itemModelResolver, tile.inv.getStackInSlot(0), tile);
        RecipeHolder<EmpowererRecipe> recipe = tile.getCurrentRecipe();
        state.crafting = recipe != null;
        if (recipe != null) state.laserColor = recipe.value().getParticleColors();
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera) {
        state.submit(matrices, collector);
        if (!state.crafting) return;
        // a laser from each of the four display stands into the empowerer while it works
        for (int i = 0; i <= 3; i++) {
            BlockPos offset = BlockPos.ZERO.relative(Direction.from2DDataValue(i), 3);
            AssetUtil.submitLaser(matrices, collector, CENTRE, new Vec3(offset.getX(), offset.getY() + 0.45, offset.getZ()), 80, state.laserColor, 1.0F, 0.1F);
        }
    }

    public static class State extends AssetUtil.FloatingItemState {
        boolean crafting;
        int laserColor;
    }
}
