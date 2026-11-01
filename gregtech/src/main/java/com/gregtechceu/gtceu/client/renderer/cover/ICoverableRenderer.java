package com.gregtechceu.gtceu.client.renderer.cover;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.utils.GTUtil;



import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ICoverableRenderer {

    @OnlyIn(Dist.CLIENT)
    TextureAtlasSprite[] COVER_BACK_PLATE = new TextureAtlasSprite[1];

    @OnlyIn(Dist.CLIENT)
    static void initSprites(TextureAtlas atlas) {
        COVER_BACK_PLATE[0] = atlas.getSprite(GTCEu.id("block/material_sets/dull/wire_side"));
    }

    @OnlyIn(Dist.CLIENT)
    default void renderCovers(List<BakedQuad> quads, @NotNull ICoverable coverable,
                              BlockPos pos, BlockAndTintGetter level, @Nullable Direction side,
                              RandomSource rand, @NotNull ModelData modelData, @Nullable RenderType renderType) {  /* ported stub */ }

    @OnlyIn(Dist.CLIENT)
    default void renderDynamicCovers(MetaMachine machine, float partialTick, PoseStack poseStack,
                                     MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ICoverable coverable = machine.getCoverContainer();
        for (Direction face : GTUtil.DIRECTIONS) {
            CoverBehavior cover = coverable.getCoverAtSide(face);
            IDynamicCoverRenderer renderer = cover != null ? cover.getDynamicRenderer().get() : null;
            if (renderer != null) {
                poseStack.pushPose();
                RenderUtil.moveToFace(poseStack, .5f, .5f, .5f, face);
                RenderUtil.rotateToFace(poseStack, face, Direction.NORTH);
                poseStack.translate(-.5f, -.5f, .01f);
                renderer.render(machine, face, partialTick, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();
            }
        }
    }
}
