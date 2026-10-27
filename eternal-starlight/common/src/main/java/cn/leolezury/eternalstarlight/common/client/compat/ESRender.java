package cn.leolezury.eternalstarlight.common.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Helpers replacing the removed ItemRenderer / BlockRenderDispatcher immediate-mode entry points.
 */
public final class ESRender {
	private static final java.util.Map<net.minecraft.client.renderer.entity.state.EntityRenderState, Entity> STATE_ENTITIES = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

	private ESRender() {
	}

	/** Remembers which entity a vanilla render state was extracted from (filled by a NeoForge render state modifier). */
	public static void bindEntity(net.minecraft.client.renderer.entity.state.EntityRenderState state, Entity entity) {
		STATE_ENTITIES.put(state, entity);
	}

	@Nullable
	public static Entity entityOf(net.minecraft.client.renderer.entity.state.EntityRenderState state) {
		return STATE_ENTITIES.get(state);
	}

	public static void submitItem(SubmitNodeCollector collector, ItemStack stack, ItemDisplayContext context, PoseStack poseStack, int light, int overlay, @Nullable Level level, @Nullable Entity owner, int seed) {
		if (stack.isEmpty()) return;
		ItemStackRenderState state = new ItemStackRenderState();
		Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, stack, context, level, owner, seed);
		state.submit(poseStack, collector, light, overlay, 0);
	}

	public static void submitItem(SubmitNodeCollector collector, ItemStack stack, ItemDisplayContext context, PoseStack poseStack, int light, Entity owner) {
		submitItem(collector, stack, context, poseStack, light, OverlayTexture.NO_OVERLAY, owner.level(), owner, owner.getId());
	}

	public static void submitBlock(SubmitNodeCollector collector, BlockState blockState, PoseStack poseStack, int light, int overlay) {
		BlockModelRenderState state = new BlockModelRenderState();
		new net.minecraft.client.renderer.block.BlockModelResolver(Minecraft.getInstance().getModelManager()).update(state, blockState, net.minecraft.client.renderer.block.model.BlockDisplayContext.create());
		state.submit(poseStack, collector, light, overlay, 0);
	}

	/** Stand-in for ItemRenderer.getFoilBufferDirect: the foil glint is not reproduced. */
	public static VertexConsumer foilBuffer(MultiBufferSource buffer, RenderType renderType, boolean solid, boolean glint) {
		return buffer.getBuffer(renderType);
	}

	public static org.joml.Quaternionf cameraOrientation() {
		return Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
	}

	/** Replacement for EntityRenderDispatcher.render(...): extracts the state and submits it. */
	public static void submitEntity(net.minecraft.client.renderer.entity.EntityRenderDispatcher dispatcher, Entity entity, float partialTick, double x, double y, double z, PoseStack poseStack, SubmitNodeCollector collector, net.minecraft.client.renderer.state.level.CameraRenderState camera, int light) {
		net.minecraft.client.renderer.entity.state.EntityRenderState state = dispatcher.extractEntity(entity, partialTick);
		state.lightCoords = light;
		dispatcher.submit(state, camera, x, y, z, poseStack, collector);
	}

	/** Replacement for SpawnerRenderer.renderEntityInSpawner. */
	public static void renderEntityInSpawner(float partialTick, PoseStack poseStack, SubmitNodeCollector collector, net.minecraft.client.renderer.state.level.CameraRenderState camera, int light, Entity entity, net.minecraft.client.renderer.entity.EntityRenderDispatcher dispatcher, double oSpin, double spin) {
		float scale = 0.53125F;
		float maxLength = Math.max(entity.getBbWidth(), entity.getBbHeight());
		if (maxLength > 1.0) {
			scale /= maxLength;
		}
		poseStack.pushPose();
		poseStack.translate(0.5F, 0.4F, 0.5F);
		poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees((float) net.minecraft.util.Mth.lerp((double) partialTick, oSpin, spin) * 10.0F));
		poseStack.translate(0.0F, -0.2F, 0.0F);
		poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-30.0F));
		poseStack.scale(scale, scale, scale);
		submitEntity(dispatcher, entity, partialTick, 0.0, 0.0, 0.0, poseStack, collector, camera, light);
		poseStack.popPose();
	}
}
