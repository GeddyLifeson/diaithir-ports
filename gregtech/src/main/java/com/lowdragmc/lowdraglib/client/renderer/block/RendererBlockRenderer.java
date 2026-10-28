package com.lowdragmc.lowdraglib.client.renderer.block;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

class RendererBlockRenderer implements IRenderer {

    public Optional<RendererBlockEntity> getMachine(@Nullable BlockEntity blockEntity) {
        return Optional.ofNullable(blockEntity).filter(RendererBlockEntity.class::isInstance).map(RendererBlockEntity.class::cast);
    }

    public Optional<RendererBlockEntity> getMachine(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos) {
        if (level == null || pos == null)
            return Optional.empty();
        return getMachine(level.getBlockEntity(pos));
    }/* ported: removed */

    @Override
    public List<BakedQuad> renderModel(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, @Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) { return null; /* ported stub */ }

    @Override
    public boolean hasTESR(BlockEntity blockEntity) { return false; /* ported stub */ }

    @Override
    public boolean isGlobalRenderer(BlockEntity blockEntity) { return false; /* ported stub */ }

    @Override
    public boolean shouldRender(BlockEntity blockEntity, Vec3 cameraPos) { return false; /* ported stub */ }

    @Override
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack stack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {  /* ported stub */ }
}
