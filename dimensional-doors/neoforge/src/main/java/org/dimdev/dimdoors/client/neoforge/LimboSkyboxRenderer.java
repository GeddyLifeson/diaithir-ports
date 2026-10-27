package org.dimdev.dimdoors.client.neoforge;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;
import org.dimdev.dimdoors.DimensionalDoors;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Limbo sky: a fixed sun above and a moon below the player. Mirrors vanilla's SkyRenderer.renderSun,
 * but with its own textures instead of the celestial atlas. The background is the dimension's fog colour.
 */
public class LimboSkyboxRenderer implements CustomSkyboxRenderer {
    public static final Identifier ID = DimensionalDoors.id("limbo");
    private static final Identifier SUN = DimensionalDoors.id("textures/other/limbo_sun.png");
    private static final Identifier MOON = DimensionalDoors.id("textures/other/limbo_moon.png");

    private GpuBuffer quad;

    @Override
    public boolean renderSky(LevelRenderState levelRenderState, SkyRenderState skyRenderState, Matrix4fc modelViewMatrix, Runnable setupFog) {
        setupFog.run();
        draw(modelViewMatrix, SUN, 100.0F);
        draw(modelViewMatrix, MOON, -100.0F);
        return true;
    }

    private GpuBuffer quad() {
        if (this.quad == null) {
            VertexFormat format = DefaultVertexFormat.POSITION_TEX;
            try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(4 * format.getVertexSize())) {
                BufferBuilder builder = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, format);
                builder.addVertex(-1.0F, 0.0F, -1.0F).setUv(0.0F, 0.0F);
                builder.addVertex(1.0F, 0.0F, -1.0F).setUv(1.0F, 0.0F);
                builder.addVertex(1.0F, 0.0F, 1.0F).setUv(1.0F, 1.0F);
                builder.addVertex(-1.0F, 0.0F, 1.0F).setUv(0.0F, 1.0F);
                try (MeshData mesh = builder.buildOrThrow()) {
                    this.quad = RenderSystem.getDevice().createBuffer(() -> "Limbo sky quad", 32, mesh.vertexBuffer());
                }
            }
        }
        return this.quad;
    }

    private void draw(Matrix4fc modelView, Identifier texture, float height) {
        Minecraft mc = Minecraft.getInstance();
        AbstractTexture tex = mc.getTextureManager().getTexture(texture);
        Matrix4f transform = new Matrix4f(modelView).translate(0.0F, height, 0.0F).scale(30.0F, 1.0F, 30.0F);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(transform, new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());
        RenderTarget main = mc.getMainRenderTarget();
        GpuTextureView color = main.getColorTextureView();
        GpuTextureView depth = main.getDepthTextureView();
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = indices.getBuffer(6);

        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(() -> "Limbo sky", color, OptionalInt.empty(), depth, OptionalDouble.empty())) {
            pass.setPipeline(RenderPipelines.CELESTIAL);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamicTransforms);
            pass.bindTexture("Sampler0", tex.getTextureView(), tex.getSampler());
            pass.setVertexBuffer(0, this.quad());
            pass.setIndexBuffer(indexBuffer, indices.type());
            pass.drawIndexed(0, 0, 6, 1);
        }
    }
}
