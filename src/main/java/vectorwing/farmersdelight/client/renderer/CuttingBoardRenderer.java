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
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import vectorwing.farmersdelight.common.block.CuttingBoardBlock;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.tag.ModTags;

import java.util.Random;

public class CuttingBoardRenderer implements BlockEntityRenderer<CuttingBoardBlockEntity, CuttingBoardRenderer.State>
{
	private final Random random = new Random();
	private final ItemModelResolver itemModelResolver;

	public CuttingBoardRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModelResolver = context.itemModelResolver();
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(CuttingBoardBlockEntity cuttingBoard, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(cuttingBoard, state, partialTicks, cameraPosition, breakProgress);
		ItemStack itemStack = cuttingBoard.getStoredItem();
		state.facing = cuttingBoard.getBlockState().getValue(CuttingBoardBlock.FACING).getOpposite();
		state.seed = itemStack.isEmpty() ? 187 : Item.getId(itemStack.getItem()) + itemStack.getDamageValue();
		state.modelCount = getModelCount(itemStack);
		state.carved = cuttingBoard.isItemCarvingBoard();
		state.flat = itemStack.is(ModTags.Items.FLAT_ON_CUTTING_BOARD);
		state.carveAngle = carveAngle(itemStack);
		itemModelResolver.updateForTopItem(state.item, itemStack, ItemDisplayContext.FIXED, cuttingBoard.getLevel(), null, (int) cuttingBoard.getBlockPos().asLong());
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.item.isEmpty()) return;
		this.random.setSeed(state.seed);
		// block models are lit like blocks; that is the 26.1 stand-in for the old "is GUI 3D" check
		boolean isBlockItem = state.item.usesBlockLight();
		for (int i = 0; i < state.modelCount; i++) {
			poseStack.pushPose();
			float xOffset = state.modelCount == 1 ? 0 : (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
			float zOffset = state.modelCount == 1 ? 0 : (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
			if (state.carved) {
				renderItemCarved(poseStack, state.facing, state.carveAngle);
			} else if (isBlockItem && !state.flat) {
				renderBlock(poseStack, state.facing, xOffset, i, zOffset);
			} else {
				renderItemLayingDown(poseStack, state.facing, xOffset, i, zOffset);
			}
			state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
	}

	public void renderItemLayingDown(PoseStack matrixStackIn, Direction direction, float xOffset, int yIndex, float zOffset) {
		// Center item above the cutting board
		matrixStackIn.translate(0.5D + xOffset, 0.08D + 0.03 * (yIndex + 1), 0.5D + zOffset);
		// Rotate item to face the cutting board's front side
		matrixStackIn.mulPose(Axis.YP.rotationDegrees(-direction.toYRot()));
		// Rotate item flat on the cutting board. Use X and Y from now on
		matrixStackIn.mulPose(Axis.XP.rotationDegrees(90.0F));
		matrixStackIn.scale(0.6F, 0.6F, 0.6F);
	}

	public void renderBlock(PoseStack matrixStackIn, Direction direction, float xOffset, int yIndex, float zOffset) {
		// Center block above the cutting board
		matrixStackIn.translate(0.5D + xOffset, 0.27D + 0.03 * (yIndex + 1), 0.5D + zOffset);
		// Rotate block to face the cutting board's front side
		matrixStackIn.mulPose(Axis.YP.rotationDegrees(-direction.toYRot()));
		matrixStackIn.scale(0.8F, 0.8F, 0.8F);
	}

	public void renderItemCarved(PoseStack matrixStackIn, Direction direction, float poseAngle) {
		// Center item above the cutting board
		matrixStackIn.translate(0.5D, 0.23D, 0.5D);
		// Rotate item to face the cutting board's front side
		matrixStackIn.mulPose(Axis.YP.rotationDegrees(-direction.toYRot() + 180));
		// Rotate item to be carved on the surface
		matrixStackIn.mulPose(Axis.ZP.rotationDegrees(poseAngle));
		matrixStackIn.scale(0.6F, 0.6F, 0.6F);
	}

	/** A little less steep for hoes and pickaxes, steeper for tridents. Tools are told apart by tag since 1.21.2. */
	private static float carveAngle(ItemStack tool) {
		if (tool.is(ItemTags.PICKAXES) || tool.is(ItemTags.HOES)) return 225.0F;
		if (tool.getItem() instanceof TridentItem) return 135.0F;
		return 180.0F;
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
		boolean carved;
		boolean flat;
		float carveAngle;
		final ItemStackRenderState item = new ItemStackRenderState();
	}
}
