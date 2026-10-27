package cn.leolezury.eternalstarlight.common.client.renderer.world;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.weather.ClientWeatherState;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public class ESSkyRenderer {
	private static final Identifier DEAD_STAR_LOCATION = EternalStarlight.id("textures/environment/dead_star.png");
	private static final float TIME = 12500f;
	private static final float FIXED_TIME = (float) (Mth.frac(TIME / 24000.0 - 0.25) * 2.0 + 0.5 - Math.cos(Mth.frac(TIME / 24000.0 - 0.25) * Math.PI) / 2.0) / 3.0F;
	private static final float STAR_SPHERE_RADIUS = 100.0F;
	private static final float DEAD_STAR_HALF_SIZE = 60.0F;
	// The opaque circle in dead_star.png is 48px wide in an 80px texture, i.e. 0.6 of the quad
	private static final float DEAD_STAR_DISK_RADIUS = DEAD_STAR_HALF_SIZE * 0.6F;
	private static final float STAR_HOLE_RADIUS = DEAD_STAR_DISK_RADIUS + 2.0F;
	private static final float STAR_FALLOFF_END_RADIUS = DEAD_STAR_HALF_SIZE;
	private static final float STAR_HOLE_ANGLE = (float) Mth.atan2(STAR_HOLE_RADIUS, STAR_SPHERE_RADIUS);
	private static final float STAR_FALLOFF_END_ANGLE = (float) Mth.atan2(STAR_FALLOFF_END_RADIUS, STAR_SPHERE_RADIUS);

	private static GpuBuffer starBuffer;
	private static GpuBuffer deadStarBuffer;
	private static int starIndexCount;
	private static final RenderSystem.AutoStorageIndexBuffer QUAD_INDICES = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);

	public static boolean renderSky(LevelRenderState levelRenderState, SkyRenderState skyRenderState, Matrix4fc modelViewMatrix, Runnable setupFog) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = minecraft.level;
		SkyRenderer vanillaSky = minecraft.levelRenderer.skyRenderer;
		if (level == null || vanillaSky == null) {
			return false;
		}
		setupFog.run();
		if (starBuffer == null) {
			createBuffers();
		}

		vanillaSky.renderSkyDisc(skyRenderState.skyColor);

		float rainLevel = level.getRainLevel(minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false));
		if (ClientWeatherState.weather != null) {
			rainLevel = ClientWeatherState.weather.modifyRainLevel(rainLevel);
		} else {
			ClientWeatherState.levelTarget = rainLevel;
			rainLevel = ClientWeatherState.getRainLevel(minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false));
		}
		float alpha = 1.0F - rainLevel;

		PoseStack poseStack = new PoseStack();
		poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
		poseStack.mulPose(Axis.XP.rotationDegrees(FIXED_TIME * 360.0F));

		renderStars(poseStack, 1.0F);
		renderDeadStar(poseStack, alpha);
		return true;
	}

	private static void renderStars(PoseStack poseStack, float brightness) {
		Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.mul(poseStack.last().pose());
		GpuTextureView color = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
		GpuTextureView depth = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
		GpuBuffer indexBuffer = QUAD_INDICES.getBuffer(starIndexCount);
		GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(modelViewStack, new Vector4f(brightness, brightness, brightness, brightness), new Vector3f(), new Matrix4f());
		try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "ES stars", color, OptionalInt.empty(), depth, OptionalDouble.empty())) {
			pass.setPipeline(RenderPipelines.STARS);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("DynamicTransforms", transforms);
			pass.setVertexBuffer(0, starBuffer);
			pass.setIndexBuffer(indexBuffer, QUAD_INDICES.type());
			pass.drawIndexed(0, 0, starIndexCount, 1);
		}
		modelViewStack.popMatrix();
	}

	private static void renderDeadStar(PoseStack poseStack, float alpha) {
		AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(DEAD_STAR_LOCATION);
		Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.mul(poseStack.last().pose());
		modelViewStack.translate(0.0F, 100.0F, 0.0F);
		modelViewStack.scale(DEAD_STAR_HALF_SIZE, 1.0F, DEAD_STAR_HALF_SIZE);
		GpuTextureView color = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
		GpuTextureView depth = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
		GpuBuffer indexBuffer = QUAD_INDICES.getBuffer(6);
		GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(modelViewStack, new Vector4f(1.0F, 1.0F, 1.0F, alpha), new Vector3f(), new Matrix4f());
		try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "ES dead star", color, OptionalInt.empty(), depth, OptionalDouble.empty())) {
			pass.setPipeline(RenderPipelines.CELESTIAL);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("DynamicTransforms", transforms);
			pass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());
			pass.setVertexBuffer(0, deadStarBuffer);
			pass.setIndexBuffer(indexBuffer, QUAD_INDICES.type());
			pass.drawIndexed(0, 0, 6, 1);
		}
		modelViewStack.popMatrix();
	}

	private static void createBuffers() {
		if (starBuffer != null) {
			starBuffer.close();
		}
		if (deadStarBuffer != null) {
			deadStarBuffer.close();
		}
		starBuffer = buildStars();
		try (ByteBufferBuilder builder = ByteBufferBuilder.exactlySized(4 * DefaultVertexFormat.POSITION_TEX.getVertexSize())) {
			BufferBuilder bufferBuilder = new BufferBuilder(builder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			bufferBuilder.addVertex(-1.0F, 0.0F, -1.0F).setUv(0.0F, 0.0F);
			bufferBuilder.addVertex(1.0F, 0.0F, -1.0F).setUv(1.0F, 0.0F);
			bufferBuilder.addVertex(1.0F, 0.0F, 1.0F).setUv(1.0F, 1.0F);
			bufferBuilder.addVertex(-1.0F, 0.0F, 1.0F).setUv(0.0F, 1.0F);
			try (MeshData mesh = bufferBuilder.buildOrThrow()) {
				deadStarBuffer = RenderSystem.getDevice().createBuffer(() -> "ES dead star quad", 32, mesh.vertexBuffer());
			}
		}
	}

	private static GpuBuffer buildStars() {
		RandomSource randomSource = RandomSource.create(10842L);
		RandomSource densityRandom = RandomSource.create(336103L);
		try (ByteBufferBuilder builder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION.getVertexSize() * 3000 * 4)) {
			BufferBuilder bufferBuilder = new BufferBuilder(builder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
			for (int j = 0; j < 3000; ++j) {
				float g = randomSource.nextFloat() * 2.0F - 1.0F;
				float h = randomSource.nextFloat() * 2.0F - 1.0F;
				float k = randomSource.nextFloat() * 2.0F - 1.0F;
				float l = 0.15F + randomSource.nextFloat() * 0.1F;
				float m = Mth.lengthSquared(g, h, k);
				if (!(m <= 0.010000001F) && !(m >= 1.0F)) {
					Vector3f center = new Vector3f(g, h, k).normalize(STAR_SPHERE_RADIUS);
					float n = (float) (randomSource.nextDouble() * Math.PI * 2.0);
					Matrix3f rotation = new Matrix3f().rotateTowards(new Vector3f(center).negate(), new Vector3f(0.0F, 1.0F, 0.0F)).rotateZ(-n);
					// Drop stars that would be drawn over the dead star's opaque disk (centered at local +Y),
					// fading the density back to normal around it so the sky still looks natural.
					float dot = h / Mth.sqrt(m);
					float angle = (float) Math.acos(Mth.clamp(dot, -1.0F, 1.0F));
					float keepChance = (float) Mth.smoothstep(Mth.clampedMap(angle, STAR_HOLE_ANGLE, STAR_FALLOFF_END_ANGLE, 0.0F, 1.0F));
					if (densityRandom.nextFloat() < keepChance) {
						bufferBuilder.addVertex(new Vector3f(l, -l, 0.0F).mul(rotation).add(center));
						bufferBuilder.addVertex(new Vector3f(l, l, 0.0F).mul(rotation).add(center));
						bufferBuilder.addVertex(new Vector3f(-l, l, 0.0F).mul(rotation).add(center));
						bufferBuilder.addVertex(new Vector3f(-l, -l, 0.0F).mul(rotation).add(center));
					}
				}
			}
			try (MeshData mesh = bufferBuilder.buildOrThrow()) {
				starIndexCount = mesh.drawState().indexCount();
				return RenderSystem.getDevice().createBuffer(() -> "ES stars", 40, mesh.vertexBuffer());
			}
		}
	}
}
