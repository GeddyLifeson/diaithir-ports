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
	private ESRender() {
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
}
