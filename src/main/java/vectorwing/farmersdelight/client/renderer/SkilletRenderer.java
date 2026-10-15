package vectorwing.farmersdelight.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import vectorwing.farmersdelight.common.block.StoveBlock;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;

import java.util.Random;

public class SkilletRenderer implements BlockEntityRenderer<SkilletBlockEntity, SkilletRenderer.State>
{
	private final Random random = new Random();
	private final ItemModelResolver itemModelResolver;

	public SkilletRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModelResolver = context.itemModelResolver();
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(SkilletBlockEntity skillet, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(skillet, state, partialTicks, cameraPosition, breakProgress);
		state.facing = skillet.getBlockState().getValue(StoveBlock.FACING);
		ItemStack stack = skillet.getStoredStack();
		state.seed = stack.isEmpty() ? 187 : Item.getId(stack.getItem()) + stack.getDamageValue();
		state.modelCount = getModelCount(stack);
		itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.FIXED, skillet.getLevel(), null, (int) skillet.getBlockPos().asLong());
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.item.isEmpty()) return;
		this.random.setSeed(state.seed);
		for (int i = 0; i < state.modelCount; i++) {
			poseStack.pushPose();
			// Stack up items in the skillet, with a slight offset per item
			float xOffset = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
			float zOffset = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
			poseStack.translate(0.5D + xOffset, 0.1D + 0.03 * (i + 1), 0.5D + zOffset);
			// Rotate item to face the skillet's front side
			poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));
			// Rotate item flat on the skillet. Use X and Y from now on
			poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			poseStack.scale(0.5F, 0.5F, 0.5F);
			state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
	}

	protected int getModelCount(ItemStack stack) {
		int modelCount = 1;
		if (stack.getCount() > 1) {
			modelCount += Mth.ceil(((float) stack.getCount() / stack.getMaxStackSize()) * 4);
		}
		return modelCount;
	}

	public static class State extends BlockEntityRenderState
	{
		Direction facing = Direction.NORTH;
		int seed;
		int modelCount;
		final ItemStackRenderState item = new ItemStackRenderState();
	}
}
