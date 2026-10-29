package com.gregtechceu.gtceu.client.renderer;



import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("unchecked")
@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class BlockEntityWithBERModelRenderer<T extends BlockEntity>  {/* ported: removed */

    public BlockEntityWithBERModelRenderer(BlockEntityRendererProvider.Context context) {
    }

    
    
    public void render(T blockEntity, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {  /* ported stub */ }

    
    
    public boolean shouldRenderOffScreen(T blockEntity) { return false; /* ported stub */ }

    
    
    public boolean shouldRender(T blockEntity, Vec3 cameraPos) { return false; /* ported stub */ }
}
