package vectorwing.farmersdelight.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import vectorwing.farmersdelight.common.block.StoveBlock;
import vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class DefaultStoveRenderer<T extends AbstractStoveBlockEntity> implements BlockEntityRenderer<T, DefaultStoveRenderer.State>
{
	private static final float SIZE = 0.375F;
	private final ItemModelResolver itemModelResolver;

	public DefaultStoveRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModelResolver = context.itemModelResolver();
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(T stove, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(stove, state, partialTicks, cameraPosition, breakProgress);
		state.facing = stove.getBlockState().getValue(StoveBlock.FACING).getOpposite();
		// items sit on top of the stove, so they are lit by the block above it
		state.topLight = stove.getLevel() != null ? LevelRenderer.getLightCoords(stove.getLevel(), stove.getBlockPos().above()) : state.lightCoords;
		state.items = new ArrayList<>();
		state.offsets = new ArrayList<>();
		int seed = (int) stove.getBlockPos().asLong();
		var items = stove.getItems();
		for (int i = 0; i < items.size(); ++i) {
			ItemStackRenderState itemState = new ItemStackRenderState();
			itemModelResolver.updateForTopItem(itemState, items.getStackInSlot(i), ItemDisplayContext.FIXED, stove.getLevel(), null, seed + i);
			state.items.add(itemState);
			state.offsets.add(stove.getStoveItemOffset(i));
		}
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		for (int i = 0; i < state.items.size(); ++i) {
			ItemStackRenderState itemState = state.items.get(i);
			if (itemState.isEmpty()) continue;
			poseStack.pushPose();
			// Center item above the stove
			poseStack.translate(0.5D, 1.02D, 0.5D);
			// Rotate item to face the stove's front side
			poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));
			// Rotate item flat on the stove. Use X and Y from now on
			poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			// Neatly align items according to their index
			Vec2 itemOffset = state.offsets.get(i);
			poseStack.translate(itemOffset.x, itemOffset.y, 0.0D);
			poseStack.scale(SIZE, SIZE, SIZE);
			itemState.submit(poseStack, collector, state.topLight, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
	}

	public static class State extends BlockEntityRenderState
	{
		Direction facing = Direction.NORTH;
		int topLight;
		List<ItemStackRenderState> items = List.of();
		List<Vec2> offsets = List.of();
	}
}
