package folk.sisby.antique_atlas.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.render.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import com.mojang.math.Axis;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class DrawUtil {
	public static void drawCenteredWithRotation(PoseStack matrices, MultiBufferSource vertexConsumers, Identifier texture, double x, double y, float z, float scale, int textureWidth, int textureHeight, float rotation, int light, int argb) {
		matrices.push();
		matrices.translate(x, y, 0.0);
		matrices.scale(scale, scale, 1.0F);
		matrices.multiply(Axis.POSITIVE_Z.rotationDegrees(180 + rotation));
		matrices.translate(-textureWidth / 2f, -textureHeight / 2f, 0f);
		DrawBatcher.drawSingle(matrices, vertexConsumers, texture, textureWidth, textureHeight, light, 0, 0, z, textureWidth, textureHeight, 0, 0, textureWidth, textureHeight, argb, false);
		matrices.pop();
	}

	public static void fill(PoseStack matrices, MultiBufferSource vertexConsumers, RenderType layer, float z, int light, int x1, int y1, int x2, int y2, float alpha, float[] color) {
		BufferBuilder bufferBuilder = null;
		VertexConsumer vertexConsumer;
		if (vertexConsumers == null) {
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			Minecraft.getInstance().gameRenderer.getLightmapTextureManager().enable();
			RenderSystem.setShader(GameRenderer::getPositionColorLightmapProgram);
			bufferBuilder = Tesselator.getInstance().begin(VertexFormat.DrawMode.QUADS, DefaultVertexFormat.POSITION_COLOR_LIGHT);
			vertexConsumer = bufferBuilder;
		} else {
			vertexConsumer = vertexConsumers.getBuffer(layer);
		}

		Matrix4f matrix4f = matrices.peek().pose();

		vertexConsumer.addVertex(matrix4f, x1, y1, z).setColor(color[0], color[1], color[2], alpha).light(light);
		vertexConsumer.addVertex(matrix4f, x1, y2, z).setColor(color[0], color[1], color[2], alpha).light(light);
		vertexConsumer.addVertex(matrix4f, x2, y2, z).setColor(color[0], color[1], color[2], alpha).light(light);
		vertexConsumer.addVertex(matrix4f, x2, y1, z).setColor(color[0], color[1], color[2], alpha).light(light);
		if (bufferBuilder != null) {
			BufferRenderer.drawWithGlobalProgram(bufferBuilder.buildOrThrow());
			Minecraft.getInstance().gameRenderer.getLightmapTextureManager().disable();
			RenderSystem.disableBlend();
		}
	}
}
