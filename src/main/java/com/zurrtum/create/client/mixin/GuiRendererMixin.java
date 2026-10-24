package com.zurrtum.create.client.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem.AutoStorageIndexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zurrtum.create.client.catnip.gui.render.*;
import com.zurrtum.create.client.catnip.render.CustomRenderPipeline;
import com.zurrtum.create.client.foundation.gui.render.*;
import com.zurrtum.create.client.ponder.foundation.render.SceneRenderState;
import com.zurrtum.create.client.ponder.foundation.render.SceneRenderer;
import com.zurrtum.create.client.ponder.foundation.render.TitleTextRenderState;
import com.zurrtum.create.client.ponder.foundation.render.TitleTextRenderer;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
    @WrapOperation(method = "executeDraw(Lnet/minecraft/client/gui/render/GuiRenderer$Draw;Lcom/mojang/blaze3d/systems/RenderPass;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;setIndexBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;)V"))
    private void setIndexBuffer(
        RenderPass instance,
        GpuBuffer gpuBuffer,
        VertexFormat.IndexType indexType,
        Operation<Void> original,
        @Local(argsOnly = true) GuiRenderer.Draw draw
    ) {
        AutoStorageIndexBuffer sequentialBuffer = ((CustomRenderPipeline) draw.pipeline()).create$getSequentialBuffer();
        if (sequentialBuffer != null) {
            original.call(instance, sequentialBuffer.getBuffer(draw.indexCount()), sequentialBuffer.type());
        } else {
            original.call(instance, gpuBuffer, indexType);
        }
    }
}
